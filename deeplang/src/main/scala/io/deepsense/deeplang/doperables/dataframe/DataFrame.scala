/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import scala.annotation.tailrec

import org.apache.spark.mllib.linalg.{Vector => SparkVector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructField, StructType}

import io.deepsense.deeplang.doperables.Report
import io.deepsense.deeplang.doperations.exceptions.{ColumnDoesNotExistException, ColumnsDoNotExistException, WrongColumnTypeException}
import io.deepsense.deeplang.parameters.ColumnType.ColumnType
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperable, ExecutionContext}

/*
* @param sparkDataFrame spark representation of data.
*                       User of this class has to assure that
*                       sparkDataFrame data fulfills its internal schema.
*/
case class DataFrame(sparkDataFrame: sql.DataFrame)
  extends DOperable
  with DataFrameReportGenerator
  with DataFrameColumnsGetter {

  def this() = this(null)

  override def save(context: ExecutionContext)(path: String): Unit =
    sparkDataFrame.write.parquet(path)

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

  override def report: Report = {
    report(sparkDataFrame)
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
    val actualType = DataFrameColumnsGetter.sparkColumnTypeToColumnType(column.dataType)
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

  def loadFromHdfs(context: ExecutionContext)(path: String): DataFrame = {
    val dataFrame = context.sqlContext.read.parquet(path)
    context.dataFrameBuilder.buildDataFrame(dataFrame)
  }
}
