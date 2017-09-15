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

package io.deepsense.deeplang.doperations

import scala.reflect.runtime.{universe => ru}

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{Metadata, StructField, StructType}

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoricalMetadata, MappingMetadataConverter}
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameMetadata}
import io.deepsense.deeplang.doperations.exceptions.SchemaMismatchException
import io.deepsense.deeplang.parameters.ColumnType.ColumnType
import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.deeplang.{DOperation2To1, ExecutionContext}


case class Union() extends DOperation2To1[DataFrame, DataFrame, DataFrame] {
  override val id: Id = "90fed07b-d0a9-49fd-ae23-dd7000a1d8ad"
  override val name: String = "Union"
  override val parameters: ParametersSchema = ParametersSchema()

  override protected def _execute(
      context: ExecutionContext)(first: DataFrame, second: DataFrame): DataFrame = {

    assertSchemaSimilarity(first, second)

    val equalSchemas =
      first.sparkDataFrame.schema == second.sparkDataFrame.schema

    val secondRemapped = equalSchemas match {
      case true => second
      case false => remapCategoricalValues(context, second, CategoricalMetadata(first))
    }

    // build a union based on metadata from the remapped second DF
    buildUnion(context, first, secondRemapped, secondRemapped.metadata.get)
  }

  /**
   * Basic schema comparison: column names and types
   */
  private def assertSchemaSimilarity(first: DataFrame, second: DataFrame): Unit = {
    val firstSchema = first.sparkDataFrame.schema
    val secondSchema = second.sparkDataFrame.schema

    def withNoMeta(f: StructField): StructField =
      f.copy(metadata = Metadata.empty)

    def columnTypes(schema: StructType): Seq[Option[ColumnType]] =
      DataFrameMetadata.fromSchema(schema).orderedColumns.map(_.columnType)

    val similar =
      (firstSchema.fields.map(withNoMeta) sameElements secondSchema.fields.map(withNoMeta)) &&
      columnTypes(firstSchema) == columnTypes(secondSchema)

    if (!similar) {
      throw new SchemaMismatchException(
        "SchemaMismatch. Expected schema " +
        s"${first.sparkDataFrame.schema.treeString}" +
          s" differs from ${second.sparkDataFrame.schema.treeString}")
    }
  }

  private def buildUnion(
      context: ExecutionContext,
      first: DataFrame,
      second: DataFrame,
      metadata: DataFrameMetadata): DataFrame = {

    context.dataFrameBuilder.buildDataFrame(
      metadata,
      first.sparkDataFrame.unionAll(second.sparkDataFrame).rdd)
  }

  /**
   * Takes a DataFrame and a categorical mapping of another DataFrame
   * and returns a new DataFrame that has each of its categorical columns
   * remapped to a new set of values, that extends the respective set of values
   * in externalMetadata
   *
   * @param dataFrame A DataFrame to base the result on
   * @param externalMetadata Metadata of another DataFrame
   *                         to be the base of new categorical mappings
   * @return A remapped DataFrame
   */
  private def remapCategoricalValues(
      context: ExecutionContext,
      dataFrame: DataFrame,
      externalMetadata: CategoricalMetadata): DataFrame = {

    val metadata = CategoricalMetadata(dataFrame)

    val mergedMappingsByColumnIdx = externalMetadata.mappingById.map {
      case (i: Int, extMapping) =>
        i -> extMapping.mergeWith(metadata.mapping(i))
    }

    def convertSingleColumn(value: Int, columnIndex: Int): Int = {
      mergedMappingsByColumnIdx(columnIndex).otherToFinal.mapId(value)
    }

    val newRows = dataFrame.sparkDataFrame.map { row =>
      val newColumnValues = row.toSeq.zipWithIndex.map {
        case (value: Int, index: Int) if metadata.isCategorical(index) =>
          convertSingleColumn(value, index)

        case (v, i: Int) => v
      }

      Row(newColumnValues: _*)
    }

    val newStructFields = dataFrame.sparkDataFrame.schema.zipWithIndex.map {
      case (field, index: Int) if metadata.isCategorical(index) =>
        val newMetadata = MappingMetadataConverter.mappingToMetadata(
          mergedMappingsByColumnIdx(index).finalMapping)
        field.copy(metadata = newMetadata)

      case (field, _) => field
    }

    context.dataFrameBuilder.buildDataFrame(StructType(newStructFields), newRows)
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTI_1: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}
