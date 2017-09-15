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

package ai.deepsense.commons.json.envelope

import org.scalatest.{FlatSpec, Matchers}
import spray.json._

class EnvelopeSpec
  extends FlatSpec
  with Matchers
  with DefaultJsonProtocol {

  val StringLabel = "ExampleOfStringLabel"
  val WrongStringLabel = "WrongExampleOfStringLabel"
  val EnvelopeStringJsonFormat = new EnvelopeJsonFormat[String](StringLabel)

  "Envelope[String]" should "be encoded to and decoded from JSON" in {
    val exampleString = "Johny Bravo"
    val envelope = Envelope(exampleString)
    val encodedEnvelope = EnvelopeStringJsonFormat.write(envelope)
    encodedEnvelope shouldBe JsObject(StringLabel -> JsString(exampleString))
    val decodedEnvelope = EnvelopeStringJsonFormat.read(encodedEnvelope.toJson)
    decodedEnvelope.content shouldBe exampleString
  }

  "Wrongly structured Envelope[String] JSON" should "throw exception during decoding" in {
    val emptyJsonSet = JsObject()
    an[DeserializationException] should
      be thrownBy EnvelopeStringJsonFormat.read(emptyJsonSet.toJson)
  }

  "Wrongly labeled Envelope[String] JSON" should "throw exception during decoding" in {
    val wrongLabeledJson = JsObject(WrongStringLabel -> JsString(""))
    an[DeserializationException] should be thrownBy EnvelopeStringJsonFormat.read(
      wrongLabeledJson.toJson)
  }
}
