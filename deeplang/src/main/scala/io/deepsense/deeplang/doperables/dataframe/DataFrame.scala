/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.doperables.dataframe

import scala.annotation.tailrec

import org.apache.spark.mllib.linalg.{Vector => SparkVector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql

import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperable, ExecutionContext}

/*
* @param optionalSparkDataFrame spark representation of data.
*                               Client of this class has to assure that
*                               sparkDataFrame data fulfills its internal schema.
*/
case class DataFrame(optionalSparkDataFrame: Option[sql.DataFrame]) extends DOperable {

  def this() = this(None)

  def save(path: String): Unit = sparkDataFrame.saveAsParquetFile(path)

  lazy val sparkDataFrame: sql.DataFrame = optionalSparkDataFrame.get

  def getColumnName(singleColumnSelection: SingleColumnSelection): String =
    singleColumnSelection match {
      case NameSingleColumnSelection(value) => value
      case IndexSingleColumnSelection(index) => getColumnNameByIndex(index)
    }

  def getColumnNames(multipleColumnSelection: MultipleColumnSelection): Set[String] = {
    val sets = multipleColumnSelection.selections.map({
      case IndexColumnSelection(indexes) => indexes.map(getColumnNameByIndex)
      case NameColumnSelection(names) => names
      case TypeColumnSelection(types) => ???
    })
    sets.foldLeft(Set.empty[String])(_++_)
  }

  private def getColumnNameByIndex(index: Int): String = sparkDataFrame.schema(index).name

  /**
   * Creates new DataFrame with new columns added.
   */
  def withColumns(context: ExecutionContext, newColumns: Traversable[sql.Column]): DataFrame = {
    val columns: List[sql.Column] = new sql.ColumnName("*") :: newColumns.toList
    val newSparkDataFrame = sparkDataFrame.select(columns:_*)
    context.dataFrameBuilder.buildDataFrame(newSparkDataFrame)
  }

  /**
   * Converts DataFrame to RDD of spark's vectors using selected columns.
   * Assumes that columns have DoubleType.
   * @param columns List of columns' names to use as vector fields.
   */
  def toSparkVectorRDD(columns: Seq[String]): RDD[SparkVector] = {
    sparkDataFrame.select(columns.head, columns.tail:_*).map(row =>
      Vectors.dense(row.toSeq.asInstanceOf[Seq[Double]].toArray))
  }

  /**
   * Converts DataFrame to RDD of spark's LabeledPoints using selected columns.
   * Assumes all that columns have DoubleType.
   * @param columns List of columns' names to use as features.
   * @param labelColumn Column name to use as label.
   */
  def toSparkLabeledPointRDD(
      columns: List[String], labelColumn: String): RDD[LabeledPoint] = {

    sparkDataFrame.select(labelColumn, columns:_*).map(row => {
      val head :: tail = row.toSeq.asInstanceOf[Seq[Double]]
      LabeledPoint(head, Vectors.dense(tail.toArray))
    })
  }

  /**
   * Method useful for generating names for new columns. When we want to add new columns
   * to a dataframe, we need to generate a new name for them assuring that this name is not already
   * used in the dataframe. Common use case is when we generate new columns' names basing on
   * existing column name by adding some sort of extension. When new name generated using base
   * column name and extension is already used, we want to add level number.
   * E. g. assume that we have column named 'xyz' and we want to add two new columns, with
   * suffixes '_a' and '_b', so 'xyz_a' and 'xyz_b'. But if there already is column 'xyz_a' in
   * dataframe, we want new columns to be named 'xyz_a_1' and 'xyz_b_1'.
   * This method allows to compute lowest unoccupied level provided original column name
   * ('xyz' from example) and extensions ({'a', 'b'} from example).
   * @param originalColumnName Name of the column which is a base for a new column.
   * @param newColumnNameExtensions Extensions of base name for generating new columns.
   * @return Lowest unoccupied level that can be used to generate a new name for columns.
   */
  def getFirstFreeNamesLevel(
      originalColumnName: String,
      newColumnNameExtensions: Set[String]): Int = {

    val existingColumnsNames = sparkDataFrame.schema.fieldNames.toSet

    @tailrec
    def getFirstFreeNamesLevel(level: Int): Int = {
      val levelTaken: Boolean = newColumnNameExtensions.exists(
        ext => existingColumnsNames.contains(DataFrame.createColumnName(
          originalColumnName, ext, level))
      )
      if (levelTaken) getFirstFreeNamesLevel(level + 1) else level
    }

    getFirstFreeNamesLevel(0)
  }

  /**
   * Generates unique column name created of some other column's name, suffix and optionally
   * integer appended.
   * E. g. uniqueColumnName('xyz', 'a') returns 'xyz_a' if there is no such name in dataframe yet,
   * and it returns 'xyz_a_2' if there already are columns 'xyz_a' and 'xyz_a_1' in dataframe.
   */
  def uniqueColumnName(originalColumnName: String, columnNameSuffix: String): String = {
    val level = getFirstFreeNamesLevel(originalColumnName, Set(columnNameSuffix))
    DataFrame.createColumnName(originalColumnName, columnNameSuffix, level)
  }
}

object DataFrame {
  /**
   * Creates column name by adding some suffix to base column name. Appends integer if it is
   * not equal to 0. Parts of name are separated by underscore.
   * E. g. createColumnName('xyz', 'a', 3) returns 'xyz_a_3',
   * and createColumnName('xyz', 'a', 0) returns 'xyz_a'.
   */
  def createColumnName(
    baseColumnName: String,
    addedPart: String,
    level: Int): String = {
    val levelSuffix = if (level > 0) "_" + level else ""
    baseColumnName + "_" + addedPart + levelSuffix
  }
}
