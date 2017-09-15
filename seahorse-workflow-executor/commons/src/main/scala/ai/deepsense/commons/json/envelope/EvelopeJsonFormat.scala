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

import spray.json._

/**
 * JSON decoder for Envelope[T] objects
 */
case class EnvelopeJsonWriter[T : JsonWriter](private val label: String)
    extends RootJsonWriter[Envelope[T]] {
  override def write(e: Envelope[T]): JsValue = EnvelopeJsonConversions.write[T](label)(e)
}

/**
 * JSON encoder for Envelope[T] objects
 */
case class EnvelopeJsonReader[T : JsonReader](private val label: String)
    extends RootJsonReader[Envelope[T]] {
  override def read(json: JsValue): Envelope[T] = EnvelopeJsonConversions.read[T](label)(json)
}

/**
 * JSON encoder and decoder for Envelope[T] objects
 */
case class EnvelopeJsonFormat[T : JsonFormat](private val label: String)
    extends RootJsonFormat[Envelope[T]] {
  override def read(json: JsValue): Envelope[T] = EnvelopeJsonConversions.read[T](label)(json)
  override def write(e: Envelope[T]): JsValue = EnvelopeJsonConversions.write[T](label)(e)
}

/**
 * Implementation of Envelope[T] objects encoding and decoding
 */
private object EnvelopeJsonConversions {
  def read[T : JsonReader](label: String)(json: JsValue): Envelope[T] = json match {
    case JsObject(fields) if fields.size == 1 && fields.contains(label) =>
      Envelope(fields.head._2.convertTo[T])
    case _ => deserializationError(s"Expected envelope with '$label' label, but got $json")
  }
  def write[T : JsonWriter](label: String)(e: Envelope[T]): JsValue =
    JsObject(label -> e.content.toJson)
}
