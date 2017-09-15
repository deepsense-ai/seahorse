/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import io.deepsense.deeplang.doperations.exceptions.{ColumnDoesNotExistException, ColumnsDoNotExistException}
import io.deepsense.deeplang.inference.{MultipleColumnsMayNotExistWarning, SingleColumnMayNotExistWarning}
import io.deepsense.deeplang.inference.{InferenceWarning, InferenceWarnings}
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrameMetadataJsonProtocol._
import io.deepsense.deeplang.doperables.dataframe.types.SparkConversions
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoriesMapping, MappingMetadataConverter}
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.parameters.ColumnType.ColumnType

import scala.collection.mutable.ListBuffer
import spray.json._
import org.apache.spark.sql.types.{StructField, StructType}

/**
 * Metadata of DataFrame.
 * Can represent partial or missing information.
 * @param isExact Indicates if all information inside metadata is exact. It is true if and only if
 *                  1. [[isColumnCountExact]] is true
 *                  2. all [[columns]] fields are set to Some
 * @param isColumnCountExact Indicates if size of [[columns]] is exact. If not, there is possibility
 *                           that actual DataFrame will have more columns than [[columns]].
 * @param columns It contains information about columns.
 */
case class DataFrameMetadata(
    isExact: Boolean,
    isColumnCountExact: Boolean,
    columns: Map[String, ColumnMetadata])
  extends DOperable.AbstractMetadata {

  /**
   * @return Spark schema basing on information that it holds.
   * @throws IllegalStateException when called on DataFrameMetadata with isExact set to false
   */
  def toSchema: StructType = {
    if (!isExact) {
      throw new IllegalStateException(
        "Cannot call toSchema on DataFrameMetadata with isExact field set to false")
    }
    val sortedColumns = columns.values.toList.sortBy(_.index.get)
    val structFields = sortedColumns.map(_.toStructField)
    StructType(structFields)
  }

  /**
   * Selects column from metadata basing on provided SingleColumnSelection.
   * If isExact field is true and provided selector points to
   * column that is not in current metadata, ColumnDoesNotExistException will be thrown.
   * If isExact field is false and provided selector points to
   * column that is not in current metadata, InferenceWarnings will contain appropriate warning
   * and no exception will be thrown.
   */
  @throws[ColumnDoesNotExistException]
  def select(
      columnSelection: SingleColumnSelection):
      (Option[ColumnMetadata], InferenceWarnings) = {
    val metadataOption = getMetadataOption(columnSelection)
    if (metadataOption.isEmpty) {
      if (isExact) {
        throw ColumnDoesNotExistException(columnSelection, this)
      }
      (None, InferenceWarnings(SingleColumnMayNotExistWarning(columnSelection, this)))
    } else {
      (metadataOption, InferenceWarnings.empty)
    }
  }

  /**
   * Selects columns from metadata basing on provided MultipleColumnSelection.
   * If isExact field is true and provided selector points to
   * columns that are not in current metadata, ColumnsDoNotExistException will be thrown.
   * If isExact field is false and provided selector points to
   * columns that are not in current metadata, InferenceWarnings will contain appropriate warning
   * and no exception will be thrown.
   *
   * Returned Seq[ColumnMetadata] will be subset of current schema,
   * the order of columns will be preserved as in orderedColumns method.
   */
  @throws[ColumnsDoNotExistException]
  def select(
      multipleColumnSelection: MultipleColumnSelection):
      (Seq[ColumnMetadata], InferenceWarnings) = {
    val warnings = assertColumnSelectionsValid(multipleColumnSelection)
    val selectedColumns = for {
      column <- orderedColumns
      selection <- multipleColumnSelection.selections
      if isFieldSelected(column.name, column.index, column.columnType, selection)
    } yield column

    (selectedColumns.toSeq.distinct, InferenceWarnings(warnings.toVector))
  }

  /**
   * @return Columns ordered by index.
   *         Columns without index will be listed at the end of the sequence.
   */
  def orderedColumns: Seq[ColumnMetadata] = {
    val values = columns.values
    val columnsSortedByIndex = values.filter(_.index.isDefined).toList.sortBy(_.index.get)
    val columnsWithoutIndex = values.filter(_.index.isEmpty).toList
    return columnsSortedByIndex ++ columnsWithoutIndex
  }

  /**
   * @return Some[ColumnMetadata] if columnSelection selects column, None otherwise
   */
  private def getMetadataOption(columnSelection: SingleColumnSelection): Option[ColumnMetadata] = {
    columnSelection match {
      case nameSelection: NameSingleColumnSelection =>
        columns.get(nameSelection.value)
      case indexSelection: IndexSingleColumnSelection =>
        columns.values.find(_.index.exists(index => index == indexSelection.value))
    }
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
  private def isFieldSelected(
      columnName: String,
      columnIndex: Option[Int],
      columnType: Option[ColumnType],
      selection: ColumnSelection): Boolean = selection match {
    case IndexColumnSelection(indexes) => columnIndex.exists(indexes.contains(_))
    case NameColumnSelection(names) => names.contains(columnName)
    case TypeColumnSelection(types) => columnType.exists(types.contains(_))
    case IndexRangeColumnSelection(Some(lowerBound), Some(upperBound)) =>
      columnIndex.exists(index => (index >= lowerBound && index <= upperBound))
    case IndexRangeColumnSelection(_, _) => false
  }

  private def assertColumnSelectionsValid(
      multipleColumnSelection: MultipleColumnSelection): Seq[InferenceWarning] = {
    val selections = multipleColumnSelection.selections
    val warnings = new ListBuffer[InferenceWarning]()
    for (selection <- selections) {
      if (!isSelectionValid(selection)) {
        if (isExact) {
          throw ColumnsDoNotExistException(selections, toSchema)
        }
        warnings += MultipleColumnsMayNotExistWarning(selection, this)

      }
    }
    warnings
  }

  /**
   * Checks if given selection is valid with regard to dataframe schema.
   * Returns false if some specified names or indexes are incorrect.
   */
  private def isSelectionValid(selection: ColumnSelection): Boolean = selection match {
    case IndexColumnSelection(indexes) =>
      val metadataIndexes = indexesSet
      val indexesIntersection = metadataIndexes & indexes.toSet
      indexesIntersection.size == indexes.size
    case NameColumnSelection(names) =>
      val metadataNames = columns.keys.toSet
      val namesIntersection = names.toSet & metadataNames
      namesIntersection.size == names.size
    case TypeColumnSelection(_) => true
    case IndexRangeColumnSelection(Some(lowerBound), Some(upperBound)) =>
      val metadataIndexes = indexesSet()
      (lowerBound to upperBound).toSet.subsetOf(metadataIndexes)
    case IndexRangeColumnSelection(_, _) => true
  }

  private def indexesSet(): Set[Int] = {
    columns.values.map(_.index).flatten.toSet
  }

  override protected def _serializeToJson = this.toJson
}

object DataFrameMetadata {

  def empty: DataFrameMetadata = DataFrameMetadata(
    isExact = false, isColumnCountExact = false, columns = Map.empty)

  def fromSchema(schema: StructType): DataFrameMetadata = {
    DataFrameMetadata(
      isExact = true,
      isColumnCountExact = true,
      columns = schema.zipWithIndex.map({ case (structField, index) =>
        val rawResult = CommonColumnMetadata.fromStructField(structField, index)
        rawResult.name -> rawResult
      }).toMap
    )
  }

  def deserializeFromJson(jsValue: JsValue): DataFrameMetadata = {
    DOperable.AbstractMetadata.unwrap(jsValue).convertTo[DataFrameMetadata]
  }
}
/**
 * Represents knowledge about a column in DataFrame.
 */
sealed trait ColumnMetadata {
  val name: String

  /**
   * Index of this column in DataFrame. None denotes unknown index.
   */
  val index: Option[Int]

  /**
   * Type of this column. None denotes unknown type.
   */
  def columnType: Option[ColumnType]

  /**
   * Assumes that this metadata contains full information.
   */
  private[dataframe] def toStructField: StructField
}

case class CommonColumnMetadata(
    name: String,
    index: Option[Int],
    columnType: Option[ColumnType])
  extends ColumnMetadata {

  private[dataframe] def toStructField: StructField = StructField(
    name = name,
    dataType = SparkConversions.columnTypeToSparkColumnType(columnType.get)
  )
}

/**
 * Represents knowledge about categorical column.
 * @param categories Mapping of categories in this column.
 *                   If None, we don't know anything about categories in this column.
 *                   If Some, information about categories is exact.
 *                   There is no possibility to store partial knowledge about categories.
 */
case class CategoricalColumnMetadata(
    name: String,
    index: Option[Int],
    categories: Option[CategoriesMapping])
  extends ColumnMetadata {

  def columnType: Option[ColumnType] = Some(ColumnType.categorical)

  private[dataframe] def toStructField: StructField = StructField(
    name = name,
    dataType = SparkConversions.columnTypeToSparkColumnType(columnType.get),
    metadata = MappingMetadataConverter.mappingToMetadata(categories.get)
  )
}

object CommonColumnMetadata {

  private[dataframe] def fromStructField(structField: StructField, index: Int): ColumnMetadata = {
    val name = structField.name
    MappingMetadataConverter.mappingFromMetadata(structField.metadata) match {
      case Some(categoriesMapping) => CategoricalColumnMetadata(
        name, Some(index), Some(categoriesMapping))
      case None => CommonColumnMetadata(
        name = structField.name,
        index = Some(index),
        columnType = Some(SparkConversions.sparkColumnTypeToColumnType(structField.dataType)))
    }
  }
}
