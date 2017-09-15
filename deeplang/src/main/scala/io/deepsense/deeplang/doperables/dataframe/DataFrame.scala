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

import org.apache.spark.sql
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructField, StructType}

import io.deepsense.commons.types.ColumnType.ColumnType
import io.deepsense.commons.types.SparkConversions
import io.deepsense.deeplang.doperables.dataframe.report.DataFrameReportGenerator
import io.deepsense.deeplang.doperables.descriptions.DataFrameInferenceResult
import io.deepsense.deeplang.doperables.report.Report
import io.deepsense.deeplang.doperations.exceptions.{DuplicatedColumnsException, WrongColumnTypeException}
import io.deepsense.deeplang.{DOperable, ExecutionContext}

/**
 * @param sparkDataFrame Spark representation of data.
 *                       User of this class has to assure that
 *                       sparkDataFrame data fulfills its internal schema.
 * @param schema Schema of the DataFrame. Usually it is schema of sparkDataFrame,
 *               but for inference, DataFrame may be not set but schema is known.
 */
case class DataFrame private[dataframe] (
    sparkDataFrame: sql.DataFrame,
    schema: Option[StructType])
  extends DOperable
  with DataFrameColumnsGetter {

  def this() = this(null, None)

  schema.foreach(
    struct => {
      val duplicatedColumnNames = struct.fieldNames.groupBy(identity).collect {
        case (col, list) if list.length > 1 => col
      }
      if (duplicatedColumnNames.nonEmpty) {
        throw DuplicatedColumnsException(duplicatedColumnNames.toList)
      }
    }
  )

  /**
   * Creates new DataFrame with new columns added.
   */
  def withColumns(context: ExecutionContext, newColumns: Traversable[sql.Column]): DataFrame = {
    val columns: List[sql.Column] = new sql.ColumnName("*") :: newColumns.toList
    val newSparkDataFrame = sparkDataFrame.select(columns: _*)
    DataFrame.fromSparkDataFrame(newSparkDataFrame)
  }

  override def report: Report = {
    DataFrameReportGenerator.report(sparkDataFrame)
  }

  override def inferenceResult: Option[DataFrameInferenceResult] =
    schema.map(DataFrameInferenceResult)
}

object DataFrame {

  def apply(sparkDataFrame: sql.DataFrame, schema: StructType): DataFrame =
    DataFrame(sparkDataFrame, Some(schema))

  /**
   * @return DataFrame object that can be used _only_ for inference,
   *         i.e. it contains only schema of this DataFrame.
   */
  def forInference(schema: StructType): DataFrame = forInference(Some(schema))

  /**
   * @return DataFrame object that can be used _only_ for inference,
   *         i.e. it contains only schema of this DataFrame.
   */
  def forInference(schema: Option[StructType] = None): DataFrame = DataFrame(null, schema)

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
    fromSparkDataFrame(emptySparkDataFrame)
  }

  def loadFromFs(context: ExecutionContext)(path: String): DataFrame = {
    val dataFrame = context.sqlContext.read.parquet(path)
    fromSparkDataFrame(dataFrame)
  }

  def fromSparkDataFrame(sparkDataFrame: sql.DataFrame): DataFrame = {
    DataFrame(sparkDataFrame, Some(sparkDataFrame.schema))
  }
}
