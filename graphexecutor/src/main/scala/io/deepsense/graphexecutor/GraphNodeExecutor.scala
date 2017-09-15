/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.graphexecutor

import java.util.UUID

import scala.concurrent.{Await, duration}
import scala.concurrent.duration.FiniteDuration

import com.typesafe.scalalogging.LazyLogging

import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.entitystorage.EntityStorageClient
import io.deepsense.graph.Node
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

      val resultVector = executeOperation(collectOutputs())

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

  private def collectOutputs() = {
    var collectedOutputs = Vector.empty[DOperable]
    graphExecutor.graphGuard.synchronized {
      for (preNodeIds <- graphExecutor.graph.get.predecessors.get(node.id)) {
        for (preNodeId <- preNodeIds) {
          if (graphExecutor.graph.get.node(preNodeId.get.nodeId).state.results.nonEmpty) {
            collectedOutputs = collectedOutputs ++ graphExecutor.dOperableCache.get(
              graphExecutor.graph.get.node(preNodeId.get.nodeId).state.results.get.head)
          }
        }
      }
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
      Some(DataObjectReport(dOperable.report)),
      saved = false
    )

    Await.result(
      entityStorageClient.createEntity(inputEntity).map(_.id),
      entityStorageResponseDelay).value
  }
}
