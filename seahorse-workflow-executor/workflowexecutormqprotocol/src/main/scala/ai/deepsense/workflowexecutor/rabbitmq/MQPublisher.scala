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
import com.thenewmotion.akka.rabbitmq.{Channel, ChannelMessage}

import ai.deepsense.commons.utils.Logging
import ai.deepsense.workflowexecutor.communication.mq.MQSerializer

/**
  * Class used to publish data to exchange under given topic.
 *
  * @param exchange name of the Exchange
  * @param messageSerializer implementation of MessageMQSerializer that is able to serialize
  *                          all messages published using this publisher
  * @param publisherActor created by rabbitmq
  */
case class MQPublisher(
    exchange: String,
    messageSerializer: MQSerializer,
    publisherActor: ActorRef)
  extends Logging {

  def publish(topic: String, message: Any): Unit = {
    val data: Array[Byte] = messageSerializer.serializeMessage(message)
    publisherActor  ! ChannelMessage(publish(topic, data), dropIfNoChannel = false)
  }

  private def publish(topic: String, data: Array[Byte])(channel: Channel): Unit = {
    channel.basicPublish(exchange, topic, null, data)
  }
}
