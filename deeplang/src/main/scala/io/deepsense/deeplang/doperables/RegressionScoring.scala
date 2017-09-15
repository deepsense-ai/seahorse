/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.GeneralizedLinearModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}

import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}
import io.deepsense.deeplang.parameters.ColumnType
import io.deepsense.deeplang.{DKnowledge, DMethod1To1, ExecutionContext}
import io.deepsense.deeplang.doperables.dataframe.{CommonColumnMetadata, DataFrameBuilder, DataFrame}

trait RegressionScoring {

  this: Scorable =>

  def transformFeatures(v: RDD[Vector]): RDD[Vector]

  val featureColumns: Option[Seq[String]]

  val targetColumn: Option[String]

  def preparedModel: GeneralizedLinearModel

  override val score = new DMethod1To1[String, DataFrame, DataFrame] {

    override def apply(
        context: ExecutionContext)(
        predictionColumnName: String)(
        dataFrame: DataFrame): DataFrame = {
      val vectors: RDD[Vector] = dataFrame.toSparkVectorRDD(featureColumns.get)
      val transformedVectors = transformFeatures(vectors)
      transformedVectors.cache()
      val predictionsRDD: RDD[Double] = preparedModel.predict(transformedVectors)

      val outputSchema = StructType(dataFrame.sparkDataFrame.schema.fields :+
        StructField(predictionColumnName, DoubleType))

      val outputRDD = dataFrame.sparkDataFrame.rdd.zip(predictionsRDD).map({
        case (row, prediction) => Row.fromSeq(row.toSeq :+ prediction)
      })

      context.dataFrameBuilder.buildDataFrame(outputSchema, outputRDD)
    }

    override def inferFull(
        context: InferContext)(
        predictionColumnName: String)(
        dataFrameKnowledge: DKnowledge[DataFrame]): (DKnowledge[DataFrame], InferenceWarnings) = {

      // TODO when model metadata is introduced:
      // add support for checking if dataframe has correct columns

      val dataFrame = dataFrameKnowledge.types.head
      val newColumn = CommonColumnMetadata(
        predictionColumnName, index = None, columnType = Some(ColumnType.numeric))
      val outputMetadata = dataFrame.inferredMetadata.get.appendColumn(newColumn)
      val dKnowledge = DKnowledge(DataFrameBuilder.buildDataFrameForInference(outputMetadata))
      (dKnowledge, InferenceWarnings.empty)
    }
  }
}
