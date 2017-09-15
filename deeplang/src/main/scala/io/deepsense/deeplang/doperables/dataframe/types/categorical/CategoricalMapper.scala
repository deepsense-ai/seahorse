/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe.types.categorical

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, StructType}

import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}

case class CategoricalMapper(dataFrame: DataFrame, dataFrameBuilder: DataFrameBuilder) {
  private val sparkDataFrame = dataFrame.sparkDataFrame

  def categorized(columnsNames: String*): DataFrame = {
    val distinctValues = columnsNames.map(n => n -> distinctColumnValues(n)).toMap
    val mappings = distinctValues.map{
      case (columnName, values) =>
        columnIndex(columnName) -> CategoriesMapping(values.toSeq)
    }
    val remappedRdd = mapCategoricals(mappings)
    dataFrameWithMappingMetadata(remappedRdd, mappings)
  }

  private def distinctColumnValues(column: String): Array[String] =
    sparkDataFrame
      .select(column)
      .distinct
      .map(r => r(0).asInstanceOf[String])
      .collect()

  private def mapCategoricals(mappings: Map[Int, CategoriesMapping]): RDD[Row] =
    sparkDataFrame.map(r => {
      val seq = Row.unapplySeq(r).get.zipWithIndex
      val mappedSeq = seq.map { case (value, index) =>
        mappings
          .get(index)
          .map(m => m.valueToId(value.asInstanceOf[String]))
          .getOrElse(value)
      }
      Row(mappedSeq: _*)
    })

  private def columnIndex(name: String): Int =
    sparkDataFrame.schema.fieldNames.toIndexedSeq.indexOf(name)

  private def dataFrameWithMappingMetadata(
      rdd: RDD[Row],
      mappings: Map[Int, CategoriesMapping]): DataFrame = {
    val schema = sparkDataFrame.schema
    val mappedType = schema.iterator.zipWithIndex.map { case (field, index) =>
      mappings
        .get(index)
        .map { m =>
          val updatedMetadata = MappingMetadataConverter.mappingToMetadata(m, field.metadata)
          field.copy(metadata = updatedMetadata, dataType = IntegerType)
        }.getOrElse(field)
    }
    val updatedSchema = StructType(mappedType.toSeq)
    dataFrameBuilder.buildDataFrame(updatedSchema, rdd)
  }
}
