/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import scala.concurrent.Await
import scala.util.Success

import akka.actor._
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging

import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.graph.Node
import io.deepsense.graphexecutor.GraphExecutorActor.Results
import io.deepsense.models.entities.Entity
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.messages._

class GraphExecutorActor(
    executionContext: ExecutionContext,
    clientActorPath: String)
  extends Actor with LazyLogging {

  this: GraphNodeExecutorFactory with SystemShutdowner =>


  import io.deepsense.graphexecutor.GraphExecutorActor.Messages._

  var experiment: Experiment = _
  var dOperableCache: Results = Map.empty
  var startedNodes: Set[Node.Id] = Set()

  var clientActor: ActorRef = _

  override def receive: Receive = {
    case Start(eid) => start(eid)
    case Launch(e) => launch(e)
    case Abort(eid) => endExecution()
    case NodeStarted(id) => nodeStarted(id)
    case NodeFinished(node, results) => nodeFinished(node, results)
  }

  def start(eid: Experiment.Id): Unit = {
    logger.info(">>> {}", Start(eid))
    val selection = context.system.actorSelection(clientActorPath)
    import scala.concurrent.duration._
    implicit val timeout: Timeout = 60.second // TODO move to configuration
    clientActor = Await.result(selection.resolveOne(), timeout.duration)
    logger.info("GEC actor resolved: {}", clientActorPath)
    clientActor ! ExecutorReady(eid)
  }

  def launch(e: Experiment): Unit = {
    logger.info(">>> Launch(experimentId={})", e.id)
    experiment = e.markRunning
    executionContext.tenantId = experiment.tenantId
    launchReadyNodes(experiment)
  }

  def nodeStarted(id: Node.Id): Unit = {
    logger.info(">>> {}", NodeStarted(id))
    experiment = experiment.markNodeRunning(id)
    clientActor ! Update(experiment)
    logger.info("<<< Update(experimentId={}) / status={}", experiment.id, experiment.state.status)
  }

  def nodeFinished(node: Node, results: Results): Unit = {
    assert(node.isFailed || node.isCompleted)
    logger.info(s">>> nodeFinished(node=$node)")
    experiment = experiment.withNode(node)

    dOperableCache = dOperableCache ++ results
    logger.debug(s"<<< NodeCompleted(experimentId=${experiment.id}, nodeId=${node.id})")
    if (experiment.readyNodes.nonEmpty) {
      launchReadyNodes(experiment)
      clientActor ! Update(experiment)
    } else {
      val nodesRunningCount = experiment.runningNodes.size
      if (nodesRunningCount > 0) {
        logger.debug(s"No nodes to run, but there are still $nodesRunningCount running")
        logger.debug(s"Awaiting $nodesRunningCount RUNNINGs reporting Completed")
        clientActor ! Update(experiment)
      } else {
        endExecution()
      }
    }
  }

  def launchReadyNodes(experiment: Experiment): Unit = {
    logger.info(">>> launchReadyNodes(experimentId={})", experiment.id)
    for {
      node <- experiment.readyNodes
      if !startedNodes.contains(node.id)
    } {
      val props = Props(createGraphNodeExecutor(executionContext, node, experiment, dOperableCache))
      val nodeRef = context.actorOf(props, s"node-executor-${node.id.value.toString}")
      nodeRef ! GraphNodeExecutorActor.Messages.Start()
    }
    startedNodes = startedNodes ++ experiment.readyNodes.map(_.id)
    logger.info("<<< launchReadyNodes(experimentId={})", experiment.id)
  }

  def endExecution(): Unit = {
    experiment = experiment.updateState()
    clientActor ! Update(experiment)
    logger.debug("<<< Update(experimentId={}) / status={}", experiment.id, experiment.state.status)
    logger.debug("Shutting down the actor system")
    context.become(ignoreAllMessages)
    shutdownSystem
  }

  def ignoreAllMessages: Receive = {
    case x => logger.info(s"Received message $x after being aborted - ignoring")
  }
}

object GraphExecutorActor {
  def props(ec: ExecutionContext, statusReceiverActorPath: String): Props =
    Props(new GraphExecutorActor(ec, statusReceiverActorPath)
      with ProductionGraphNodeExecutorFactory
      with ProductionSystemShutdowner)

  type Results = Map[Entity.Id, DOperable]

  object Messages {
    sealed trait Message
    case class Start(experimentId: Experiment.Id) extends Message
    case class NodeStarted(nodeId: Node.Id) extends Message
    case class NodeFinished(node: Node, results: Results) extends Message
  }
}

trait GraphNodeExecutorFactory {
  def createGraphNodeExecutor(
      executionContext: ExecutionContext,
      node: Node,
      experiment: Experiment,
      dOperableCache: Results): Actor
}

trait ProductionGraphNodeExecutorFactory extends GraphNodeExecutorFactory {

  override def createGraphNodeExecutor(
      executionContext: ExecutionContext,
      node: Node,
      experiment: Experiment,
      dOperableCache: Results): Actor =
    new GraphNodeExecutorActor(executionContext, node, experiment, dOperableCache)
}

trait SystemShutdowner {
  def shutdownSystem: Unit
}

trait ProductionSystemShutdowner extends SystemShutdowner {
  val context: ActorContext
  def shutdownSystem: Unit = context.system.shutdown()
}
