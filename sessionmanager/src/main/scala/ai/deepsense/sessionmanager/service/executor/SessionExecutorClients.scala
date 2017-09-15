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

package ai.deepsense.sessionmanager.service.executor

import scala.collection.mutable

import com.google.inject.Inject

import ai.deepsense.commons.utils.Logging
import ai.deepsense.models.workflows.Workflow
import ai.deepsense.workflowexecutor.communication.mq.MQCommunication
import ai.deepsense.workflowexecutor.rabbitmq.MQCommunicationFactory

class SessionExecutorClients @Inject() (private val communicationFactory: MQCommunicationFactory) extends Logging {
  private val clients = mutable.Map[Workflow.Id, SessionExecutorClient]()

  def launchWorkflow(id: Workflow.Id): Unit = get(id).launchWorkflow()

  def sendPoisonPill(id: Workflow.Id): Unit = get(id).sendPoisonPill()

  private def get(id: Workflow.Id): SessionExecutorClient = {
    clients.get(id) match {
      case Some(client) => client
      case None =>
        val client = create(id)
        clients.put(id, client)
        client
    }
  }

  private def create(id: Workflow.Id): SessionExecutorClient = {
    val key = s"workflow.${id.toString}.sm.from"
    logger.debug(s"Creating SessionExecutorClient for $key")
    val publisher = communicationFactory.createPublisher(
      s"workflow.${id.toString}.sm.from",
      MQCommunication.Actor.Publisher.workflow(id))
    new SessionExecutorClient(id, publisher)
  }
}
