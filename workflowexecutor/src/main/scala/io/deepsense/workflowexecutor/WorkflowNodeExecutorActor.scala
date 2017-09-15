/**
 * Copyright 2015, CodiLime Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.workflowexecutor

import akka.actor.{Actor, PoisonPill}

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.{DKnowledge, DOperable, ExecutionContext}
import io.deepsense.graph.{Graph, Node}
import io.deepsense.models.entities.Entity
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages.{NodeFinished, NodeStarted}
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Results

/**
 * WorkflowNodeExecutorActor is responsible for execution of single node.
 * It requires that this node has state of RUNNING.
 * Actor performs its execution, changes to state COMPLETED (on success)
 * or FAILED (on fail) and finally notifies GraphExecutor of finished execution.
 */
class WorkflowNodeExecutorActor(
    executionContext: ExecutionContext,
    node: Node,
    graph: Graph,
    dOperableCache: Results)
  extends Actor
  with Logging {

  import io.deepsense.workflowexecutor.WorkflowNodeExecutorActor.Messages._

  lazy val nodeDescription = s"'${node.operation.name}-${node.id}'"
  var executionStart: Long = _

  override def receive: Receive = {
    case Start() =>
      executionStart = System.currentTimeMillis()
      logger.info(s">>> Start(node=${node.id})")
      val msg = NodeStarted(node.id)
      val runningNode = node.markRunning
      sender ! msg
      logger.info(s"<<< $msg")

      logger.debug(s"Collecting data for operation input ports for $nodeDescription")
      val collectedOutput = collectOutputs(graph, dOperableCache)
      logger.debug(s"Executing operation $nodeDescription")

      try {
        val resultVector = executeOperation(collectedOutput)
        logger.debug(s"Operation executed (without reports): $resultVector")

        logger.info(s"${runningNode.id} Registering data from operation output ports")
        val results: Map[Entity.Id, DOperable] = resultVector.map { dOperable =>
          Entity.Id.randomId -> dOperable
        }.toMap
        logger.debug(s"Data registered for $nodeDescription: results=$results")
        val finished = NodeFinished(runningNode.markCompleted(results.keys.toSeq), results)
        sender ! finished
        logger.info(s"<<< $finished")
      } catch {
        case e: Throwable =>
          logger.error(s"[nodeId: ${runningNode.id}] Graph execution failed", e)
          val failed = NodeFinished(runningNode.markFailed(e), results = Map.empty)
          sender ! failed
          logger.info(s"<<< $failed")
      } finally {
        // Exception thrown here could result in slightly delayed graph execution
        val duration = (System.currentTimeMillis() - executionStart) / 1000.0
        logger.info(s"$nodeDescription Execution of node ends (duration: $duration seconds)")
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
    logger.debug(s"$nodeDescription inputVector.size = ${inputVector.size}")
    val inputKnowledge = inputVector.map { dOperable => DKnowledge(dOperable.toInferrable) }
    // if inference throws, we do not perform execution
    node.operation.inferKnowledge(executionContext)(inputKnowledge)

    val resultVector = node.operation.execute(executionContext)(inputVector)
    logger.debug(s"$nodeDescription resultVector.size = ${resultVector.size}")
    resultVector
  }
}

object WorkflowNodeExecutorActor {
  object Messages {
    sealed trait Message
    case class Start() extends Message
  }
}
