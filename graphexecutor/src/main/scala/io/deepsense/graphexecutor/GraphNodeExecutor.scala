/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.graphexecutor

import java.util.UUID

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, duration}

import com.typesafe.scalalogging.LazyLogging

import io.deepsense.commons.metrics.Instrumented
import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.entitystorage.EntityStorageClient
import io.deepsense.graph.{Graph, Node}
import io.deepsense.models.entities.{DataObjectReference, InputEntity}

/**
 * GraphNodeExecutor is responsible for execution of single node.
 * It requires that this node has state of RUNNING and performs its execution,
 * changes to state COMPLETED (on success) or FAILED (on fail)
 * and finally notifies GraphExecutor of finished execution.
 *
 * NOTE: This class probably will have to be thoroughly modified when first DOperation is prepared.
 */
class GraphNodeExecutor(
    executionContext: ExecutionContext,
    graphExecutor: GraphExecutor,
    node: Node,
    entityStorageClient: EntityStorageClient)
  extends Runnable with LazyLogging with Instrumented {

  implicit val entityStorageResponseDelay = new FiniteDuration(5L, duration.SECONDS)
  import scala.concurrent.ExecutionContext.Implicits.global

  private val createEntityTimer = metrics.timer("createEntity")
  private val calculateReportTimer = metrics.timer("calculateReport")
  private val executeOperationTimer = metrics.timer("executeOperation")
  private val nodeDescription = s"'${node.operation.name}-${node.id}'"

  override def run(): Unit = {
    val executionStart = System.currentTimeMillis()
    try {
      logger.info("Execution of node starts: {}", nodeDescription)
      graphExecutor.graphGuard.synchronized {
        logger.debug("In graphGuard synchronized for {}", nodeDescription)
        require(node.isRunning)
      }

      logger.debug("Collecting data for operation input ports for {}", nodeDescription)
      val collectedOutput = graphExecutor.graphGuard.synchronized {
        collectOutputs(graphExecutor.graph.get, graphExecutor.dOperableCache)
      }
      logger.debug("Data for input ports collected for {}", nodeDescription)

      logger.debug("Executing operation {}", nodeDescription)
      val resultVector = executeOperation(collectedOutput)
      logger.debug("Operation executed (without reports) for {}", nodeDescription)

      logger.debug("Registering data from operation output ports for {}", nodeDescription)
      graphExecutor.graphGuard.synchronized {
        logger.debug("In graphExecutor.graphGuard.synchronized section for {}", nodeDescription)
        graphExecutor.experiment = Some(graphExecutor.experiment.get.copy(graph =
          graphExecutor.graph.get.markAsCompleted(
            node.id,
            resultVector.map(dOperable => {
              val uuid = storeAndRegister(dOperable)
              graphExecutor.dOperableCache.put(uuid, dOperable)
              uuid
            }).toList)))
        logger.debug("Data registered for {}", nodeDescription)
      }
    } catch {
      case e: Exception => {
        logger.error(s"${node.id} Graph execution failed $e", e)
        // Exception thrown here, during handling exception could result in application deadlock
        // (node stays in status RUNNING forever, Graph Executor waiting for this status change)
        graphExecutor.graphGuard.synchronized {
          // TODO: Exception should be relayed to graph
          // TODO: To decision: exception in single node should result in abortion of:
          // (current) only descendant nodes of failed node? / only queued nodes? / all other nodes?

          // TODO Include ERROR in Node's status instead of Experiment's status
          val errorMessage = graphExecutor.experiment.get.state.error
            .map(previous => s"$previous, \n${node.id}:  ${e.toString}")
            .getOrElse(e.toString)
          graphExecutor.experiment =
            Some(graphExecutor.experiment.get
              .copy(graph = graphExecutor.graph.get.markAsFailed(node.id))
              .markFailed(errorMessage))
        }
      }
    } finally {
      // Exception thrown here could result in slightly delayed graph execution
      val duration = (System.currentTimeMillis() - executionStart) / 1000.0
      logger.info("{} Execution of node ends (duration: {} seconds)",
        nodeDescription, duration.toString)
      graphExecutor.nodeTiming.put(nodeDescription, duration)
      graphExecutor.graphEventBinarySemaphore.release()
    }
  }

  /**
   * Returns Vector of DOperable's to pass to current node as DOperation arguments.
   * NOTE: Currently we do not support optional input ports.
   * @param graph graph of operations to execute (contains current node)
   * @param dOperableCache mutable map UUID -> DOperable
   * @return Vector of DOperable's to pass to current node
   */
  def collectOutputs(
      graph: Graph,
      dOperableCache: mutable.Map[UUID, DOperable]): Vector[DOperable] = {
    var collectedOutputs = Vector.empty[DOperable]
    // Iterate through predecessors, constructing Vector of DOperable's
    // (predecessors are ordered by current node input port number connected with them)
    for (predecessorEndpoint <- graph.predecessors.get(node.id).get) {
      // NOTE: Currently we do not support optional input ports
      // (require assures that all ports are obligatory)
      require(predecessorEndpoint.nonEmpty)
      val nodeId = predecessorEndpoint.get.nodeId
      val portIndex = predecessorEndpoint.get.portIndex
      collectedOutputs = collectedOutputs ++ dOperableCache.get(
        graph.node(nodeId).state.results.get(portIndex))
    }
    collectedOutputs
  }

  private def executeOperation(inputVector: Vector[DOperable]): Vector[DOperable] = {
    logger.debug("{} inputVector.size = {}", nodeDescription, inputVector.size.toString)
    val resultVector = executeOperationTimer.time {
      node.operation.execute(executionContext)(inputVector)
    }
    logger.debug("{} resultVector.size = {}", nodeDescription, resultVector.size.toString)
    resultVector
  }

  private def storeAndRegister(dOperable: DOperable): UUID = {
    logger.debug("storeAndRegister started for {}", nodeDescription)

    val experiment = graphExecutor.experiment.get
    val inputEntity = InputEntity(
      tenantId = experiment.tenantId,
      name = dOperable.getClass.toString,
      description = s"Output from Operation: ${nodeDescription}",
      dClass = dOperable.getClass.toString,
      data = dOperable.url.map(DataObjectReference),
      report = Some(calculateReportTimer.time {
        dOperable.report.toDataObjectReport
      }),
      saved = false
    )

    logger.debug("createEntity started for {}", nodeDescription)
    val result = createEntityTimer.time {
      Await.result(
        entityStorageClient.createEntity(inputEntity).map(_.id),
        entityStorageResponseDelay).value
    }
    logger.debug("createEntity finished for {}", nodeDescription)
    logger.debug("storeAndRegister finished for {}", nodeDescription)
    result
  }
}
