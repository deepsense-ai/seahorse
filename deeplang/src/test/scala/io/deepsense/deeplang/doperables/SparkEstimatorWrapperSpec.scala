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
import org.apache.spark.sql.{DataFrame => SparkDataFrame}

import io.deepsense.deeplang.{ExecutionContext, UnitSpec}

class SparkEstimatorWrapperSpec extends UnitSpec {

  import EstimatorModelWrappersFixtures._

  "SparkEstimatorWrapper" should {
    "fit a DataFrame" in {
      val wrapper = new ExampleSparkEstimatorWrapper().setNumericParamWrapper(paramValueToSet)
      val inputDataFrame = mockInputDataFrame()

      val modelWrapper =
        wrapper._fit(mock[ExecutionContext], inputDataFrame).asInstanceOf[ExampleSparkModelWrapper]
      modelWrapper.model shouldBe fitModel
    }
    "infer knowledge when schema is provided" in {
      val wrapper = new ExampleSparkEstimatorWrapper().setNumericParamWrapper(paramValueToSet)
      val inferredModelWrapper = wrapper._fit_infer(Some(mock[StructType]))
        .asInstanceOf[ExampleSparkModelWrapper]
      inferredModelWrapper.parentEstimator.sparkEstimator shouldBe wrapper.sparkEstimator
    }
    "infer knowledge when schema isn't provided" in {
      val wrapper = new ExampleSparkEstimatorWrapper()
      val inferredModelWrapper = wrapper._fit_infer(None).asInstanceOf[ExampleSparkModelWrapper]
      inferredModelWrapper.parentEstimator.sparkEstimator shouldBe wrapper.sparkEstimator
    }
  }
}
