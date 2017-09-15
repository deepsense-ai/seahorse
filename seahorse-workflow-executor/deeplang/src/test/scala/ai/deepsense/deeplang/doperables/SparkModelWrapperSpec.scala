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

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.serialization.SerializableSparkModel
import ai.deepsense.deeplang.inference.exceptions.SparkTransformSchemaException
import ai.deepsense.deeplang.params.ParamMap
import ai.deepsense.deeplang.{DeeplangTestSupport, ExecutionContext, UnitSpec}

class SparkModelWrapperSpec extends UnitSpec with DeeplangTestSupport{

  import EstimatorModelWrappersFixtures._

  "SparkModelWrapper" should {
    "ignore default parameter values" in {
      val wrapper = new ExampleSparkModelWrapper()
      wrapper.extractParamMap() shouldBe ParamMap.empty
    }
    "transform a DataFrame" in {
      val wrapper = prepareWrapperWithParams()
      wrapper._transform(mock[ExecutionContext], createDataFrame()) shouldBe
        DataFrame.fromSparkDataFrame(fitDataFrame)
    }
    "transform schema" in {
      val inputSchema = createSchema()
      val wrapper = prepareWrapperWithParams()
      wrapper._transformSchema(inputSchema) shouldBe Some(transformedSchema)
    }
    "forward an exception thrown by transformSchema wrapped in DeepLangException" in {
      val inputSchema = createSchema()
      val wrapper = prepareWrapperWithParams()
      wrapper.parentEstimator.sparkEstimator
        .setTransformSchemaShouldThrow(true)
      val e = intercept[SparkTransformSchemaException] {
        wrapper._transformSchema(inputSchema)
      }
      e.exception shouldBe exceptionThrownByTransformSchema
    }
  }

  private def prepareWrapperWithParams(): ExampleSparkModelWrapper = {
    val model = new SerializableSparkModel(new ExampleSparkModel())
    val wrapper = new ExampleSparkModelWrapper().setModel(model)
    val parentEstimator = new ExampleSparkEstimatorWrapper()
    wrapper.setParent(parentEstimator).setNumericParamWrapper(paramValueToSet)
  }
}
