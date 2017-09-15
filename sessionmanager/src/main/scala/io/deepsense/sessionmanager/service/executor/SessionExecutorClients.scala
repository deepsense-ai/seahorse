/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.executor

import scala.collection.mutable

import com.google.inject.Inject

import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowexecutor.communication.mq.MQCommunication
import io.deepsense.workflowexecutor.rabbitmq.MQCommunicationFactory

class SessionExecutorClients @Inject() (private val communicationFactory: MQCommunicationFactory) {
  private val clients = mutable.Map[Workflow.Id, SessionExecutorClient]()

  def get(id: Workflow.Id): SessionExecutorClient = {
    clients.get(id) match {
      case Some(client) => client
      case None =>
        val client = create(id)
        clients.put(id, client)
        client
    }
  }

  private def create(id: Workflow.Id): SessionExecutorClient = {
    val publisher = communicationFactory.createPublisher(
      s"workflow.${id.toString}.sm.from",
      MQCommunication.Actor.Publisher.workflow(id))
    new SessionExecutorClient(publisher)
  }
}
