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

package ai.deepsense.deeplang.params.wrappers.spark

import org.apache.spark.ml
import org.apache.spark.ml.param._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

class WrappersDefaultValidationSpec
  extends WordSpec
  with Matchers
  with MockitoSugar {

  class ExampleSparkParams extends ml.param.Params {
    override val uid: String = "id"
    val intSparkParam = new IntParam("", "name", "description")
    val floatSparkParam = new FloatParam("", "name", "description")
    val doubleSparkParam = new DoubleParam("", "name", "description")

    override def copy(extra: ParamMap): Params = ???
  }

  "IntParamWrapper" should {

    val intParamWrapper = new IntParamWrapper[ExampleSparkParams](
      "name",
      Some("description"),
      _.intSparkParam)

    "validate whole Int range" in {
      intParamWrapper.validate(Int.MinValue + 1) shouldBe empty
      intParamWrapper.validate(Int.MaxValue - 1) shouldBe empty
    }
    "reject fractional values" in {
      intParamWrapper.validate(Int.MinValue + 0.005) should have size 1
      intParamWrapper.validate(Int.MaxValue - 0.005) should have size 1
    }
  }

  "FloatParamWrapper" should {

    val floatParamWrapper = new FloatParamWrapper[ExampleSparkParams](
      "name",
      Some("description"),
      _.floatSparkParam)

    "validate whole Float range" in {
      floatParamWrapper.validate(Float.MinValue + 1) shouldBe empty
      floatParamWrapper.validate(Float.MaxValue - 1) shouldBe empty
    }
    "reject values out of Float range" in {
      floatParamWrapper.validate(Double.MinValue + 1) should have size 1
      floatParamWrapper.validate(Double.MaxValue - 1) should have size 1
    }
  }

  "DoubleParamWrapper" should {
    "validate whole Double range" in {
      val doubleParamWrapper = new DoubleParamWrapper[ExampleSparkParams](
        "name",
        Some("description"),
        _.doubleSparkParam)
      doubleParamWrapper.validate(Double.MinValue + 1) shouldBe empty
      doubleParamWrapper.validate(Double.MinValue - 1) shouldBe empty
    }
  }
}
