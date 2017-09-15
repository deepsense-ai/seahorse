/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import org.apache.spark.sql.types.{StructField, StructType}

import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.doperables.dataframe.types.SparkConversions
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoricalMapper
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoricalMapper.CategoricalMappingsMap
import io.deepsense.deeplang.parameters.ColumnType.ColumnType
import spray.json._
import DataFrameMetadataJsonProtocol._

/**
 * Metadata of DataFrame.
 * Can represent partial or missing information.
 * @param isExact Indicates if all information inside metadata is exact. It is true if and only if
 *                  1. [[isColumnCountExact]] is true
 *                  2. all [[columns]] fields are set to Some
 *                  3. for all categorical columns names,
 *                     there exists mapping in [[categoricalMappings]]
 * @param isColumnCountExact Indicates if size of [[columns]] is exact. If not, there is possibility
 *                           that actual DataFrame will have more columns than [[columns]].
 * @param columns It contains information about columns.
 * @param categoricalMappings Map from column name to information about categories in this column.
 *                            If some name is present as key in this map, it means that information
 *                            about categories in column of this name are exact.
 *                            If some column is categorical and it's name is not present in map,
 *                            that means we don't know anything about categories in this column.
 *                            There is no possibility to store partial knowledge
 *                            about categories in one column.
 */
case class DataFrameMetadata(
    isExact: Boolean,
    isColumnCountExact: Boolean,
    columns: Map[String, ColumnMetadata],
    categoricalMappings: CategoricalMappingsMap)
  extends DOperable.AbstractMetadata {

  /**
   * @return Spark schema basing on information that it holds.
   *         Assumes that it contains full (not partial) information.
   */
  def toSchema: StructType = {
    val schema = StructType(columns.values.toList.map(_.toStructField))
    val categorizedSchema = CategoricalMapper.categorizedSchema(schema, categoricalMappings)
    categorizedSchema
  }

  override protected def _serializeToJson = this.toJson
}

object DataFrameMetadata {
  def fromSchema(schema: StructType): DataFrameMetadata = {
    DataFrameMetadata(
      isExact = true,
      isColumnCountExact = true,
      columns = schema.zipWithIndex.map({ case (structField, index) =>
        val rawResult = ColumnMetadata.fromStructField(structField, index)
        rawResult.name -> rawResult
      }).toMap,
      categoricalMappings = CategoricalMapper.mappingsMapFromSchema(schema)
    )
  }
}

case class ColumnMetadata(name: String, index: Option[Int], columnType: Option[ColumnType]) {
  private[dataframe] def toStructField: StructField = StructField(
    name = name,
    dataType = SparkConversions.columnTypeToSparkColumnType(columnType.get)
  )
}

object ColumnMetadata {
  private[dataframe] def fromStructField(structField: StructField, index: Int): ColumnMetadata =
    ColumnMetadata(
      name = structField.name,
      index = Some(index),
      columnType = Some(SparkConversions.sparkColumnTypeToColumnType(structField.dataType))
    )
}
