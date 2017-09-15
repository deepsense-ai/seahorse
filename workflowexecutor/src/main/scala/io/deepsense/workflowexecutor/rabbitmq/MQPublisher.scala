/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.workflowexecutor.rabbitmq

import java.nio.charset.Charset

import akka.actor.ActorRef
import com.rabbitmq.client.Channel
import com.thenewmotion.akka.rabbitmq.ChannelMessage

import io.deepsense.commons.utils.Logging
import io.deepsense.workflowexecutor.communication.WriteMessageMQ

/**
  * Class used to publish data to exchange under given topic.
  * @param exchange name of the Exchange
  * @param publisherActor created by rabbitmq
  */
case class MQPublisher(exchange: String, publisherActor: ActorRef)
  extends Logging {

  def publish(topic: String, message: WriteMessageMQ): Unit = {
    val data: Array[Byte] = message.toJsonObject.compactPrint.getBytes(Charset.forName("UTF-8"))
    publisherActor  ! ChannelMessage(publish(topic, data), dropIfNoChannel = false)
  }

  private def publish(topic: String, data: Array[Byte])(channel: Channel): Unit = {
    channel.basicPublish(exchange, topic, null, data)
  }
}
