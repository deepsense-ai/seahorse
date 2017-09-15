/**
 * Copyright 2016, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang.doperables.stringindexingwrapper

import java.util.UUID

import org.apache.spark.annotation.DeveloperApi
import org.apache.spark.ml.feature.{IndexToString, StringIndexer}
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.ml.{Estimator => SparkEstimator, Pipeline}
import org.apache.spark.sql.types.{DataType, StructType}
import org.apache.spark.{ml, sql}

import io.deepsense.deeplang.doperables.dataframe.DataFrame

/**
  * In order to add string-indexing behaviour to estimators we need to put it into Sparks Pipeline
  */
private [stringindexingwrapper] object StringIndexingPipeline {

  def apply[M, T](
      dataFrame: DataFrame,
      sparkEstimator: SparkEstimator[_],
      labelColumnName: String,
      predictionColumnName: String): Pipeline = {

    val sparkDataFrame = dataFrame.sparkDataFrame

    val indexedLabelColumnName = UUID.randomUUID().toString
    val stringIndexer = new StringIndexer()
      .setInputCol(labelColumnName)
      .setOutputCol(indexedLabelColumnName)
      .fit(sparkDataFrame)

    val predictedLabelsColumnName = UUID.randomUUID().toString
    val labelConverter = new IndexToString()
      .setInputCol(predictionColumnName)
      .setOutputCol(predictedLabelsColumnName)
      .setLabels(stringIndexer.labels)

    val tempLabelsColumnName = UUID.randomUUID().toString
    val predictionColumnType = sparkDataFrame.schema(labelColumnName).dataType
    new Pipeline().setStages(Array(
      stringIndexer,
      new RenameColumnTransformer(labelColumnName, tempLabelsColumnName),
      new RenameColumnTransformer(indexedLabelColumnName, labelColumnName),
      sparkEstimator,
      labelConverter,
      new FilterNotTransformer(Set(labelColumnName)),
      new RenameColumnTransformer(tempLabelsColumnName, labelColumnName),
      new SetUpPredictionColumnTransformer(
        predictionColumnName,
        predictionColumnType,
        predictedLabelsColumnName)))
  }

  /**
    * Transformer that filters out prediction column and renames
    * predictedLabels column to prediction column.
    */
  private class SetUpPredictionColumnTransformer(
      predictionColumnName: String,
      predictionColumnType: DataType,
      predictedLabelsColumnName: String)
    extends ml.Transformer {

    import org.apache.spark.sql.functions._

    private val outSet =
      Set(predictedLabelsColumnName, predictionColumnName)

    override def transform(dataset: sql.DataFrame): sql.DataFrame = {
      val columnsNames = dataset.schema.fieldNames.filterNot(outSet.contains)
      val predictionColumnType = dataset.schema(predictionColumnName).dataType
      val cols = columnsNames.map(col) :+
        col(predictedLabelsColumnName).as(predictionColumnName).cast(predictionColumnType)
      dataset.select(cols: _*)
    }

    override def copy(extra: ml.param.ParamMap): ml.Transformer =
      new SetUpPredictionColumnTransformer(
        predictionColumnName,
        predictionColumnType,
        predictedLabelsColumnName)

    @DeveloperApi
    override def transformSchema(schema: StructType): StructType = {
      val columns = schema.fields.filterNot(field => outSet.contains(field.name)) :+
        schema(predictedLabelsColumnName).copy(
          name = predictionColumnName,
          dataType = predictionColumnType)
      StructType(columns)
    }

    override val uid: String = Identifiable.randomUID("SetUpPredictionColumnTransformer")
  }

  /**
    * Transformer that changes column name.
    *
    * @param originalColumnName column name to change.
    * @param newColumnName new column name.
    */
  private class RenameColumnTransformer(
      private val originalColumnName: String,
      private val newColumnName: String) extends ml.Transformer {

    override def transform(dataset: sql.DataFrame): sql.DataFrame = {
      // WARN: cannot use dataset.withColumnRenamed - it does not preserve metadata.
      val fieldsNames = dataset.schema.fieldNames
      val columns = fieldsNames.map { case name =>
        if (name == originalColumnName) {
          dataset(name).as(newColumnName)
        } else {
          dataset(name)
        }
      }
      val transformed = dataset.select(columns: _*)
      transformed
    }

    override def copy(extra: ml.param.ParamMap): ml.Transformer =
      new RenameColumnTransformer(originalColumnName, newColumnName)

    @DeveloperApi
    override def transformSchema(schema: StructType): StructType =
      StructType(schema.fields.map { case field =>
        if (field.name == originalColumnName) {
          field.copy(name = newColumnName)
        } else {
          field
        }
      })

    override val uid: String = Identifiable.randomUID("RenameColumnTransformer")
  }

  /**
    * Transformer that filters out columns specified in columnsToOmit.
    *
    * @param columnsToOmit columns to filter out.
    */
  private class FilterNotTransformer(
      private val columnsToOmit: Set[String]) extends ml.Transformer {

    override def transform(dataset: sql.DataFrame): sql.DataFrame = {
      val fieldsNames = dataset.schema.fieldNames.filterNot(columnsToOmit.contains)
      val columns = fieldsNames.map(dataset(_))
      val transformed = dataset.select(columns: _*)
      transformed
    }

    override def copy(extra: ml.param.ParamMap): ml.Transformer =
      new FilterNotTransformer(columnsToOmit)

    @DeveloperApi
    override def transformSchema(schema: StructType): StructType =
      StructType(schema.fields.filterNot(field => columnsToOmit.contains(field.name)))

    override val uid: String = Identifiable.randomUID("FilterNotTransformer")
  }
}
