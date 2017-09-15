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

import akka.actor.Actor

import io.deepsense.commons.utils.Logging
import io.deepsense.workflowexecutor.communication.{MQCommunication, ExecutionStatus, WriteMessageMQ}

class PublisherActor(publisher: MQPublisher) extends Actor with Logging {

  override def receive: Receive = {
    case publishMessage: PublishMessage =>
      publisher.publish(publishMessage.topic, publishMessage.messageMQ)
    case executionStatus: ExecutionStatus =>
      logger.info(s"PublisherActor recv status: $executionStatus")
      publisher.publish(MQCommunication.editorTopic, executionStatus)
  }
}

case class PublishMessage(topic: String, messageMQ: WriteMessageMQ)
