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

import org.apache.spark.annotation.DeveloperApi
import org.apache.spark.ml
import org.apache.spark.ml.param.DoubleParam
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame => SparkDataFrame}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.wrappers.spark.DoubleParamWrapper
import io.deepsense.deeplang.{ExecutionContext, UnitSpec}

class SparkTransformerWrapperSpec extends UnitSpec {

  import SparkTransformerWrapperSpec._

  "SparkTransformerWrapper" should {
    "transform DataFrame" in {
      val sparkTransformerWrapper =
        ExampleSparkTransformerWrapper().setParamWrapper(paramValueToSet)

      val context = mock[ExecutionContext]
      val inputDataFrame = mockInputDataFrame()

      sparkTransformerWrapper._transform(context, inputDataFrame) shouldBe
        DataFrame.fromSparkDataFrame(outputDataFrame)
    }
    "infer schema" in {
      val sparkTransformerWrapper =
        ExampleSparkTransformerWrapper().setParamWrapper(paramValueToSet)
      val inputSchema = mock[StructType]
      sparkTransformerWrapper._transformSchema(inputSchema) shouldBe
        Some(outputSchema)
    }
  }

  private def mockInputDataFrame() = {
    val inputSparkDataFrame = mock[SparkDataFrame]
    when(inputSparkDataFrame.schema).thenReturn(StructType(Seq()))

    val inputDataFrame = mock[DataFrame]
    when(inputDataFrame.sparkDataFrame).thenReturn(inputSparkDataFrame)

    inputDataFrame
  }
}

object SparkTransformerWrapperSpec extends MockitoSugar {

  case class ExampleSparkTransformerWrapper()
    extends SparkTransformerWrapper[ParamValueCheckingTransformer] {

    val paramWrapper = new DoubleParamWrapper[ParamValueCheckingTransformer](
      "name",
      "description",
      _.param)
    setDefault(paramWrapper, 0.0)

    def setParamWrapper(value: Double): this.type = set(paramWrapper, value)

    override val params: Array[Param[_]] = declareParams(paramWrapper)
    override def report(executionContext: ExecutionContext): Report = ???
  }

  class ParamValueCheckingTransformer extends ml.Transformer {

    def this(id: String) = this()

    val param = new DoubleParam("id", "name", "description")

    override def transform(dataset: SparkDataFrame): SparkDataFrame = {
      require($(param) == paramValueToSet)
      outputDataFrame
    }

    @DeveloperApi
    override def transformSchema(schema: StructType): StructType = {
      require($(param) == paramValueToSet)
      outputSchema
    }

    override val uid: String = "id"
  }

  val outputDataFrame = mock[SparkDataFrame]

  val outputSchema = mock[StructType]

  val paramValueToSet = 12.0
}
