/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import org.apache.spark.sql
import org.apache.spark.sql.UserDefinedFunction

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.dataframe.types.Conversions
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoricalMapper, CategoricalMetadata}
import io.deepsense.deeplang.parameters.ColumnType.ColumnType
import io.deepsense.deeplang.parameters.{ChoiceParameter, ColumnSelectorParameter, ColumnType, ParametersSchema}
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}

class ConvertType extends DOperation1To1[DataFrame, DataFrame] {

  override val name: String = "Convert Type"
  override val id: Id = "f8b3c5d0-febe-11e4-b939-0800200c9a66"
  override val parameters: ParametersSchema = ParametersSchema(
    ConvertType.SelectedColumns -> ColumnSelectorParameter(
      "Columns to be converted",
      required = true
    ),
    ConvertType.TargetType -> ChoiceParameter(
      "Target type of the columns",
      None,
      required = true,
      options = ColumnType.values.map(v => v.toString -> ParametersSchema()).toMap
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
      val converters = findConverters(dataFrame, columns, targetType)
      val (columnsOldToNew, columnsNewToOld) = findColumnNameMapping(converters.keys, dataFrame)
      val convertedDf = executeConverters(dataFrame, converters, columnsOldToNew)
      val cleanedUpDf = selectConvertedColumns(
        convertedDf,
        columnsNewToOld,
        columnsOldToNew,
        dataFrame.sparkDataFrame.columns,
        converters.keys)
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
        if DataFrame.sparkColumnTypeToColumnType(sourceDataType) != targetType =>
        val sourceColumnType = DataFrame.sparkColumnTypeToColumnType(sourceDataType)
        val converter = if (sourceColumnType == ColumnType.categorical) {
          val mapping = CategoricalMetadata(dataFrame)
          Conversions.generateCategoricalConversion(mapping.mapping(columnName), targetType)
        } else {
          Conversions.UdfConverters(
            (DataFrame.sparkColumnTypeToColumnType(sourceDataType), targetType))
        }
        columnName -> converter
    }
  }

  private def findColumnNameMapping(columnsToConvert: Iterable[String], dataFrame: DataFrame) = {
    val pairs = columnsToConvert.map(old => (old, dataFrame.uniqueColumnName(old, "convert_type")))
    val oldToNew = pairs.toMap
    val newToOld = pairs.map { case (oldName, newName) => (newName, oldName) }.toMap
    (oldToNew, newToOld)
  }

  private def executeConverters(
      dataFrame: DataFrame,
      converters: Map[String, UserDefinedFunction],
      columnsOldToNew: Map[String, String]) = {
    converters.foldLeft(dataFrame.sparkDataFrame){ case (df, (columnName, converter)) =>
      df.withColumn(columnsOldToNew(columnName), converter(dataFrame.sparkDataFrame(columnName)))
    }
  }

  private def selectConvertedColumns(
      sparkDataFrame: sql.DataFrame,
      columnsNewToOld: Map[String, String],
      columnsOldToNew: Map[String, String],
      inputColumns: Array[String],
      changedColumns: Iterable[String]) = {
    val convertedColumnsNames = inputColumns.toSeq
      .map { name =>
      sparkDataFrame(columnsOldToNew.getOrElse(name, name))
    }
    val convertedDataFrame = sparkDataFrame.select(convertedColumnsNames: _*)
    changedColumns.foldLeft(convertedDataFrame){ case (df, columnName) =>
      val columnAfterRename = columnsOldToNew(columnName)
      df.withColumnRenamed(columnAfterRename, columnsNewToOld(columnAfterRename))
    }
  }
}

object ConvertType {
  val SelectedColumns = "selectedColumns"
  val TargetType = "targetType"
}
