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

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DataType, StructField, StructType}

import ai.deepsense.deeplang.exceptions.DeepLangException
import ai.deepsense.deeplang.{DeeplangIntegTestSupport, ExecutionContext}

/**
 * Allows to test in an easy way whether the behavior of
 * 'transformSingleColumn' and 'transformSingleColumnSchema' methods
 * match the behavior that MultiColumnTransformer expects.
 */
trait MultiColumnTransformerTestSupport {
  self: DeeplangIntegTestSupport =>

  import DeeplangIntegTestSupport._

  /**
   * Transformer name. Used as a test subject's name.
   * Visible in tests output.
   */
  def transformerName: String

  /**
   * Transformer with all necessary parameters already set.
   */
  def transformer: MultiColumnTransformer

  /**
   * Name of the input column used in fixtures.
   */
  def inputColumnName: String = "inputColumn"

  /**
   * Name of the output column used in fixtures.
   */
  def outputColumnName: String = "outputColumn"

  /**
   * The method will be used to create input for the transformer
   * and to create expected output data (input, output)
   */
  def testValues: Seq[(Any, Any)]

  def inputType: DataType

  def outputType: DataType

  transformerName when {
    "transforming a column" should {
      "transform the selected column and save the results in output column" in {
        val t = transformer
        val out = t.transformSingleColumn(
          inputColumnName,
          outputColumnName,
          mock[ExecutionContext],
          supportInputDataFrame)
        assertDataFramesEqual(
          out,
          supportExpectedOutputDataFrame,
          checkRowOrder = true,
          checkNullability = false)
      }
    }
    "transforming schema" should {
      "return a schema that reflects changes done by transformation" in {
        val t = transformer
        val out = t.transformSingleColumnSchema(
          inputColumnName,
          outputColumnName,
          supportInputDataFrame.schema.get)
        out shouldBe 'Defined
        assertSchemaEqual(
          out.get,
          supportExpectedOutputDataFrame.schema.get,
          checkNullability = false)
      }
      "throw an exception" when {
        "output column already exists" in {
          val t = transformer
          a[DeepLangException] shouldBe thrownBy {
            t.transformSingleColumnSchema(
              inputColumnName,
              outputColumnName,
              supportDuplicatedColumnDataFrame.schema.get)
          }
        }
        "selected columns do not exist" in {
          val t = transformer
          an[DeepLangException] shouldBe thrownBy {
            t.transformSingleColumnSchema(
              "columnThatDoesNotExist",
              outputColumnName,
              supportInputDataFrame.schema.get)
          }
        }
      }
    }
  }

  private lazy val (
      supportInputDataFrame,
      supportExpectedOutputDataFrame,
      supportDuplicatedColumnDataFrame) = {
    val inputValues = testValues.map(_._1)
    val outputValues = testValues.map(_._2)
    val inputColumn = inputValues
    val outputColumn = outputValues
    val anotherColumn = inputValues

    val inputData = inputColumn.zipWithIndex.map { case (v, idx) =>
      Row(v, anotherColumn(idx))
    }
    val outputData = inputColumn.zipWithIndex.map { case (v, idx) =>
      Row(v, anotherColumn(idx), outputColumn(idx))
    }
    val duplicatedColumnData = inputData

    val inputSchema = StructType(Seq(
      StructField(inputColumnName, inputType),
      StructField("thirdColumn", inputType)
    ))

    val outputSchema = inputSchema.add(StructField(outputColumnName, outputType))

    val duplicatedColumnSchema = StructType(Seq(
      StructField(inputColumnName, inputType),
      StructField(outputColumnName, outputType),
      StructField("thirdColumn", inputType)
    ))

    val inputDataFrame = createDataFrame(inputData, inputSchema)
    val outputDataFrame = createDataFrame(outputData, outputSchema)
    val duplicatedColumnDataFrame = createDataFrame(duplicatedColumnData, duplicatedColumnSchema)

    (inputDataFrame, outputDataFrame, duplicatedColumnDataFrame)
  }
}
