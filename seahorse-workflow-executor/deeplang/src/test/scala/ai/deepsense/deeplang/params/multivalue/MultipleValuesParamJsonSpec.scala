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

package ai.deepsense.deeplang.params.multivalue

import spray.json._

import ai.deepsense.deeplang.UnitSpec

class MultipleValuesParamJsonSpec extends UnitSpec with BasicFormats {

  "MultipleValuesParam" should {
    "deserialize from json" in {
      val gridParam = MultipleValuesParam.fromJson[Int](json)
      gridParam.values shouldBe Seq(1, 2, 3, 4, 5)
    }
  }

  val json = JsObject(
    "values" -> JsArray(
      JsObject(
        "type" -> JsString("seq"),
        "value" -> JsObject(
          "sequence" -> JsArray(JsNumber(1), JsNumber(2.0), JsNumber(3), JsNumber(4)))),
      JsObject(
        "type" -> JsString("seq"),
        "value" -> JsObject(
          "sequence" -> JsArray(JsNumber(4), JsNumber(5.0))))
    )
  )
}
