/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoricalMetadata, CategoriesMapping}
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import io.deepsense.deeplang.doperations.exceptions.WrongColumnTypeException
import io.deepsense.deeplang.parameters.{BooleanParameter, ColumnSelectorParameter, ColumnType, ParametersSchema}
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}

case class OneHotEncoder() extends DOperation1To1[DataFrame, DataFrame] {
  override val name: String = "One Hot Encoder"
  override val id: Id = "b1b6eefe-f7b7-11e4-a322-1697f925ec7b"
  val selectedColumnsKey = "columns"
  val withRedundantKey = "with redundant"

  override val parameters: ParametersSchema = ParametersSchema(
    selectedColumnsKey -> ColumnSelectorParameter(
      "Columns to encode", required = true, portIndex = 0),
    withRedundantKey -> BooleanParameter(
      "Preserve redundant column", default = Some(false), required = true
    )
  )

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = {
    val categoricalMetadata = CategoricalMetadata(dataFrame)
    val selectedColumnNames = dataFrame.getColumnNames(
      parameters.getColumnSelection(selectedColumnsKey).get)
    val withRedundant = parameters.getBoolean(withRedundantKey).get

    def sqlOneHotEncodingExpression(
        columnName: String,
        mapping: CategoriesMapping): List[String] = {

      val valueIdPairs = mapping.valueIdPairs.dropRight(if (withRedundant) 0 else 1)
      val uniqueLevel = dataFrame.getFirstFreeNamesLevel(columnName, mapping.values.toSet)
      for ((value, id) <- valueIdPairs) yield {
        val newColumnName = DataFrameColumnsGetter.createColumnName(columnName, value, uniqueLevel)

        s"IF(`$columnName` IS NULL, CAST(NULL as Double), IF(`$columnName`=$id, 1.0, 0.0))" +
          s"as `$newColumnName`"
      }
    }

    val expressions = for (columnName <- selectedColumnNames) yield {
      categoricalMetadata.mappingOptional(columnName) match {
        case Some(mapping) => sqlOneHotEncodingExpression(columnName, mapping)
        case None => throw WrongColumnTypeException(
          columnName, dataFrame.columnType(columnName), ColumnType.categorical)
      }
    }
    val resultSparkDataFrame = dataFrame.sparkDataFrame.selectExpr("*" +: expressions.flatten: _*)
    context.dataFrameBuilder.buildDataFrame(resultSparkDataFrame)
  }
}
