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

/**
 * Metadata of DataFrame.
 * Can represent partial or missing information.
 * @param isExact Indicates if all information inside metadata is exact. It is true if and only if
 *                  1. [[isColumnCountExact]] is true
 *                  2. all [[columns]] fields are set to Some
 *                  3. for all categorical columns indices,
 *                     there exists mapping in [[categoricalMappings]]
 * @param isColumnCountExact Indicates if size of [[columns]] is exact. If not, there is possibility
 *                           that actual DataFrame will have more columns than [[columns]].
 * @param columns It contains information about columns. Indices are exact - at index i we have
 *                information about i-th DataFrame's column.
 * @param categoricalMappings Map from column index to information about categories in this column.
 *                            If some index is present as key in this map, it means that information
 *                            about categories in column at this index are exact.
 *                            If some column is categorical and it's index is not present in map,
 *                            that means we don't know anything about categories in this column.
 *                            There is no possibility to store partial knowledge
 *                            about categories in one column.
 */
case class DataFrameMetadata(
    isExact: Boolean,
    isColumnCountExact: Boolean,
    columns: Seq[ColumnMetadata],
    categoricalMappings: CategoricalMappingsMap)
  extends DOperable.AbstractMetadata {

  /**
   * @return Spark schema basing on information that it holds.
   *         Assumes that it contains full (not partial) information.
   */
  def toSchema: StructType = {
    val schema = StructType(columns.map(_.toStructField))
    val categorizedSchema = CategoricalMapper.categorizedSchema(schema, categoricalMappings)
    categorizedSchema
  }
}

object DataFrameMetadata {
  def fromSchema(schema: StructType): DataFrameMetadata = {
    DataFrameMetadata(
      isExact = true,
      isColumnCountExact = true,
      columns = schema.map(ColumnMetadata.fromStructField),
      categoricalMappings = CategoricalMapper.mappingsMapFromSchema(schema)
    )
  }
}

case class ColumnMetadata(name: Option[String], columnType: Option[ColumnType]) {
  private[dataframe] def toStructField: StructField = StructField(
    name = name.get,
    dataType = SparkConversions.columnTypeToSparkColumnType(columnType.get)
  )
}

object ColumnMetadata {
  private[dataframe] def fromStructField(structField: StructField): ColumnMetadata =
    ColumnMetadata(
      name = Some(structField.name),
      columnType = Some(SparkConversions.sparkColumnTypeToColumnType(structField.dataType))
    )
}
