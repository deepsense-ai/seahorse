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

import org.apache.spark.ml.param.Param
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

class SparkParamUtilsSpec extends WordSpec
  with Matchers
  with MockitoSugar {

  "SparkParamUtils" should {

    "split camel case variable names to uppercase words" in {
      val paramMock = new Param[String]("", name = "minInstancesPerNode", "")
      SparkParamUtils.defaultName(paramMock) shouldBe "MIN INSTANCES PER NODE"
    }

    "convert description's first letter to uppercase" in {
      val paramMock = new Param[String]("", "", doc = "label column name")
      SparkParamUtils.defaultDescription(paramMock) shouldBe "Label column name"
    }
  }
}
