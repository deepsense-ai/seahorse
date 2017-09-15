/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import scala.concurrent.Await

import akka.actor.{Actor, PoisonPill}
import com.typesafe.scalalogging.LazyLogging

import io.deepsense.commons.metrics.Instrumented
import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphexecutor.GraphExecutorActor.Messages.{NodeFinished, NodeStarted}
import io.deepsense.graphexecutor.GraphExecutorActor.Results
import io.deepsense.models.entities.{DataObjectReference, Entity, CreateEntityRequest}
import io.deepsense.models.experiments.Experiment

/**
 * GraphNodeExecutorActor is responsible for execution of single node.
 * It requires that this node has state of RUNNING.
 * Actor performs its execution, changes to state COMPLETED (on success)
 * or FAILED (on fail) and finally notifies GraphExecutor of finished execution.
 */
class GraphNodeExecutorActor(
    executionContext: ExecutionContext,
    node: Node,
    experiment: Experiment,
    dOperableCache: Results)
  extends Actor with LazyLogging with Instrumented {

  import scala.concurrent.duration._

  implicit val entityStorageResponseDelay = 5.seconds

  import io.deepsense.graphexecutor.GraphNodeExecutorActor.Messages._

  lazy val nodeDescription = s"'${node.operation.name}-${node.id}'"
  var executionStart: Long = _

  override def receive: Receive = {
    case Start() =>
      executionStart = System.currentTimeMillis()
      logger.info(">>> Start(node={})", node.id)
      val msg = NodeStarted(node.id)
      // FIXME Duplication to GEA's NodeStarted message handling
      val runningNode = node.markRunning
      sender ! msg
      logger.info("<<< {}", msg)

      logger.debug("Collecting data for operation input ports for {}", nodeDescription)
      val collectedOutput = collectOutputs(experiment.graph, dOperableCache)
      logger.debug("Executing operation {}", nodeDescription)

      try {
        val resultVector = executeOperation(collectedOutput)
        logger.debug("Operation executed (without reports): {}", resultVector)

        logger.info(s"${runningNode.id} Registering data from operation output ports")
        val results: Map[Entity.Id, DOperable] = resultVector.map { dOperable =>
          val uuid = storeAndRegister(dOperable)
          uuid -> dOperable
        }.toMap
        logger.debug("Data registered for {}: results={}", nodeDescription, results)
        val finished = NodeFinished(runningNode.markCompleted(results.keys.toSeq), results) // TODO
        sender ! finished
        logger.info("<<< {}", finished)
      } catch {
        case e: Throwable =>
          logger.error(s"[nodeId: ${runningNode.id}] Graph execution failed", e)
          val failed = NodeFinished(runningNode.markFailed(e), results = Map.empty)
          sender ! failed
          logger.info("<<< {}", failed)
      } finally {
        // Exception thrown here could result in slightly delayed graph execution
        val duration = (System.currentTimeMillis() - executionStart) / 1000.0
        logger.info("{} Execution of node ends (duration: {} seconds)",
          nodeDescription, duration.toString)
        self ! PoisonPill
      }
  }

  /**
   * Returns Vector of DOperable's to pass to current node as DOperation arguments.
   * NOTE: Currently we do not support optional input ports.
   * @param graph graph of operations to execute (contains current node)
   * @param dOperableCache map UUID -> DOperable
   * @return Vector of DOperable's to pass to current node
   */
  def collectOutputs(
    graph: Graph,
    dOperableCache: Results): Vector[DOperable] = {
    // Iterate through predecessors, constructing Vector of DOperable's
    // (predecessors are ordered by current node input port number connected with them)
    val result = for {predecessorEndpoint <- graph.predecessors.get(node.id).get} yield {
      // NOTE: Currently we do not support optional input ports
      // (require assures that all ports are obligatory)
      require(predecessorEndpoint.nonEmpty)
      val nodeId = predecessorEndpoint.get.nodeId
      val portIndex = predecessorEndpoint.get.portIndex
      dOperableCache(graph.node(nodeId).state.results.get(portIndex))
    }
    result.toVector
  }

  private def executeOperation(inputVector: Vector[DOperable]): Vector[DOperable] = {
    logger.debug("{} inputVector.size = {}", nodeDescription, inputVector.size.toString)
    val resultVector = node.operation.execute(executionContext)(inputVector)
    logger.debug("{} resultVector.size = {}", nodeDescription, resultVector.size.toString)
    resultVector
  }

  private def storeAndRegister(dOperable: DOperable): Entity.Id = {
    logger.debug("storeAndRegister started for {}", nodeDescription)

    val inputEntity = CreateEntityRequest(
      tenantId = experiment.tenantId,
      name = dOperable.getClass.toString,
      description = s"Output from Operation: $nodeDescription",
      dClass = dOperable.getClass.toString,
      dataReference = dOperable.url.map(DataObjectReference),
      report = dOperable.report.toDataObjectReport,
      saved = false
    )

    logger.debug("createEntity started")
    val result: Entity.Id = Await.result(
      executionContext.entityStorageClient.createEntity(inputEntity),
      entityStorageResponseDelay).value
    logger.debug("createEntity finished for {}", nodeDescription)
    logger.debug("storeAndRegister finished for {}", nodeDescription)
    result
  }
}

object GraphNodeExecutorActor {

  object Messages {
    sealed trait Message
    case class Start() extends Message
  }

}
