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

import io.deepsense.deeplang.params.ParamMap
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.doperables.dataframe.DataFrame
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
    "throw an exception in transform when params are not set" in {
      val wrapper = prepareWrapperWithoutParams()
      an[Exception] shouldBe thrownBy(
        wrapper._transform(mock[ExecutionContext], mockInputDataFrame()))
    }
    "throw an exception in transformSchema when params are not set" in {
      val inputSchema = mock[StructType]
      val wrapper = prepareWrapperWithoutParams()
      an[Exception] shouldBe thrownBy(wrapper._transformSchema(inputSchema))
    }
  }

  private def prepareWrapperAndEstimator = {
    val model = new ExampleSparkModel()
    val wrapper = new ExampleSparkModelWrapper().setModel(model)
    (wrapper, new ExampleSparkEstimatorWrapper())
  }

  private def prepareWrapperWithoutParams(): ExampleSparkModelWrapper = {
    val (wrapper, parentEstimator) = prepareWrapperAndEstimator
    wrapper.setParent(parentEstimator)
  }

  private def prepareWrapperWithParams(): ExampleSparkModelWrapper = {
    val (wrapper, parentEstimator) = prepareWrapperAndEstimator
    wrapper.setParent(parentEstimator).setNumericParamWrapper(paramValueToSet)
  }
}
