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
import io.deepsense.models.entities.{DataObjectReport, InputEntity}

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
    try {
      graphExecutor.graphGuard.synchronized {
        require(node.isRunning)
      }
      logger.info("Execution of node: " + node.id + " time: " + System.currentTimeMillis)
      logger.info("real node execution")
      // TODO: pass real vector of DOperable's, not "deceptive mock"
      // (we are assuming that all DOperation's return at most one DOperable)

      val collectedOutput = graphExecutor.graphGuard.synchronized {
        collectOutputs(graphExecutor.graph.get, graphExecutor.dOperableCache)
      }
      val resultVector = executeOperation(collectedOutput)

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
        // Exception thrown here, during handling exception could result in application deadlock
        // (node stays in status RUNNING forever, Graph Executor waiting for this status change)
        graphExecutor.graphGuard.synchronized {
          // TODO: Exception should be relayed to graph
          // TODO: To decision: exception in single node should result in abortion of:
          // (current) only descendant nodes of failed node? / only queued nodes? / all other nodes?
          graphExecutor.experiment =
            Some(graphExecutor.experiment.get
              .copy(graph = graphExecutor.graph.get.markAsFailed(node.id)))
        }
        logger.error("Graph execution failes", e)
      }
    } finally {
      // Exception thrown here could result in slightly delayed graph execution
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

  private def executeOperation(input: Vector[DOperable]): Vector[DOperable] = {
    // TODO Replace with logging
    logger.info("node.operation= {}", node.operation.name)
    logger.info("vector#= {}", input.size.toString)
    val resultVector = node.operation.execute(executionContext)(input)
    logger.info("resultVector#= {}", resultVector.size.toString)
    resultVector
  }

  private def storeAndRegister(dOperable: DOperable): UUID = {
    val experiment = graphExecutor.experiment.get
    val inputEntity = InputEntity(
      experiment.tenantId,
      "temporary Entity",
      "temporary Entity",
      "?",
      None,
      Some(dOperable.report.toDataObjectReport),
      saved = false
    )

    Await.result(
      entityStorageClient.createEntity(inputEntity).map(_.id),
      entityStorageResponseDelay).value
  }
}
