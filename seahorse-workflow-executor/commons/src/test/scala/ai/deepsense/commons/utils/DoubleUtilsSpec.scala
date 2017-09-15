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

package ai.deepsense.commons.utils

import ai.deepsense.commons.{StandardSpec, UnitTestSupport}

class DoubleUtilsSpec extends StandardSpec with UnitTestSupport {

  "double2String" should {
    "return string 'NaN' only when Double.NaN is passed as argument" in {
      DoubleUtils.double2String(Double.NaN) shouldBe "NaN"
      DoubleUtils.double2String(1.0) should not be "NaN"
    }
    "return default formatting within precision" in {
      val numbers = List(
        (0.0, "0"),
        (0.5, "0.5"),
        (1D, "1"),
        (5.5, "5.5"),
        (1000D, "1000"),
        (-1001D, "-1001")
      )
      for {
        (d, s) <- numbers
      } DoubleUtils.double2String(d) shouldBe s
    }
    "return decimal rounded formatting to rounded 6 significant figures" in {
      val numbers = List(
        (0.0000001, "1e-7"),
        (0.00000012345, "1.2345e-7"),
        (0.12345, "0.12345"),
        (12.345, "12.345"),
        (1234.5, "1234.5"),
        (0.55454545, "0.554545"),
        (100.55454545, "100.555"),
        (1000000.999999999D, "1e+6"),
        (77777777777.7777777, "7.77778e+10")
      )
      for {
        (d, s) <- numbers
      } DoubleUtils.double2String(d) shouldBe s
    }
  }
}
