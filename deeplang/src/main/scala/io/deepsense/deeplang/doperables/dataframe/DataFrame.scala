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

package io.deepsense.deeplang.doperables.dataframe

import org.apache.spark.mllib.linalg.{Vector => SparkVector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructField, StructType}

import io.deepsense.deeplang.doperables.Report
import io.deepsense.deeplang.doperables.dataframe.types.SparkConversions
import io.deepsense.deeplang.doperations.exceptions.{ColumnsDoNotExistException, WrongColumnTypeException}
import io.deepsense.deeplang.parameters.ColumnType.ColumnType
import io.deepsense.deeplang.parameters._
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
   * Converts DataFrame to RDD of spark's vectors using selected columns.
   * Throws [[ColumnsDoNotExistException]] if non-existing column names are selected.
   * Throws [[WrongColumnTypeException]] if not numeric columns names are selected.
   * @param columns List of columns' names to use as vector fields.
   */
  def toSparkVectorRDD(columns: Seq[String]): RDD[SparkVector] = {
    DataFrameColumnsGetter.assertColumnNamesValid(sparkDataFrame.schema, columns)
    val sparkDataFrameWithSelectedColumns = sparkDataFrame.select(columns.head, columns.tail: _*)
    DataFrame.assertExpectedColumnType(sparkDataFrameWithSelectedColumns.schema, ColumnType.numeric)
    sparkDataFrameWithSelectedColumns.map(row =>
      Vectors.dense(row.toSeq.asInstanceOf[Seq[Double]].toArray))
  }

  /**
   * Converts DataFrame to RDD of spark's LabeledPoints using selected columns.
   * Throws [[WrongColumnTypeException]] if not numeric columns names are selected.
   * @param columns List of columns' names to use as features.
   * @param labelColumn Column name to use as label.
   */
  def toSparkLabeledPointRDD(
      columns: Seq[String], labelColumn: String): RDD[LabeledPoint] = {
    DataFrameColumnsGetter.assertColumnNamesValid(sparkDataFrame.schema, columns)
    val sparkDataFrameWithSelectedColumns = sparkDataFrame.select(labelColumn, columns: _*)
    DataFrame.assertExpectedColumnType(sparkDataFrameWithSelectedColumns.schema, ColumnType.numeric)
    sparkDataFrameWithSelectedColumns.map(row => {
      val doubles = row.toSeq.asInstanceOf[Seq[Double]]
      LabeledPoint(doubles.head, Vectors.dense(doubles.tail.toArray))
    })
  }

  override def report(executionContext: ExecutionContext): Report = {
    report(executionContext, sparkDataFrame)
  }
}

object DataFrame {

  /**
   * Throws [[WrongColumnTypeException]]
   * if some columns of schema have different type than expected.
   */
  def assertExpectedColumnType(schema: StructType, expectedType: ColumnType): Unit = {
    for (field <- schema.fields) {
      assertExpectedColumnType(field, expectedType)
    }
  }

  /**
   * Throws [[WrongColumnTypeException]] if column has different type than expected.
   */
  def assertExpectedColumnType(column: StructField, expectedType: ColumnType): Unit = {
    val actualType = SparkConversions.sparkColumnTypeToColumnType(column.dataType)
    if (actualType != expectedType) {
      throw WrongColumnTypeException(column.name, actualType, expectedType)
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
