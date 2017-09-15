/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe.types.categorical

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{IntegerType, StringType, StructType}
import org.joda.time.DateTime

import io.deepsense.deeplang.doperables.dataframe.types.{SparkConversions, Conversions}
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoricalMapper.CategoricalMappingsMap
import io.deepsense.deeplang.doperables.dataframe.types.categorical.MappingMetadataConverter._
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder, DataFrameColumnsGetter}
import io.deepsense.deeplang.parameters.ColumnType

case class CategoricalMapper(dataFrame: DataFrame, dataFrameBuilder: DataFrameBuilder) {
  private val sparkDataFrame = dataFrame.sparkDataFrame

  def categorized(columnsNames: String*): DataFrame = {
    val distinctValues = columnsNames.map(n => n -> distinctColumnValues(n)).toMap
    val mappings = distinctValues.map {
      case (columnName, values) =>
        columnIndex(columnName) -> CategoriesMapping(values.toSeq)
    }
    val remappedRdd = mapCategoricals(mappings)
    dataFrameWithMappingMetadata(remappedRdd, mappings)
  }

  private def distinctColumnValues(column: String): Array[String] = {
    val mappingFunction = findMappingFunction(column)
    val convertedColumn = column + "_converted" + DateTime.now.getMillis
    sparkDataFrame
      .select(column)
      .filter(sparkDataFrame(column).isNotNull)
      .distinct
      .withColumn(convertedColumn, mappingFunction(sparkDataFrame(column)))
      .select(convertedColumn)
      .collect()
      .map(_.getString(0))
  }

  private def findMappingFunction(column: String) = {
    val convertFrom = sparkDataFrame.schema(column)
    if (convertFrom.dataType == StringType) {
      udf[String, String](identity[String])
    } else {
      Conversions.UdfConverters(
        SparkConversions.sparkColumnTypeToColumnType(convertFrom.dataType),
        ColumnType.string
      )
    }
  }

  private def mapCategoricals(mappings: CategoricalMappingsMap): RDD[Row] = {
    sparkDataFrame.map(r => {
      val seq = Row.unapplySeq(r).get.zipWithIndex
      val mappedSeq = seq.map { case (value, index) =>
        mappings
          .get(index)
          .map { m =>
            if (value == null) {
              null
            } else {
              val convertedValue = Conversions.anyToString(value)
              m.valueToId(convertedValue)
            }
          }.getOrElse(value)
      }
      Row(mappedSeq: _*)
    })
  }

  private def columnIndex(name: String): Int =
    sparkDataFrame.schema.fieldNames.toIndexedSeq.indexOf(name)

  private def dataFrameWithMappingMetadata(
      rdd: RDD[Row],
      mappings: CategoricalMappingsMap): DataFrame = {
    val schema = sparkDataFrame.schema
    val updatedSchema = CategoricalMapper.categorizedSchema(schema, mappings)
    dataFrameBuilder.buildDataFrame(updatedSchema, rdd)
  }
}

object CategoricalMapper {

  /**
   * A map from column index to categorical mapping for this column
   */
  type CategoricalMappingsMap = Map[Int, CategoriesMapping]

  def categorizedSchema(schema: StructType, mappings: Map[Int, CategoriesMapping]): StructType = {
    val mappedType = schema.iterator.zipWithIndex.map { case (field, index) =>
      mappings
        .get(index)
        .map { m =>
        val updatedMetadata = MappingMetadataConverter.mappingToMetadata(m, field.metadata)
        field.copy(metadata = updatedMetadata, dataType = IntegerType)
      }.getOrElse(field)
    }
    StructType(mappedType.toSeq)
  }

  def mappingsMapFromSchema(schema: StructType): CategoricalMappingsMap = {
    schema.zipWithIndex.flatMap {
      case (field, index) => mappingFromMetadata(field.metadata).map(index -> _)
    }.toMap
  }
}
