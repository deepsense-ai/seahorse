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

package io.deepsense.workflowexecutor.communication.mq

import io.deepsense.models.workflows.Workflow

object MQCommunication {
  val mqActorSystemName = "rabbitmq"

  def subscriberName(topic: String): String = s"${topic}_subscriber"
  def publisherName(topic: String): String = s"${topic}_publisher"
  def queueName(topic: String): String = s"${topic}_to_executor"

  object Actor {

    object Publisher {
      val seahorse = prefixedName("seahorse")
      def notebook(id: Workflow.Id): String = prefixedName(s"notebook_$id")
      def heartbeat(id: Workflow.Id): String = prefixedName(s"heartbeat_$id")
      def heartbeatAll: String = prefixedName(s"heartbeat_all")
      def ready(id: Workflow.Id): String = prefixedName(s"ready_$id")
      def workflow(id: Workflow.Id): String = prefixedName(id.toString)
      private def prefixedName = name("publisher") _
    }

    object Subscriber {
      val seahorse = prefixedName("seahorse")
      val notebook = prefixedName("notebook")
      val workflows: String = prefixedName("workflows")
      private def prefixedName = name("subscriber") _
    }
    private[this] def name(prefix: String)(suffix: String): String = s"${prefix}_$suffix"
  }

  object Exchange {
    val seahorse = "seahorse"

    def heartbeats(workflowId: Workflow.Id): String = s"${seahorse}_heartbeats_$workflowId"
    def heartbeatsAll: String = s"${seahorse}_heartbeats_all"
    def ready(workflowId: Workflow.Id): String = s"${seahorse}_ready_$workflowId"
  }

  object Topic {
    private val workflowPrefix = "workflow"
    private val notebook = "notebook"
    private val kernelManager = "kernelmanager"
    def allWorkflowsSubscriptionTopic(sessionId: String): String =
      subscriptionTopic(s"$workflowPrefix.$sessionId.*")
    def seahorsePublicationTopic(sessionId: String): String =
      publicationTopic(s"seahorse.$sessionId")
    val notebookSubscriptionTopic = subscriptionTopic(notebook)
    val notebookPublicationTopic = publicationTopic(notebook)
    def kernelManagerSubscriptionTopic(id: Workflow.Id, sessionId: String): String =
      subscriptionTopic(kernelManagerTopic(id, sessionId))
    def workflowPublicationTopic(id: Workflow.Id, sessionId: String): String =
      publicationTopic(workflowTopic(id, sessionId))
    private def kernelManagerTopic(workflowId: Workflow.Id, sessionId: String): String =
      s"$kernelManager.$sessionId.${workflowId.toString}"
    private def workflowTopic(workflowId: Workflow.Id, sessionId: String): String =
      s"$workflowPrefix.$sessionId.${workflowId.toString}"
    private def subscriptionTopic(topic: String): String = s"$topic.from"
    private def publicationTopic(topic: String): String = s"$topic.to"
  }
}
