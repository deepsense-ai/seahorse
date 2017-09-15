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

import org.apache.spark.sql.types.StructType

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.{DuplicatedColumnsException, MathematicalTransformationExecutionException}
import io.deepsense.deeplang.params.{Param, StringParam}
import io.deepsense.reportlib.model.{ReportContent, Table}

case class MathematicalTransformation() extends Transformer {

  val formulaParam = StringParam(
    name = "formula",
    description = "SQL formula")

  def getFormula: String = $(formulaParam)
  def setFormula(formula: String): this.type = set(formulaParam, formula)

  val columnNameParam = StringParam(
    name = "column name",
    description = "Name of column holding the result")

  def getColumnName: String = $(columnNameParam)
  def setColumnName(columnName: String): this.type = set(columnNameParam, columnName)

  override val params: Array[Param[_]] = declareParams(formulaParam, columnNameParam)

  override def _transform(context: ExecutionContext, dataFrame: DataFrame): DataFrame = {
    val formula = getFormula
    val columnName = getColumnName

    val (transformedSparkDataFrame, schema) = try {
      val transformedSparkDataFrame =
        dataFrame.sparkDataFrame.selectExpr("*", s"$formula AS `$columnName`")
      val schema = StructType(transformedSparkDataFrame.schema.map { _.copy(nullable = true) })
      (transformedSparkDataFrame, schema)
    }
    catch {
      case e: Exception =>
        throw new MathematicalTransformationExecutionException(
          formula, columnName, Some(e))
    }

    val columns = transformedSparkDataFrame.columns
    if (columns.distinct.length != columns.length) {
      throw new MathematicalTransformationExecutionException(
        formula, columnName, Some(DuplicatedColumnsException(List(columnName))))
    }

    context.dataFrameBuilder.buildDataFrame(schema, transformedSparkDataFrame.rdd)
  }

  override def report(executionContext: ExecutionContext): Report = {
    val table = Table("Mathematical Formula", "",
      Some(List("Formula", "Column name")),
      List(ColumnType.string, ColumnType.string), None,
      List(List(Some(getFormula), Some(getColumnName))))
    Report(ReportContent(
      "Report for MathematicalTransformation", List(table)))
  }
}
