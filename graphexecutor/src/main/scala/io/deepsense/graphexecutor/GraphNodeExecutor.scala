/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.graphexecutor

import java.util.UUID

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, duration}

import com.typesafe.scalalogging.LazyLogging

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
 * TODO: Currently it is impossible to debug GE without println
 */
class GraphNodeExecutor(
    executionContext: ExecutionContext,
    graphExecutor: GraphExecutor,
    node: Node,
    entityStorageClient: EntityStorageClient)
  extends Runnable with LazyLogging {

  implicit val entityStorageResponseDelay = new FiniteDuration(5L, duration.SECONDS)
  import scala.concurrent.ExecutionContext.Implicits.global

  override def run(): Unit = {
    val executionStart = System.currentTimeMillis()
    try {
      logger.info(s"${node.id} Execution of node starts")
      graphExecutor.graphGuard.synchronized {
        require(node.isRunning)
      }

      logger.info(s"${node.id} Collecting data for operation input ports")
      val collectedOutput = graphExecutor.graphGuard.synchronized {
        collectOutputs(graphExecutor.graph.get, graphExecutor.dOperableCache)
      }

      logger.info(s"${node.id} Executing operation")
      val resultVector = executeOperation(collectedOutput)

      logger.info(s"${node.id}) Registering data from operation output ports")
      graphExecutor.graphGuard.synchronized {
        graphExecutor.experiment = Some(graphExecutor.experiment.get.copy(graph =
          graphExecutor.graph.get.markAsCompleted(
            node.id,
            resultVector.map(dOperable => {
              val uuid = storeAndRegister(dOperable)
              graphExecutor.dOperableCache.put(uuid, dOperable)
              uuid
            }).toList)))
      }
    } catch {
      case e: Exception => {
        logger.error(s"${node.id} Graph execution failed $e")
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
      logger.info(s"${node.id} Execution of node ends (duration: $duration seconds)")
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
    logger.info(s"${node.id} node.operation.name = ${node.operation.name}")
    logger.info(s"${node.id} inputVector.size = ${inputVector.size}")
    val resultVector = node.operation.execute(executionContext)(inputVector)
    logger.info(s"${node.id} resultVector.size = ${resultVector.size}")
    resultVector
  }

  private def storeAndRegister(dOperable: DOperable): UUID = {
    val experiment = graphExecutor.experiment.get
    val inputEntity = InputEntity(
      experiment.tenantId,
      "temporary Entity",
      "temporary Entity",
      "?",
      dOperable.url.map(DataObjectReference),
      Some(dOperable.report.toDataObjectReport),
      saved = false
    )

    Await.result(
      entityStorageClient.createEntity(inputEntity).map(_.id),
      entityStorageResponseDelay).value
  }
}
