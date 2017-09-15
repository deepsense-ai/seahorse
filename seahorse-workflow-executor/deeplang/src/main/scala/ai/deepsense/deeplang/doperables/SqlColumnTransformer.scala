/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperables

import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.exceptions._
import ai.deepsense.deeplang.exceptions.DeepLangException
import ai.deepsense.deeplang.params.selections.NameColumnSelection
import ai.deepsense.deeplang.params.{Param, StringParam}
import ai.deepsense.deeplang.utils.SparkUtils
import ai.deepsense.sparkutils.SQL

case class SqlColumnTransformer() extends MultiColumnTransformer {

  val inputColumnAlias = StringParam(
    name = "input column alias",
    description = Some("An identifier that can be used in SQL formula to refer the input column."))
  setDefault(inputColumnAlias -> "x")
  def getInputColumnAlias: String = $(inputColumnAlias)
  def setInputColumnAlias(value: String): this.type = set(inputColumnAlias, value)

  val formula = StringParam(
    name = "formula",
    description = Some("SQL formula that uses input column as \"x\"."))
  setDefault(formula -> "x")
  def getFormula: String = $(formula)
  def setFormula(value: String): this.type = set(formula, value)

  override def getSpecificParams: Array[Param[_]] = Array(inputColumnAlias, formula)

  override def transformSingleColumn(
      inputColumn: String,
      outputColumn: String,
      context: ExecutionContext,
      dataFrame: DataFrame): DataFrame = {
    val inputColumnAlias = SparkUtils.escapeColumnName(getInputColumnAlias)
    val formula = getFormula
    val inputColumnName = SparkUtils.escapeColumnName(inputColumn)
    val outputColumnName = SparkUtils.escapeColumnName(outputColumn)

    val dataFrameSchema = dataFrame.sparkDataFrame.schema
    validate(dataFrameSchema)

    val (transformedSparkDataFrame, schema) = try {

      val inputColumnNames = dataFrameSchema.map(c => SparkUtils.escapeColumnName(c.name))
      val outputColumnNames = inputColumnNames :+ s"$formula AS $outputColumnName"

      val outputDataFrame = dataFrame.sparkDataFrame
        .selectExpr("*", s"$inputColumnName AS $inputColumnAlias")
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
      val expression = SQL.SqlParser.parseExpression(formula)
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
