/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.graphexecutor

import scala.concurrent.Await
import scala.util.Success

import akka.actor._
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging

import io.deepsense.commons.exception.FailureCode._
import io.deepsense.commons.exception.{FailureDescription, DeepSenseFailure}
import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.graph.Node.Id
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphexecutor.GraphNodeExecutor.Messages.Start
import io.deepsense.models.entities.Entity
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.messages._

/**
 * Executes graph
 */
class GraphExecutorActor(ec: ExecutionContext, gecActorPath: String)
    extends Actor with LazyLogging {

  import GraphExecutorActor.Messages._

  var experiment: Experiment = _
  var dOperableCache: Map[Entity.Id, DOperable] = Map.empty

  var gec: ActorRef = _

  override def receive: Receive = {
    case ReadyToExecute(eid) => readyToExecute(eid)
    case Launch(e) => launch(e)
    case NodeRunning(id) => nodeRunning(id)
    case Completed(nodeId, results) => completed(nodeId, results)
    case Failed(nodeId, reason) => failed(nodeId, reason)
    case Abort(eid) => abort(eid)
  }

  def readyToExecute(eid: Experiment.Id): Unit = {
    logger.info(">>> {}", ReadyToExecute(eid))
    val selection = context.system.actorSelection(gecActorPath)
    import scala.concurrent.duration._
    implicit val timeout: Timeout = 60.second
    gec = Await.result(selection.resolveOne(), timeout.duration)
    logger.info("GEC actor resolved: {}", gecActorPath)
    gec ! ExecutorReady(eid)
  }

  def launch(e: Experiment): Unit = {
    logger.info(">>> Launch(experimentId={})", e.id)
    experiment = e.markRunning
    gec ! Success(experiment)
    logger.info("<<< Success(experimentId={})", e.id)
    ec.tenantId = experiment.tenantId
    launchReadyNodes(experiment)
  }

  def nodeRunning(id: Id): Unit = {
    logger.info(">>> {}", NodeRunning(id))
    experiment = experiment.copy(graph = experiment.graph.markAsRunning(id))
    gec ! Update(experiment)
    logger.info("<<< Update(experimentId={}) / status={}", experiment.id, experiment.state.status)
  }

  def completed(nodeId: Id, results: Map[Entity.Id, DOperable]): Unit = {
    logger.debug(s">>> Completed(nodeId=$nodeId, results=$results)")
    experiment = experiment.copy(
      graph = experiment.graph.markAsCompleted(nodeId, results.keys.toList))
    dOperableCache = dOperableCache ++ results
    gec ! Update(experiment)
    logger.debug(s"<<< NodeCompleted(experimentId=${experiment.id}, nodeId=$nodeId)")
    if (experiment.graph.readyNodes.nonEmpty) {
      logger.debug("Launching READY nodes")
      launchReadyNodes(experiment)
    } else {
      val nodesRunningCount = experiment.graph.nodes.count(_.isRunning)
      if (nodesRunningCount > 0) {
        logger.debug(s"No nodes to run, but there are still $nodesRunningCount running")
        logger.debug(s"Awaiting $nodesRunningCount RUNNINGs reporting Completed")
      } else {
        // FIXME Use become to mark the state of experiment and never change
        experiment = if (experiment.isFailed) experiment else experiment.markCompleted
        gec ! Update(experiment)
        logger.debug("<<< Update(experimentId={}) / status={}", experiment.id, experiment.state.status)
        logger.debug("Shutting down the actor system")
        context.system.shutdown()
      }
    }
  }

  def failed(nodeId: Node.Id, reason: Throwable): Unit = {

    logger.debug(">>> {}", Failed(nodeId, reason))

    experiment = experiment.markNodeFailed(nodeId, reason)
    gec ! Update(experiment)
    logger.debug("<<< Update(experimentId={}) for nodeId={}", experiment.id, nodeId)
    if (experiment.graph.readyNodes.nonEmpty) {
      logger.debug("Launching READY nodes")
      launchReadyNodes(experiment)
    } else {
      val nodesRunningCount = experiment.graph.nodes.count(_.isRunning)
      if (nodesRunningCount > 0) {
        logger.info(s"No nodes to run, but there are still $nodesRunningCount running")
        logger.info(s"Awaiting $nodesRunningCount RUNNINGs reporting Completed")
      } else {
        gec ! Update(experiment)
        logger.debug("<<< Update(experimentId={}) / status={}", experiment.id, experiment.state.status)
        logger.debug("Shutting down the actor system")
        context.system.shutdown()
      }
    }
  }

  def abort(eid: Experiment.Id): Unit = {
    logger.debug(">>> {}", Abort(eid))
    experiment = experiment.markAborted
    context.children.foreach(_ ! PoisonPill)
    gec ! Update(experiment)
    logger.debug("<<< {}", Abort(eid))
  }

  def launchReadyNodes(experiment: Experiment): Unit = {
    logger.info(">>> launchReadyNodes(experimentId={})", experiment.id)
    for (node <- experiment.graph.readyNodes) {
      logger.info("Creating actor for: {}", node)
      val nodeRef = context.actorOf(
        Props(classOf[GraphNodeExecutor], ec, node, experiment), s"node-executor-${node.id.value.toString}")
      nodeRef ! Start(experiment.graph, dOperableCache)
    }
    logger.info("<<< launchReadyNodes(experimentId={})", experiment.id)
  }
}

object GraphExecutorActor {

  def props(ec: ExecutionContext, statusReceiverActorPath: String) =
    Props(classOf[GraphExecutorActor], ec, statusReceiverActorPath)

  object Messages {

    sealed trait Message

    case class ReadyToExecute(experimentId: Experiment.Id) extends Message

    case class NodeRunning(nodeId: Node.Id) extends Message

    // FIXME Why would GEA return a map not result of the computation itself?
    case class Completed(nodeId: Node.Id, results: Map[Entity.Id, DOperable]) extends Message
    case class Failed(nodeId: Node.Id, reason: Throwable) extends Message

  }
}
