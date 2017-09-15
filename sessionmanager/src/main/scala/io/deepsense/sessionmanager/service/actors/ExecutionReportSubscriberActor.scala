/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.actors

import scala.util.Success

import akka.actor.{Actor, ActorRef, Props}

import io.deepsense.graph.Node
import io.deepsense.graph.nodestate.name.NodeStatusName
import io.deepsense.models.workflows.ExecutionReport
import io.deepsense.models.workflows.Workflow.Id
import io.deepsense.sessionmanager.rest.responses.NodeStatusesResponse

class ExecutionReportSubscriberActor(val workflowId: Id) extends Actor {
  import ExecutionReportSubscriberActor._

  private var nodeStatusById: NodeStatusMap = createNodeStatusMap

  override def receive: Receive = {
    case e: ExecutionReport => nodeStatusById = updateNodeStatusResponse(nodeStatusById, e)
    case ReportQuery(onBehalfOf: ActorRef) => onBehalfOf ! Success(generateNodeStatusRespone(nodeStatusById))
  }

}

object ExecutionReportSubscriberActor {
  type NodeStatusMap = Map[Node.Id, NodeStatusName]

  sealed trait ExecutionReportSubscriberActorMessage

  case class ReportQuery(onBehalfOf: ActorRef) extends ExecutionReportSubscriberActorMessage

  def apply(workflowId: Id): Props = Props(classOf[ExecutionReportSubscriberActor], workflowId)

  val createNodeStatusMap: NodeStatusMap = Map()

  def updateNodeStatusResponse(
      nodeStatusMap: NodeStatusMap,
      executionReport: ExecutionReport): NodeStatusMap = {
    nodeStatusMap ++ executionReport.nodesStatuses.mapValues(_.name)
  }

  def generateNodeStatusRespone(nodeStatusMap: NodeStatusMap): NodeStatusesResponse = {
    NodeStatusesResponse(nodeStatusMap.groupBy(_._2).mapValues(_.size))
  }
}
