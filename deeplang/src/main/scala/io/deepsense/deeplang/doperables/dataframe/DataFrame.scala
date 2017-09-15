/**
 * Copyright (c) 2015, CodiLime, Inc.
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
* @param optionalSparkDataFrame spark representation of data.
*                               Client of this class has to assure that
*                               sparkDataFrame data fulfills its internal schema.
*/
case class DataFrame(optionalSparkDataFrame: Option[sql.DataFrame])
  extends DOperable
  with DataFrameReportGenerator {

  def this() = this(None)

  override def save(context: ExecutionContext)(path: String): Unit =
    sparkDataFrame.saveAsParquetFile(path)

  lazy val sparkDataFrame: sql.DataFrame = optionalSparkDataFrame.get

  /**
   * Returns name of column basing on selection.
   * Throws [[ColumnDoesNotExistException]] if out-of-range index
   * or non-existing column name is selected.
   */
  def getColumnName(singleColumnSelection: SingleColumnSelection): String =
    tryGetColumnName(singleColumnSelection) match {
      case Some(name) => name
      case None => throw ColumnDoesNotExistException(singleColumnSelection, this)
    }

  private def tryGetColumnName(singleColumnSelection: SingleColumnSelection): Option[String] =
    singleColumnSelection match {
      case NameSingleColumnSelection(name) =>
        if (sparkDataFrame.schema.fieldNames.contains(name)) Some(name) else None
      case IndexSingleColumnSelection(index) =>
        if (index >= 0 && index < sparkDataFrame.schema.length) {
          Some(sparkDataFrame.schema.fieldNames(index))
        } else {
          None
        }
    }

  /**
   * Names of columns selected by provided selections.
   * Order of returned columns is the same as in schema.
   * If a column will occur in many selections, it won't be duplicated in result.
   * Throws [[ColumnsDoNotExistException]] if out-of-range indexes
   * or non-existing column names are selected.
   */
  def getColumnNames(multipleColumnSelection: MultipleColumnSelection): Seq[String] = {
    DataFrame.getColumnNames(sparkDataFrame.schema, multipleColumnSelection)
  }

  /**
   * Column type by column name.
   */
  def columnType(columnName: String): ColumnType =
    DataFrame.sparkColumnTypeToColumnType(sparkDataFrame.schema(columnName).dataType)

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
   * Throws [[ColumnsDoNotExistException]] if non-existing column names are selected.
   * Throws [[WrongColumnTypeException]] if not numeric columns names are selected.
   * @param columns List of columns' names to use as vector fields.
   */
  def toSparkVectorRDD(columns: Seq[String]): RDD[SparkVector] = {
    DataFrame.assertColumnNamesValid(sparkDataFrame.schema, columns)
    val sparkDataFrameWithSelectedColumns = sparkDataFrame.select(columns.head, columns.tail:_*)
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
    DataFrame.assertColumnNamesValid(sparkDataFrame.schema, columns)
    val sparkDataFrameWithSelectedColumns = sparkDataFrame.select(labelColumn, columns:_*)
    DataFrame.assertExpectedColumnType(sparkDataFrameWithSelectedColumns.schema, ColumnType.numeric)
    sparkDataFrameWithSelectedColumns.map(row => {
      val doubles = row.toSeq.asInstanceOf[Seq[Double]]
      LabeledPoint(doubles.head, Vectors.dense(doubles.tail.toArray))
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

  override def report: Report = {
    report(sparkDataFrame)
  }
}

object DataFrame {
  /**
   * Creates column name by adding some suffix to base column name. Appends integer if it is
   * not equal to 0. Parts of name are separated by underscore.
   * E. g. createColumnName('xyz', 'a', 3) returns 'xyz_a_3',
   * and createColumnName('xyz', 'a', 0) returns 'xyz_a'.
   */
  private [deeplang] def createColumnName(
    baseColumnName: String,
    addedPart: String,
    level: Int): String = {
    val levelSuffix = if (level > 0) "_" + level else ""
    (baseColumnName + "_" + addedPart + levelSuffix).replace(".", "_")
    // TODO: 'replace' should be removed after spark upgrade to 1.4 version. DS-635
  }

  /**
   * Names of columns selected by provided selections.
   * Order of returned columns is the same as in schema.
   * If a column will occur in many selections, it won't be duplicated in result.
   * Throws [[ColumnsDoNotExistException]] if out-of-range indexes
   * or non-existing column names are selected.
   */
  // TODO better support for categorical type
  def getColumnNames(
      schema: StructType,
      multipleColumnSelection: MultipleColumnSelection): Seq[String] = {

    assertColumnSelectionsValid(schema, multipleColumnSelection)
    val selectedColumns = for {
      (column, index) <- schema.fields.zipWithIndex
      columnName = column.name
      columnType = DataFrame.sparkColumnTypeToColumnType(column.dataType)
      selection <- multipleColumnSelection.selections
      if DataFrame.isFieldSelected(columnName, index, columnType, selection)
    } yield columnName
    selectedColumns.distinct
  }

  private def assertColumnSelectionsValid(
      schema: StructType,
      multipleColumnSelection: MultipleColumnSelection): Unit = {

    val selections = multipleColumnSelection.selections
    for (selection <- selections) {
      if (!isSelectionValid(schema, selection)) {
        throw ColumnsDoNotExistException(selections, schema)
      }
    }
  }

  private def assertColumnNamesValid(schema: StructType, columns: Seq[String]): Unit = {
    assertColumnSelectionsValid(
      schema, MultipleColumnSelection(Vector(NameColumnSelection(columns.toSet))))
  }

  /**
   * Checks if given selection is valid with regard to dataframe schema.
   * Returns false if some specified names or indexes are incorrect.
   */
  private def isSelectionValid(
      schema: StructType,
      selection: ColumnSelection): Boolean = selection match {

    case IndexColumnSelection(indexes) =>
      val length = schema.length
      val indexesOutOfBounds = indexes.filter(index => index < 0 || index >= length)
      indexesOutOfBounds.isEmpty
    case NameColumnSelection(names) =>
      val allNames = schema.fieldNames.toSet
      val nonExistingNames = names.filter(!allNames.contains(_))
      nonExistingNames.isEmpty
    case TypeColumnSelection(_) => true
  }

  /**
   * Tells if column is selected by given selection.
   * Out-of-range indexes and non-existing column names are ignored.
   * @param columnName Name of field.
   * @param columnIndex Index of field in schema.
   * @param columnType Type of field's column.
   * @param selection Selection of columns.
   * @return True iff column meets selection's criteria.
   */
  private [DataFrame] def isFieldSelected(
      columnName: String,
      columnIndex: Int,
      columnType: ColumnType,
      selection: ColumnSelection): Boolean = selection match {
    case IndexColumnSelection(indexes) => indexes.contains(columnIndex)
    case NameColumnSelection(names) => names.contains(columnName)
    case TypeColumnSelection(types) => types.contains(columnType)
  }

  /**
   * Converts spark's column type used in schemas to column type.
   */
  def sparkColumnTypeToColumnType(sparkColumnType: sql.types.DataType): ColumnType =
    sparkColumnType match {
      case sql.types.DoubleType => ColumnType.numeric
      case sql.types.StringType => ColumnType.string
      case sql.types.BooleanType => ColumnType.boolean
      case sql.types.TimestampType => ColumnType.timestamp
      case sql.types.LongType => ColumnType.ordinal
      case sql.types.IntegerType => ColumnType.categorical
    }

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
    val actualType = DataFrame.sparkColumnTypeToColumnType(column.dataType)
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
    val dataFrame = context.sqlContext.parquetFile(path)
    context.dataFrameBuilder.buildDataFrame(dataFrame)
  }

  val dataSampleTableName = "Data Sample"
  val maxRowsNumberInReport = 100
  val maxColumnsNumberInReport = 100
}
