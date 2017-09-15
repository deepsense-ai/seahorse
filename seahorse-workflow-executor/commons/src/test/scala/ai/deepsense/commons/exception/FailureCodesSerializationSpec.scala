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

package ai.deepsense.commons.exception

import ai.deepsense.commons.StandardSpec
import ai.deepsense.commons.exception.FailureCode.FailureCode
import ai.deepsense.commons.serialization.Serialization

class FailureCodesSerializationSpec extends StandardSpec with Serialization {
  "FailureCode" should {
    "serialize and deserialize" in {
      val code = FailureCode.UnexpectedError
      val serialized = serialize(code)
      val deserialized = deserialize[FailureCode](serialized)
      deserialized shouldBe code
    }
  }
}
