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

package ai.deepsense.workflowexecutor.rabbitmq

import akka.actor.ActorRef
import com.thenewmotion.akka.rabbitmq.{BasicProperties, Channel, DefaultConsumer, Envelope}

import ai.deepsense.commons.serialization.Serialization
import ai.deepsense.commons.utils.Logging
import ai.deepsense.workflowexecutor.communication.mq.MQDeserializer

case class MQSubscriber(
  subscriberActor: ActorRef,
  mqMessageDeserializer: MQDeserializer,
  channel: Channel
) extends DefaultConsumer(channel)
    with Logging
    with Serialization {

  override def handleDelivery(
      consumerTag: String,
      envelope: Envelope,
      properties: BasicProperties,
      body: Array[Byte]): Unit = {
    try {
      subscriberActor ! mqMessageDeserializer.deserializeMessage(body)
    } catch {
      case e: Exception => logger.error("Message deserialization failed", e)
    }
  }
}
