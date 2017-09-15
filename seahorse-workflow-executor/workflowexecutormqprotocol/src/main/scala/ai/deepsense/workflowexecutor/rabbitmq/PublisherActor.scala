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

import akka.actor.{Actor, Props}

import ai.deepsense.commons.utils.Logging

class PublisherActor(topic: String, publisher: MQPublisher) extends Actor with Logging {

  override def receive: Receive = {
    case message: Any =>
      logger.info(
        "PublisherActor for topic: {} receives message {} from '{}'",
        topic,
        message.getClass.getName,
        sender().path.name)
      publisher.publish(topic, message)
  }
}

object PublisherActor {
  def props(topic: String, publisher: MQPublisher): Props = {
    Props(new PublisherActor(topic, publisher))
  }
}
