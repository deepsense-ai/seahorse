/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.graphexecutor

import java.util.UUID

import scala.collection.mutable

import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.graph.{Status, Node}

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
    node: Node)
  extends Runnable {

  override def run(): Unit = {
    try {
      graphExecutor.graphGuard.synchronized {
        require(node.isRunning)
      }
      println("Execution of node: " + node.id + " time: " + System.currentTimeMillis)
      println("real node execution")
      // TODO: pass real vector of DOperable's, not "deceptive mock"
      // (we are assuming that all DOperation's return at most one DOperable)
      var vector = Vector.empty[DOperable]
      graphExecutor.graphGuard.synchronized {
        for (preNodeIds <- graphExecutor.graph.get.predecessors.get(node.id)) {
          for (preNodeId <- preNodeIds) {
            println("preNodeId=" + preNodeId.get.nodeId)
            if (graphExecutor.graph.get.node(preNodeId.get.nodeId).state.results.nonEmpty) {
              vector = vector ++ graphExecutor.dOperableCache.get(
                graphExecutor.graph.get.node(preNodeId.get.nodeId).state.results.get(0))
            }
          }
        }
      }
      println("node.operation= " + node.operation.name)
      println("vector#= " + vector.size)
      val resultVector = node.operation.execute(executionContext)(vector)
      println("resultVector#= " + resultVector.size)


      graphExecutor.graphGuard.synchronized {
        graphExecutor.graph =
          Some(graphExecutor.graph.get.markAsCompleted(
            node.id,
            resultVector.map(dOperable => {
              val uuid = UUID.randomUUID()
              graphExecutor.dOperableCache.put(uuid, dOperable)
              uuid
            }).toList))
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
