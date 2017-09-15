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

import spray.json.JsObject

import ai.deepsense.workflowexecutor.communication.mq.MQSerializer

class JsonMQSerializer(
  jsonSerializers: Seq[JsonMessageSerializer],
  parent: Option[JsonMQSerializer] = None
) extends MQSerializer with JsonMessageSerializer {

  private val combinedJsonSerializers = {
    jsonSerializers.tail.foldLeft(jsonSerializers.head.serialize) {
      case (acc, serializer) =>
        acc.orElse(serializer.serialize)
    }
  }

  override val serialize: PartialFunction[Any, JsObject] = {
    parent match {
      case Some(p) => combinedJsonSerializers.orElse(p.serialize)
      case None => combinedJsonSerializers
    }
  }

  override def serializeMessage(message: Any): Array[Byte] = {
    serialize(message).compactPrint.getBytes(Global.charset)
  }

  def orElse(next: JsonMQSerializer): JsonMQSerializer =
    new JsonMQSerializer(jsonSerializers, Some(next))
}
