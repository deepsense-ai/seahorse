/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import scala.collection.immutable.ListMap

import org.apache.spark.sql.types.DataType
import org.apache.spark.sql.{Column, UserDefinedFunction}

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.types.{SparkConversions, Conversions}
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoricalMapper, CategoricalMetadata}
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import io.deepsense.deeplang.parameters.ColumnType.ColumnType
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}

case class ConvertType() extends DOperation1To1[DataFrame, DataFrame] {

  override val name: String = "Convert Type"
  override val id: Id = "f8b3c5d0-febe-11e4-b939-0800200c9a66"
  override val parameters: ParametersSchema = ParametersSchema(
    ConvertType.SelectedColumns -> ColumnSelectorParameter(
      "Columns to be converted",
      required = true,
      portIndex = 0
    ),
    ConvertType.TargetType -> ChoiceParameter(
      "Target type of the columns",
      None,
      required = true,
      options = ListMap(ColumnType.values.toList.map(_.toString -> ParametersSchema()): _*)
    )
  )

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = {
    val targetType = ColumnType.withName(parameters.getChoice(ConvertType.TargetType).get.label)
    val columns = dataFrame
      .getColumnNames(parameters.getColumnSelection(ConvertType.SelectedColumns).get)

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
  val SelectedColumns = "selectedColumns"
  val TargetType = "targetType"

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
