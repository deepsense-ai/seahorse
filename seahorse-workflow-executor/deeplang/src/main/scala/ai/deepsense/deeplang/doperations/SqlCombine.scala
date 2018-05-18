/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations

import scala.reflect.runtime.{universe => ru}

import org.apache.spark.sql
import org.apache.spark.sql.types.StructType

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.exceptions.DeepLangException
import ai.deepsense.deeplang.inference.{InferenceWarnings, SqlSchemaInferrer}
import ai.deepsense.deeplang.params.exceptions.ParamsEqualException
import ai.deepsense.deeplang.params.{CodeSnippetLanguage, CodeSnippetParam, Param, StringParam}
import ai.deepsense.deeplang.{DOperation2To1, DPortPosition, DataFrame2To1Operation, ExecutionContext}
import ai.deepsense.deeplang.DPortPosition.DPortPosition
import ai.deepsense.sparkutils.{SQL, SparkSQLSession}

final class SqlCombine
  extends DOperation2To1[DataFrame, DataFrame, DataFrame]
  with DataFrame2To1Operation
  with OperationDocumentation {

  override val id: Id = "8f254d75-276f-48b7-872d-e4a18b6a86c6"
  override val name: String = "SQL Combine"
  override val description: String = "Combines two DataFrames into one using custom SQL"

  override def inPortsLayout: Vector[DPortPosition] =
    Vector(DPortPosition.Left, DPortPosition.Right)

  val leftTableName = StringParam(
    name = "Left dataframe id",
    description = Some("The identifier that can be used in the Spark SQL expression to refer the " +
      "left-hand side DataFrame."))
  setDefault(leftTableName, "")

  def getLeftTableName: String = $(leftTableName)
  def setLeftTableName(name: String): this.type = set(leftTableName, name)

  val rightTableName = StringParam(
    name = "Right dataframe id",
    description = Some("The identifier that can be used in the Spark SQL expression to refer the " +
      "right-hand side DataFrame."))
  setDefault(rightTableName, "")

  def getRightTableName: String = $(rightTableName)
  def setRightTableName(name: String): this.type = set(rightTableName, name)

  val sqlCombineExpression = CodeSnippetParam(
    name = "expression",
    description = Some("SQL expression to be executed on two DataFrames, yielding a DataFrame."),
    language = CodeSnippetLanguage(CodeSnippetLanguage.sql))
  setDefault(sqlCombineExpression, "")

  def getSqlCombineExpression: String = $(sqlCombineExpression)
  def setSqlCombineExpression(expression: String): this.type = set(sqlCombineExpression, expression)

  override protected def execute(left: DataFrame, right: DataFrame)(ctx: ExecutionContext): DataFrame = {
    logger.debug(s"SqlCombine(expression = '$getSqlCombineExpression', " +
      s"leftTableName = '$getLeftTableName', " +
      s"rightTableName = '$getRightTableName')")
    val localSparkSQLSession = ctx.sparkSQLSession.newSession()
    val leftDf = moveToSparkSQLSession(left.sparkDataFrame, localSparkSQLSession)
    val rightDf = moveToSparkSQLSession(right.sparkDataFrame, localSparkSQLSession)

    SQL.registerTempTable(leftDf, getLeftTableName)
    SQL.registerTempTable(rightDf, getRightTableName)
    logger.debug(s"Tables '$getLeftTableName', '$getRightTableName' registered. " +
      s"Executing the expression")
    val localSqlResult = localSparkSQLSession.sql(getSqlCombineExpression)
    val sqlResult = moveToSparkSQLSession(localSqlResult, ctx.sparkSQLSession)
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

  private def moveToSparkSQLSession(df: sql.DataFrame, destinationCtx: SparkSQLSession): sql.DataFrame =
    destinationCtx.createDataFrame(df.rdd, df.schema)

  override def specificParams: Array[Param[_]] =
    Array(leftTableName, rightTableName, sqlCombineExpression)

  override def since: Version = Version(1, 4, 0)

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTI_1: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}
