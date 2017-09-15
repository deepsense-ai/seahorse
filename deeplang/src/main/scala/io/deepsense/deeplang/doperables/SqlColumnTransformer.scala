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

package io.deepsense.deeplang.doperables

import org.apache.spark.sql.catalyst.SqlParser
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions._
import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.params.selections.NameColumnSelection
import io.deepsense.deeplang.params.{Param, StringParam}

case class SqlColumnTransformer() extends MultiColumnTransformer {

  val inputColumnAlias = StringParam(
    name = "input column alias",
    description = "An identifier that can be used in SQL formula to refer the input column.")
  setDefault(inputColumnAlias, "x")

  def getInputColumnAlias: String = $(inputColumnAlias)
  def setInputColumnAlias(value: String): this.type = set(inputColumnAlias, value)

  val formula = StringParam(
    name = "formula",
    description = "SQL formula involving input column as \"x\".")

  def getFormula: String = $(formula)
  def setFormula(value: String): this.type = set(formula, value)

  override def getSpecificParams: Array[Param[_]] = Array(inputColumnAlias, formula)

  override def transformSingleColumn(
      inputColumn: String,
      outputColumn: String,
      context: ExecutionContext,
      dataFrame: DataFrame): DataFrame = {
    val inputColumnAlias = getInputColumnAlias
    val formula = getFormula
    val inputColumnName = inputColumn
    val outputColumnName = outputColumn

    val dataFrameSchema = dataFrame.sparkDataFrame.schema
    validate(dataFrameSchema)

    val (transformedSparkDataFrame, schema) = try {

      val inputColumnNames = dataFrameSchema.map(_.name)
      val outputColumnNames = inputColumnNames :+ s"$formula AS `$outputColumnName`"

      val outputDataFrame = dataFrame.sparkDataFrame
        .selectExpr("*", s"`$inputColumnName` AS `$inputColumnAlias`")
        .selectExpr(outputColumnNames: _*)

      val schema = StructType(outputDataFrame.schema.map {
        _.copy(nullable = true)
      })

      (outputDataFrame, schema)
    }
    catch {
      case e: Exception =>
        throw new SqlColumnTransformationExecutionException(
          inputColumnName, formula, outputColumnName, Some(e))
    }

    val columns = transformedSparkDataFrame.columns
    if (columns.distinct.length != columns.length) {
      throw new SqlColumnTransformationExecutionException(
        inputColumnName,
        formula,
        outputColumnName,
        Some(DuplicatedColumnsException(List(outputColumnName))))
    }

    context.dataFrameBuilder.buildDataFrame(schema, transformedSparkDataFrame.rdd)

  }

  override def transformSingleColumnSchema(
      inputColumn: String,
      outputColumn: String,
      schema: StructType): Option[StructType] = {
    validate(schema)
    // Output column type cannot be determined easily without SQL expression evaluation on DF
    None
  }

  private def validate(schema: StructType) = {
    validateFormula(schema)
    validateUniqueAlias(schema)
  }

  private def validateFormula(schema: StructType) = {
    val formula = getFormula
    try {
      val expression = SqlParser.parseExpression(formula)
      val columnNames = schema.map(_.name).toSet + getInputColumnAlias
      val referredColumnNames = expression.references.map(_.name).toSet
      if(!referredColumnNames.subsetOf(columnNames)) {
        val nonExistingColumns = referredColumnNames -- columnNames
        throw ColumnsDoNotExistException(NameColumnSelection(nonExistingColumns), schema)
      }
    } catch {
      case de: DeepLangException =>
        throw de
      case e: Exception =>
        throw SqlColumnExpressionSyntaxException(formula)
    }
  }

  private def validateUniqueAlias(schema: StructType) = {
    val alias = getInputColumnAlias
    if(schema.map(_.name).contains(alias)) {
      throw ColumnAliasNotUniqueException(alias)
    }
  }
}
