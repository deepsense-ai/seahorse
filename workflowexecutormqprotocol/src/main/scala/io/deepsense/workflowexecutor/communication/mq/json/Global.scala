/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.workflowexecutor.communication.mq.json

import java.nio.charset.Charset

import io.deepsense.workflowexecutor.communication.message.global._
import io.deepsense.workflowexecutor.communication.message.global.HeartbeatJsonProtocol._
import io.deepsense.workflowexecutor.communication.message.global.PoisonPillJsonProtocol._
import io.deepsense.workflowexecutor.communication.message.global.ReadyJsonProtocol._

object Global {
  val charset = Charset.forName("UTF-8")

  import Constants.MessagesTypes._

  object HeartbeatDeserializer extends DefaultJsonMessageDeserializer[Heartbeat](heartbeat)
  object HeartbeatSerializer extends DefaultJsonMessageSerializer[Heartbeat](heartbeat)

  object PoisonPillDeserializer extends DefaultJsonMessageDeserializer[PoisonPill](poisonPill)
  object PoisonPillSerializer extends DefaultJsonMessageSerializer[PoisonPill](poisonPill)

  object ReadyDeserializer extends DefaultJsonMessageDeserializer[Ready](ready)
  object ReadySerializer extends DefaultJsonMessageSerializer[Ready](ready)

  object GlobalMQSerializer extends JsonMQSerializer(
    Seq(HeartbeatSerializer, PoisonPillSerializer, ReadySerializer))

  object GlobalMQDeserializer extends JsonMQDeserializer(
    Seq(HeartbeatDeserializer, PoisonPillDeserializer, ReadyDeserializer))
}
