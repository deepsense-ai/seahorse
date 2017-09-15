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

package io.deepsense.deeplang.params.wrappers.spark

import org.apache.spark.ml.param.{DoubleParam, FloatParam, IntParam}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

class WrappersDefaultValidationSpec
  extends WordSpec
  with Matchers
  with MockitoSugar {

  "IntParamWrapper" should {

    val intSparkParam = new IntParam("", "name", "description")
    val intParamWrapper = new IntParamWrapper(intSparkParam)

    "validate whole Int range" in {
      intParamWrapper.validate(Int.MinValue + 1) shouldBe Vector()
      intParamWrapper.validate(Int.MaxValue - 1) shouldBe Vector()
    }
    "reject fractional values" in {
      intParamWrapper.validate(Int.MinValue + 0.005).size shouldBe 1
      intParamWrapper.validate(Int.MaxValue - 0.005).size shouldBe 1
    }
  }

  "FloatParamWrapper" should {

    val floatSparkParam = new FloatParam("", "name", "description")
    val floatParamWrapper = new FloatParamWrapper(floatSparkParam)

    "validate whole Float range" in {
      floatParamWrapper.validate(Float.MinValue + 1) shouldBe Vector()
      floatParamWrapper.validate(Float.MaxValue - 1) shouldBe Vector()
    }
    "reject values out of Float range" in {
      floatParamWrapper.validate(Double.MinValue + 1).size shouldBe 1
      floatParamWrapper.validate(Double.MaxValue - 1).size shouldBe 1
    }
  }

  "DoubleParamWrapper" should {
    "validate whole Double range" in {
      val doubleSparkParam = new DoubleParam("", "name", "description")
      val doubleParamWrapper = new DoubleParamWrapper(doubleSparkParam)
      doubleParamWrapper.validate(Double.MinValue + 1) shouldBe Vector()
      doubleParamWrapper.validate(Double.MinValue - 1) shouldBe Vector()
    }
  }
}
