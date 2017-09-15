/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import org.apache.spark.sql.types.{StructField, StructType}
import spray.json._

import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.doperables.dataframe.DataFrameMetadataJsonProtocol._
import io.deepsense.deeplang.doperables.dataframe.types.SparkConversions
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoriesMapping, MappingMetadataConverter}
import io.deepsense.deeplang.parameters.ColumnType
import io.deepsense.deeplang.parameters.ColumnType.ColumnType

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
   *         Assumes that it contains full (not partial) information.
   */
  def toSchema: StructType = StructType(columns.values.toList.map(_.toStructField))

  override protected def _serializeToJson = this.toJson
}

object DataFrameMetadata {
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
   * TODO
   */
  def columnType: Option[ColumnType]

  /**
   * Assumes that all information
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
