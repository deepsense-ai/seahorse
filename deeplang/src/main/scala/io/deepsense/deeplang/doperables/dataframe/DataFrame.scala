/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang.doperables.dataframe

import org.apache.spark.mllib.linalg.{Vector => SparkVector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructField, StructType}

import io.deepsense.deeplang.doperables.dataframe.types.SparkConversions
import io.deepsense.deeplang.doperables.{ColumnTypesPredicates, Report}
import io.deepsense.deeplang.doperations.exceptions.WrongColumnTypeException
import io.deepsense.deeplang.parameters.ColumnType.ColumnType
import io.deepsense.deeplang.{DOperable, ExecutionContext}

/**
 * @param sparkDataFrame Spark representation of data.
 *                       User of this class has to assure that
 *                       sparkDataFrame data fulfills its internal schema.
 * @param inferredMetadata Used only if this instance is used for inference.
 *                         Contains metadata inferred so far for this instance.
 */
case class DataFrame private[dataframe] (
    sparkDataFrame: sql.DataFrame,
    override val inferredMetadata: Option[DataFrameMetadata] = Some(DataFrameMetadata.empty))
  extends DOperable
  with DataFrameReportGenerator
  with DataFrameColumnsGetter {

  type M = DataFrameMetadata

  def this() = this(null)

  override def metadata: Option[DataFrameMetadata] =
    Option(DataFrameMetadata.fromSchema(sparkDataFrame.schema))

  override def save(context: ExecutionContext)(path: String): Unit =
    sparkDataFrame.write.parquet(path)

  override def toInferrable: DOperable = DataFrame(null, metadata)

  /**
   * Creates new DataFrame with new columns added.
   */
  def withColumns(context: ExecutionContext, newColumns: Traversable[sql.Column]): DataFrame = {
    val columns: List[sql.Column] = new sql.ColumnName("*") :: newColumns.toList
    val newSparkDataFrame = sparkDataFrame.select(columns: _*)
    context.dataFrameBuilder.buildDataFrame(newSparkDataFrame)
  }

  /**
   * Returns an RDD of Double
   * after checking the conformance of selected column type with provided predicate.
   *
   * throws [[WrongColumnTypeException]]
   * throws [[io.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException]]
   */
  def selectDoubleRDD(column: String, predicate: ColumnTypesPredicates.Predicate): RDD[Double] = {
    DataFrameColumnsGetter.assertColumnNamesValid(sparkDataFrame.schema, Seq(column))

    val selected = sparkDataFrame.select(column)
    predicate(selected.schema.fields.head).get

    selected.map(rowToDoubles(_).head)
  }

  /**
   * Returns an RDD of SparkVector
   * after checking the conformance of selected column types with provided predicate.
   *
   * throws [[WrongColumnTypeException]]
   * throws [[io.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException]]
   */
  def selectSparkVectorRDD(
      columns: Seq[String], predicate: ColumnTypesPredicates.Predicate): RDD[SparkVector] = {
    DataFrameColumnsGetter.assertColumnNamesValid(sparkDataFrame.schema, columns)

    val selected = sparkDataFrame.select(columns.head, columns.tail: _*)
    selected.schema.fields.foreach(predicate(_).get)

    selected.map { row => Vectors.dense(rowToDoubles(row).toArray) }
  }

  /**
   * Returns an RDD of LabeledPoint(label, features: _*)
   * after checking the conformance of selected column types with provided predicates.
   *
   * throws [[WrongColumnTypeException]]
   * throws [[io.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException]]
   */
  def selectAsSparkLabeledPointRDD(
      labelColumn: String,
      featureColumns: Seq[String],
      labelPredicate: ColumnTypesPredicates.Predicate,
      featurePredicate: ColumnTypesPredicates.Predicate): RDD[LabeledPoint] = {

    val selectedLabel = selectDoubleRDD(labelColumn, labelPredicate)
    val selectedFeatures = selectSparkVectorRDD(featureColumns, featurePredicate)

    selectedLabel zip selectedFeatures map {
      case (label, features) => LabeledPoint(label, features)
    }
  }

  /**
   * Returns an RDD of (prediction, label) pairs
   * after checking the conformance of selected column types with provided predicates.
   *
   * throws [[WrongColumnTypeException]]
   * throws [[io.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException]]
   */
  def selectPredictionsAndLabelsRDD(
      labelColumn: String,
      predictionColumn: String,
      labelPredicate: ColumnTypesPredicates.Predicate,
      predictionPredicate: ColumnTypesPredicates.Predicate): RDD[(Double, Double)] = {

    val selectedPrediction = selectDoubleRDD(predictionColumn, predictionPredicate)
    val selectedLabel = selectDoubleRDD(labelColumn, labelPredicate)
    selectedPrediction zip selectedLabel
  }

  override def report(executionContext: ExecutionContext): Report = {
    report(executionContext, sparkDataFrame)
  }

  private def rowToDoubles(row: Row): Seq[Double] = {
    row.toSeq.map {
      case null => 0.0
      case b: Boolean => if (b) 1.0 else 0.0
      case d: Double => d
      case i: Int => i.toDouble
      case _ => ???
    }
  }
}

object DataFrame {

  /**
   * Throws [[WrongColumnTypeException]]
   * if some columns of schema have type different than one of expected.
   */
  def assertExpectedColumnType(schema: StructType, expectedTypes: ColumnType*): Unit = {
    for (field <- schema.fields) {
      assertExpectedColumnType(field, expectedTypes: _*)
    }
  }

  /**
   * Throws [[WrongColumnTypeException]] if column has type different than one of expected.
   */
  def assertExpectedColumnType(column: StructField, expectedTypes: ColumnType*): Unit = {
    val actualType = SparkConversions.sparkColumnTypeToColumnType(column.dataType)
    if (!expectedTypes.contains(actualType)) {
      throw WrongColumnTypeException(column.name, actualType, expectedTypes: _*)
    }
  }

  /**
   * Generates a DataFrame with no columns.
   */
  def empty(context: ExecutionContext): DataFrame = {
    val emptyRdd = context.sqlContext.sparkContext.parallelize(Seq[Row]())
    val emptySparkDataFrame = context.sqlContext.createDataFrame(emptyRdd, StructType(Seq.empty))
    context.dataFrameBuilder.buildDataFrame(emptySparkDataFrame)
  }

  def loadFromFs(context: ExecutionContext)(path: String): DataFrame = {
    val dataFrame = context.sqlContext.read.parquet(path)
    context.dataFrameBuilder.buildDataFrame(dataFrame)
  }
}
