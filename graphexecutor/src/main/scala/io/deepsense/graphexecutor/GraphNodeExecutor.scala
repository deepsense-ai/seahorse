/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.graphexecutor

import java.util.UUID

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.graph.{Status, Node}

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
    node: Node)
  extends Runnable {

  override def run(): Unit = {
    try {
      graphExecutor.graphGuard.synchronized {
        require(node.isRunning)
      }
      // TODO: Perform real execution of node
      println("Execution of node: " + node.id + " time: " + System.currentTimeMillis)
      // node.operation.execute(ec)
      Thread.sleep(10 * 1000)

      graphExecutor.graphGuard.synchronized {
        graphExecutor.graph = Some(graphExecutor.graph.get.markAsCompleted(node.id, List[UUID]()))
      }
    } catch {
      case e: Exception => {
        // Exception thrown here, during handling exception could result in application deadlock
        // (node stays in status RUNNING forever, Graph Executor waiting for this status change)
        graphExecutor.graphGuard.synchronized {
          // TODO: Exception should be relayed to graph
          // TODO: To decision: exception in single node should result in abortion of:
          // (current) only descendant nodes of failed node? / only queued nodes? / all other nodes?
          graphExecutor.graph = Some(graphExecutor.graph.get.markAsFailed(node.id))
        }
        // TODO: proper logging
        e.printStackTrace()
      }
    } finally {
      // Exception thrown here could result in slightly delayed graph execution
      graphExecutor.graphEventBinarySemaphore.release()
    }
  }
}
