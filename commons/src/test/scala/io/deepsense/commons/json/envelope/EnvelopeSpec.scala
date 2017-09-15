/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.json.envelope

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
