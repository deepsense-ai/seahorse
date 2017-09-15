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

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoricalMetadata, CategoriesMapping}
import io.deepsense.deeplang.doperations.exceptions.WrongColumnTypeException
import io.deepsense.deeplang.parameters.{BooleanParameter, ColumnSelectorParameter, ColumnType, ParametersSchema, _}
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}

case class OneHotEncoder() extends DOperation1To1[DataFrame, DataFrame] {

  override val name: String = "One Hot Encoder"
  override val id: Id = "b1b6eefe-f7b7-11e4-a322-1697f925ec7b"

  val selectedColumnsParam = ColumnSelectorParameter(
    "Categorical columns to encode", required = true, portIndex = 0)

  val withRedundantParam = BooleanParameter(
    "Preserve redundant column", default = Some(false), required = true)

  val prefixParam = PrefixBasedColumnCreatorParameter(
    "Prefix for generated columns", None, required = false)

  override val parameters: ParametersSchema = ParametersSchema(
    "columns" -> selectedColumnsParam,
    "with redundant" -> withRedundantParam,
    "prefix" -> prefixParam
  )

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = {
    val categoricalMetadata = CategoricalMetadata(dataFrame)
    val selectedColumnNames = dataFrame.getColumnNames(selectedColumnsParam.value.get)
    val withRedundant = withRedundantParam.value.get
    val prefix = prefixParam.value.getOrElse("")

    def sqlOneHotEncodingExpression(
        columnName: String,
        mapping: CategoriesMapping): List[String] = {

      val valueIdPairs = mapping.valueIdPairs.dropRight(if (withRedundant) 0 else 1)
      for ((value, id) <- valueIdPairs) yield {
        // TODO: replace should be removed after spark upgrade to version containing bugfix. DS-635
        val newColumnName = (prefix + columnName + "_" + value).replace(".", "_")

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

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

object OneHotEncoder {
  def apply(
      selection: MultipleColumnSelection,
      withRedundancy: Boolean,
      prefix: Option[String]): OneHotEncoder = {

    val encoder = new OneHotEncoder
    encoder.selectedColumnsParam.value = Some(selection)
    encoder.withRedundantParam.value = Some(withRedundancy)
    encoder.prefixParam.value = prefix
    encoder
  }
}
