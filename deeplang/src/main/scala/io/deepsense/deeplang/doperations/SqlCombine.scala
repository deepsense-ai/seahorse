/**
 * Copyright 2016, deepsense.io
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

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.{DOperation2To1, DataFrame2To1Operation, ExecutionContext}
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.inference.{InferenceWarnings, SqlSchemaInferrer}
import io.deepsense.deeplang.params.exceptions.ParamsEqualException
import io.deepsense.deeplang.params.{CodeSnippetLanguage, CodeSnippetParam, Param, StringParam}
import org.apache.spark.sql
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.documentation.OperationDocumentation

final class SqlCombine
  extends DOperation2To1[DataFrame, DataFrame, DataFrame]
  with DataFrame2To1Operation
  with OperationDocumentation {

  override val id: Id = "8f254d75-276f-48b7-872d-e4a18b6a86c6"
  override val name: String = "SQL Combine"
  override val description: String = "Combines two DataFrames into one using custom SQL"

  val leftTableName = StringParam(
    name = "Left dataframe id",
    description = "The identifier that can be used in the Spark SQL expression to refer the " +
      "left-hand side DataFrame.")
  setDefault(leftTableName, "")

  def getLeftTableName: String = $(leftTableName)
  def setLeftTableName(name: String): this.type = set(leftTableName, name)

  val rightTableName = StringParam(
    name = "Right dataframe id",
    description = "The identifier that can be used in the Spark SQL expression to refer the " +
      "right-hand side DataFrame.")

  def getRightTableName: String = $(rightTableName)
  def setRightTableName(name: String): this.type = set(rightTableName, name)

  val sqlCombineExpression = CodeSnippetParam(
    name = "expression",
    description = "SQL expression to be executed on two DataFrames, yielding a DataFrame.",
    language = CodeSnippetLanguage(CodeSnippetLanguage.sql))

  def getSqlCombineExpression: String = $(sqlCombineExpression)
  def setSqlCombineExpression(expression: String): this.type = set(sqlCombineExpression, expression)

  override protected def execute(left: DataFrame, right: DataFrame)(ctx: ExecutionContext): DataFrame = {
    logger.debug(s"SqlCombine(expression = '$getSqlCombineExpression', " +
      s"leftTableName = '$getLeftTableName', " +
      s"rightTableName = '$getRightTableName')")
    val localSparkSession = ctx.sparkSession.newSession()
    val leftDf = moveToSparkSession(left.sparkDataFrame, localSparkSession)
    val rightDf = moveToSparkSession(right.sparkDataFrame, localSparkSession)

    leftDf.createOrReplaceTempView(getLeftTableName)
    rightDf.createOrReplaceTempView(getRightTableName)
    logger.debug(s"Tables '$getLeftTableName', '$getRightTableName' registered. " +
      s"Executing the expression")
    val sqlResult = moveToSparkSession(localSparkSession.sql(getSqlCombineExpression),
      ctx.sparkSession)
    DataFrame.fromSparkDataFrame(sqlResult)
  }

  override protected def inferSchema(leftSchema: StructType, rightSchema: StructType)
  : (StructType, InferenceWarnings) = {
    new SqlSchemaInferrer().inferSchema(getSqlCombineExpression,
      (getLeftTableName, leftSchema),
      (getRightTableName, rightSchema))
  }

  override protected def customValidateParams: Vector[DeepLangException] = {
    if (getLeftTableName == getRightTableName) {
      ParamsEqualException(
        firstParamName = "left dataframe id",
        secondParamName = "right dataframe id",
        value = getLeftTableName).toVector
    } else {
      Vector.empty
    }
  }

  private def moveToSparkSession(df: sql.DataFrame, destinationCtx: SparkSession): sql.DataFrame =
    destinationCtx.createDataFrame(df.rdd, df.schema)

  override def params: Array[Param[_]] =
    declareParams(leftTableName, rightTableName, sqlCombineExpression)

  override def since: Version = Version(1, 4, 0)
}
