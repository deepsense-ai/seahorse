/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowexecutor.communication.mq.json

import java.nio.charset.Charset

import spray.json._

import ai.deepsense.workflowexecutor.communication.mq.MQDeserializer
import ai.deepsense.workflowexecutor.communication.mq.json.Constants.JsonKeys._

class JsonMQDeserializer(
    jsonDeserializers: Seq[JsonMessageDeserializer],
    parent: Option[JsonMQDeserializer] = None)
  extends MQDeserializer with JsonMessageDeserializer {

  private val combinedJsonDeserializers = {
    jsonDeserializers.tail.foldLeft(jsonDeserializers.head.deserialize) {
      case (acc, deserializer) =>
        acc.orElse(deserializer.deserialize)
    }
  }

  override val deserialize: PartialFunction[(String, JsObject), Any] = {
    parent match {
      case Some(p) => combinedJsonDeserializers.orElse(p.deserialize)
      case None => combinedJsonDeserializers
    }
  }

  override def deserializeMessage(data: Array[Byte]): Any = {
    val json = new String(data, Global.charset).parseJson
    val jsObject = json.asJsObject
    val fields = jsObject.fields
    import spray.json.DefaultJsonProtocol._
    val messageType = getField(fields, messageTypeKey).convertTo[String]
    val body = getField(fields, messageBodyKey).asJsObject()
    deserialize(messageType, body)
  }

  def orElse(next: JsonMQDeserializer): JsonMQDeserializer =
    new JsonMQDeserializer(jsonDeserializers, Some(next))

  private def getField(fields: Map[String, JsValue], fieldName: String): JsValue = {
    try {
      fields(fieldName)
    } catch {
      case e: NoSuchElementException =>
        throw new DeserializationException(s"Missing field: $fieldName", e)
    }
  }
}
