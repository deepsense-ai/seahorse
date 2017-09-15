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

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.exceptions.SparkTransformSchemaException
import io.deepsense.deeplang.params.ParamMap
import io.deepsense.deeplang.{ExecutionContext, UnitSpec}

class SparkModelWrapperSpec extends UnitSpec {

  import EstimatorModelWrappersFixtures._

  "SparkModelWrapper" should {
    "ignore default parameter values" in {
      val wrapper = new ExampleSparkModelWrapper()
      wrapper.extractParamMap() shouldBe ParamMap.empty
    }
    "transform a DataFrame" in {
      val wrapper = prepareWrapperWithParams()
      wrapper._transform(mock[ExecutionContext], mockInputDataFrame()) shouldBe
        DataFrame.fromSparkDataFrame(fitDataFrame)
    }
    "transform schema" in {
      val inputSchema = mock[StructType]
      val wrapper = prepareWrapperWithParams()
      wrapper._transformSchema(inputSchema) shouldBe Some(transformedSchema)
    }
    "forward an exception thrown by transformSchema wrapped in DeepLangException" in {
      val inputSchema = mock[StructType]
      val wrapper = prepareWrapperWithParams()
      wrapper.parentEstimator.sparkEstimator.setTransformSchemaShouldThrow(true)
      val e = intercept[SparkTransformSchemaException] {
        wrapper._transformSchema(inputSchema)
      }
      e.exception shouldBe exceptionThrownByTransformSchema
    }
  }

  private def prepareWrapperWithParams(): ExampleSparkModelWrapper = {
    val model = new ExampleSparkModel()
    val wrapper = new ExampleSparkModelWrapper().setModel(model)
    val parentEstimator = new ExampleSparkEstimatorWrapper()
    wrapper.setParent(parentEstimator).setNumericParamWrapper(paramValueToSet)
  }
}
