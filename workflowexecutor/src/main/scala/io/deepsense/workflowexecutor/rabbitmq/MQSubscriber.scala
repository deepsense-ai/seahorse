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

import io.deepsense.commons.serialization.Serialization
import io.deepsense.commons.utils.Logging


case class MQSubscriber(subscriberActor: SubscriberActor) extends Logging with Serialization {

  def handleDelivery(body: Array[Byte]): Unit = {
    try {
      subscriberActor.processMessage(body)
    } catch {
      case e: Exception => logger.error("Message deserialization failed", e)
    }
  }
}
