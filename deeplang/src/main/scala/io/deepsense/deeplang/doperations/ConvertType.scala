/**
 * Copyright 2015, CodiLime Inc.
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

import scala.collection.immutable.ListMap
import scala.collection.mutable.ListBuffer

import org.apache.spark.sql.types.DataType
import org.apache.spark.sql.{Column, UserDefinedFunction}

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.types.{SparkConversions, Conversions}
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoricalMapper, CategoricalMetadata}
import io.deepsense.deeplang.doperables.dataframe._
import io.deepsense.deeplang.inference.{InferenceWarning, ConversionMayNotBePossibleWarning, InferenceWarnings, InferContext}
import io.deepsense.deeplang.parameters.ColumnType.ColumnType
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang._

case class ConvertType() extends DOperation1To1[DataFrame, DataFrame] {

  override val name: String = "Convert Type"
  override val id: Id = "f8b3c5d0-febe-11e4-b939-0800200c9a66"

  val selectedColumnsParameter = ColumnSelectorParameter(
    "Columns to be converted",
    required = true,
    portIndex = 0
  )

  val targetTypeParameter = ChoiceParameter(
    "Target type of the columns",
    None,
    required = true,
    options = ListMap(ColumnType.values.toList.map(_.toString -> ParametersSchema()): _*)
  )

  override val parameters: ParametersSchema = ParametersSchema(
    ConvertType.SelectedColumns -> selectedColumnsParameter,
    ConvertType.TargetType -> targetTypeParameter
  )

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = {
    val targetType = ColumnType.withName(targetTypeParameter.selection.get.label)
    val columns = dataFrame
      .getColumnNames(selectedColumnsParameter.value.get)

    if (targetType == ColumnType.categorical) {
      val metadata = CategoricalMetadata(dataFrame)
      val notYetCategorical = columns.filterNot(metadata.isCategorical)
      val mapper = CategoricalMapper(dataFrame, context.dataFrameBuilder)
      mapper.categorized(notYetCategorical: _*)
    } else {
      logger.info("Finding converters...")
      val converters = findConverters(dataFrame, columns, targetType)
      val columnsOldToNew = findColumnNameMapping(converters.keys, dataFrame)
      logger.info("Executing converters and selecting converted columns...")
      val cleanedUpDf = convert(dataFrame, converters, columnsOldToNew)
      logger.info("Building DF...")
      context.dataFrameBuilder.buildDataFrame(cleanedUpDf)
    }
  }

  private def findConverters(
      dataFrame: DataFrame,
      columns: Seq[String],
      targetType: ColumnType): Map[String, UserDefinedFunction] = {
    require(targetType != ColumnType.categorical)
    val columnDataTypes = columns
      .map(n => n -> dataFrame.sparkDataFrame.schema.apply(n).dataType)
      .toMap

    columnDataTypes.collect {
      case (columnName, sourceDataType)
        if SparkConversions.sparkColumnTypeToColumnType(sourceDataType) != targetType =>
          val converter = findConverter(dataFrame, columnName, targetType, sourceDataType)
          columnName -> converter
    }
  }

  private def findConverter(
      dataFrame: DataFrame,
      columnName: String,
      targetType: ColumnType,
      sourceDataType: DataType): UserDefinedFunction = {
    val sourceColumnType = SparkConversions.sparkColumnTypeToColumnType(sourceDataType)
    if (sourceColumnType == ColumnType.categorical) {
      val mapping = CategoricalMetadata(dataFrame)
      Conversions.generateCategoricalConversion(mapping.mapping(columnName), targetType)
    } else {
      Conversions.UdfConverters(
        (SparkConversions.sparkColumnTypeToColumnType(sourceDataType), targetType))
    }
  }

  private def findColumnNameMapping(columnsToConvert: Iterable[String], dataFrame: DataFrame) = {
    val pairs = columnsToConvert.map(old => (old, dataFrame.uniqueColumnName(old, "convert_type")))
    val oldToNew = pairs.toMap
    oldToNew
  }

  override protected def _inferFullKnowledge(context: InferContext)(
    inputKnowledge: DKnowledge[DataFrame]): (DKnowledge[DataFrame], InferenceWarnings) = {
    val targetType = ColumnType.withName(targetTypeParameter.selection.get.label)
    val inputDF = inputKnowledge.types.head
    val inputDFMetadata = inputDF.inferredMetadata.get
    val (columnsToConvert, selectionsWarnings) =
      inputDFMetadata.select(selectedColumnsParameter.value.get)
    val columnNamesToConvert = columnsToConvert.map(_.name)

    val outputMetadata = convertDataFrameMetadata(
      inputDFMetadata,
      columnNamesToConvert,
      targetType)

    val outputKnowledge = DKnowledge(DataFrameBuilder.buildDataFrameForInference(DataFrameMetadata(
      inputDFMetadata.isExact,
      inputDFMetadata.isColumnCountExact,
      DataFrameMetadata.buildColumnsMap(outputMetadata))))

    val conversionWarningsList = buildConversionWarnings(
      inputDFMetadata,
      columnNamesToConvert,
      targetType)
    (outputKnowledge, selectionsWarnings ++ InferenceWarnings(conversionWarningsList: _*))
  }

  private def convertDataFrameMetadata(
      inputMetadata: DataFrameMetadata,
      columnNamesToConvert: Seq[String],
      targetType: ColumnType): List[ColumnMetadata] = {
    val outputMetadata = new ListBuffer[ColumnMetadata]()
    for (columnMetadata <- inputMetadata.columns.values) {
      val conversionRequired = columnNamesToConvert.contains(columnMetadata.name)
      if (!conversionRequired) {
        outputMetadata += columnMetadata
      } else {
        outputMetadata += convertSingleColumnMetadata(columnMetadata, targetType)
      }
    }
    outputMetadata.toList
  }

  private def convertSingleColumnMetadata(
      metadata: ColumnMetadata,
      targetType: ColumnType): ColumnMetadata = {
    if (metadata.columnType.exists(_ == targetType)) {
      metadata
    } else {
      targetType match {
        case ColumnType.categorical =>
          CategoricalColumnMetadata(metadata.name, metadata.index, None)
        case _ =>
          CommonColumnMetadata(metadata.name, metadata.index, Some(targetType))
      }
    }
  }

  val safeConvertsTo = Seq(ColumnType.string, ColumnType.categorical)
  val safeConvertsMap = Map(
    ColumnType.numeric -> (safeConvertsTo),
    ColumnType.boolean -> (safeConvertsTo ++ Seq(ColumnType.numeric)),
    ColumnType.categorical -> Seq(ColumnType.string),
    ColumnType.string -> Seq(ColumnType.categorical),
    ColumnType.timestamp -> (safeConvertsTo ++ Seq(ColumnType.numeric))
  )

  private def buildConversionWarnings(
      metadata: DataFrameMetadata,
      columnNamesToConvert: Seq[String],
      outputType: ColumnType): Seq[InferenceWarning] = {
    val warnings = new ListBuffer[InferenceWarning]()
    for (column <- columnNamesToConvert) {
      val columnMetadata = metadata.columns.get(column).get
      val inputType = columnMetadata.columnType
      if (inputType.isEmpty) {
        warnings += ConversionMayNotBePossibleWarning(columnMetadata, outputType)
      } else {
        val safeConverts = safeConvertsMap.get(inputType.get)
        if (!safeConverts.exists(_.contains(outputType))) {
          warnings += ConversionMayNotBePossibleWarning(columnMetadata, outputType)
        }
      }
    }
    warnings.toList
  }

  private def convert(
      dataFrame: DataFrame,
      converters: Map[String, UserDefinedFunction],
      columnsOldToNew: Map[String, String]) = {
    val convertedColumns: Seq[Column] = converters.toSeq.map {
      case (columnName: String, converter: UserDefinedFunction) =>
        val column = converter(dataFrame.sparkDataFrame(columnName))
        val alias = columnsOldToNew(columnName)
        column.as(alias)
    }
    val dfWithConvertedColumns =
      dataFrame.sparkDataFrame.select(new Column("*") +: convertedColumns: _*)
    val correctColumns: Seq[Column] = dataFrame.sparkDataFrame.columns.toSeq.map(name => {
      if (columnsOldToNew.contains(name)) {
        new Column(columnsOldToNew(name)).as(name)
      } else {
        new Column(name)
      }
    })
    dfWithConvertedColumns.select(correctColumns: _*)
  }
}

object ConvertType {
  val SelectedColumns = "selected columns"
  val TargetType = "target type"

  def apply(
      targetType: ColumnType.ColumnType,
      names: Set[String] = Set.empty,
      indices: Set[Int] = Set.empty): ConvertType = {
    val ct = new ConvertType
    val params = ct.parameters
    params.getColumnSelectorParameter(SelectedColumns).value =
      Some(MultipleColumnSelection(Vector(
        NameColumnSelection(names),
        IndexColumnSelection(indices))))

    params.getChoiceParameter(TargetType).value = Some(targetType.toString)
    ct
  }

}
