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

import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.catalyst.analysis.NoSuchTableException
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import ai.deepsense.deeplang.DeeplangIntegTestSupport
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.spark.wrappers.transformers.TransformerSerialization
import ai.deepsense.deeplang.doperations.exceptions.SqlExpressionException
import ai.deepsense.sparkutils.SQL
import ai.deepsense.sparkutils.spi.SparkSessionInitializer

class SqlTransformationIntegSpec extends DeeplangIntegTestSupport with TransformerSerialization {

  import DeeplangIntegTestSupport._
  import TransformerSerialization._

  val dataFrameId = "ThisIsAnId"
  val validExpression = s"select * from $dataFrameId"
  val invalidExpression = "foobar"

  val firstColumn = "firstColumn"
  val secondColumn = "secondColumn"
  val thirdColumn = "thirdColumn"

  val schema = StructType(Seq(
    StructField(firstColumn, StringType),
    StructField(secondColumn, DoubleType),
    StructField(thirdColumn, BooleanType)
  ))

  val data = Seq(
    Row("c",  5.0,  true),
    Row("a",  5.0,  null),
    Row("b",  null, false),
    Row(null, 2.1,  true)
  )

  "SqlTransformation" should {
    "allow to manipulate the input DataFrame using the specified name" in {
      val expression = s"select $secondColumn from $dataFrameId"
      val result = executeSqlTransformation(expression, dataFrameId, sampleDataFrame)
      val selectedColumnsIndices = Seq(1) // 'secondColumn' index
      val expectedDataFrame = subsetDataFrame(selectedColumnsIndices)
      assertDataFramesEqual(result, expectedDataFrame)
      assertTableUnregistered()
    }
    "allow use of UDFs defined in primary SQL Context" in {
      val expression = s"select myOp($secondColumn) as foo from $dataFrameId"
      val resultSchema = executeSqlSchemaTransformation(expression, dataFrameId, schema)
      val expectedSchema = StructType(Seq(StructField("foo", IntegerType)))
      val result = executeSqlTransformation(expression, dataFrameId, sampleDataFrame)
      val expectedDataFrame = sampleDataFrame.sparkDataFrame.select(col(secondColumn) cast IntegerType as "foo")
      assertSchemaEqual(resultSchema, expectedSchema)

      assertDataFramesEqual(result, DataFrame.fromSparkDataFrame(expectedDataFrame))
      assertTableUnregistered()
    }
    "correctly infer output DataFrame schema" in {
      val expression = s"select $firstColumn, $thirdColumn from $dataFrameId"
      val result = executeSqlSchemaTransformation(expression, dataFrameId, schema)
      val selectedColumnsIndices = Seq(0, 2)
      val expectedSchema = StructType(selectedColumnsIndices.map(schema))
      assertSchemaEqual(result, expectedSchema)
      assertTableUnregistered()
    }
    "correctly infer schema when using user defined functions" in {
      val expression = s"SELECT SIGNUM($secondColumn) sig FROM $dataFrameId"
      val result = executeSqlSchemaTransformation(expression, dataFrameId, schema)
      val expectedSchema = StructType(Seq(StructField("sig", DoubleType)))
      assertSchemaEqual(result, expectedSchema)
    }
    "throw the appropriate exception when the table does not exist" in {
      val expression = s"SELECT * FROM notexistanttable"
      a [SqlExpressionException] should be thrownBy {
        executeSqlSchemaTransformation(expression, dataFrameId, schema)
      }
    }
    "throw the appropriate exception when trying to infer schema with invalid statement" in {
      val expression = s"SELEC * FRMO $dataFrameId"
      a [SqlExpressionException] should be thrownBy {
        executeSqlSchemaTransformation(expression, dataFrameId, schema)
      }
    }
    "throw the appropriate exception when no tables are used" in {
      val expression = s"SELECT *"
      a [SqlExpressionException] should be thrownBy {
        executeSqlSchemaTransformation(expression, dataFrameId, schema)
      }
    }
    "unregister the input DataFrame after execution" in {
      val dataFrame = sampleDataFrame
      executeSqlTransformation(validExpression, dataFrameId, dataFrame)
      assertTableUnregistered()
    }
    "unregister the input DataFrame if execution failed" in {
      val dataFrame = sampleDataFrame
      a [SQL.ExceptionThrownByInvalidExpression] should be thrownBy {
        executeSqlTransformation(invalidExpression, dataFrameId, dataFrame)
      }
      assertTableUnregistered()
    }
  }

  def assertTableUnregistered(): Unit = {
    intercept[NoSuchTableException] {
      executionContext.sparkSQLSession.table(dataFrameId)
    }
  }

  def executeSqlTransformation(
    expression: String,
    dataFrameId: String,
    input: DataFrame): DataFrame = {

    val transformer = new SqlTransformer()
      .setExpression(expression)
      .setDataFrameId(dataFrameId)
    transformer.applyTransformationAndSerialization(tempDir, input)
  }

  def executeSqlSchemaTransformation(
    expression: String,
    dataFrameId: String,
    input: StructType): StructType = {

    val transformer = new SqlTransformer()
      .setExpression(expression)
      .setDataFrameId(dataFrameId)
    transformer._transformSchema(input).getOrElse(new StructType())
  }

  def sampleDataFrame: DataFrame = createDataFrame(data, schema)

  def subsetDataFrame(columns: Seq[Int]): DataFrame = {
    val subSchema = StructType(columns.map(schema))
    val subData = data.map { r => Row(columns.map(r.get): _*) }
    createDataFrame(subData, subSchema)
  }
}


