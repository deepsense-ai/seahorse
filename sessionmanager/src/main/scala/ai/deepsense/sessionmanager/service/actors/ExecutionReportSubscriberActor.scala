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

package ai.deepsense.sessionmanager.service.actors

import scala.util.Success

import akka.actor.{Actor, ActorRef, Props}

import ai.deepsense.graph.Node
import ai.deepsense.graph.nodestate.name.NodeStatusName
import ai.deepsense.models.workflows.ExecutionReport
import ai.deepsense.models.workflows.Workflow.Id
import ai.deepsense.sessionmanager.rest.responses.NodeStatusesResponse

class ExecutionReportSubscriberActor(val workflowId: Id) extends Actor {

  import ExecutionReportSubscriberActor._

  private[this] var nodeStatusById: NodeStatusMap = Map()
  private[this] var initialized: Boolean = false

  override def receive: Receive = {
    case e: ExecutionReport =>
      nodeStatusById = updateNodeStatusResponse(nodeStatusById, e)
      initialized = true
    case ReportQuery(onBehalfOf: ActorRef) =>
      onBehalfOf ! Success(generateNodeStatusResponse(nodeStatusById, initialized))
  }

}

object ExecutionReportSubscriberActor {
  type NodeStatusMap = Map[Node.Id, NodeStatusName]

  sealed trait ExecutionReportSubscriberActorMessage

  case class ReportQuery(onBehalfOf: ActorRef) extends ExecutionReportSubscriberActorMessage

  def apply(workflowId: Id): Props = Props(classOf[ExecutionReportSubscriberActor], workflowId)

  private def updateNodeStatusResponse(
      nodeStatusMap: NodeStatusMap,
      executionReport: ExecutionReport): NodeStatusMap = {
    nodeStatusMap ++ executionReport.nodesStatuses.mapValues(_.name)
  }

  // When this actor is first initialized, we don't have enough information about workflow - e.g. about nodes.
  // If we didn't handle this case, empty workflow could not be recognized from workflow for which this actor isn't
  // yet initialized.
  private def generateNodeStatusResponse(nodeStatusMap: NodeStatusMap, initialized: Boolean): NodeStatusesResponse = {
    if (initialized) {
      NodeStatusesResponse(Some(nodeStatusMap.groupBy(_._2).mapValues(_.size)))
    } else {
      NodeStatusesResponse(None)
    }
  }

}
