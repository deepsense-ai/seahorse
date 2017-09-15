/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.workflowexecutor.partialexecution

import io.deepsense.deeplang.inference.InferContext
import io.deepsense.graph.Node.Id
import io.deepsense.graph.graphstate.GraphState
import io.deepsense.graph.nodestate.NodeState
import io.deepsense.graph.{StatefulGraph, DirectedGraph, Node, ReadyNode}
import io.deepsense.models.entities.Entity

trait Execution {
  type NodeStates = Map[Node.Id, NodeState]

  def node(id: Node.Id): Node

  def nodeStarted(id: Node.Id): Execution
  def nodeFailed(id: Node.Id, cause: Exception): Execution
  def nodeFinished(id: Node.Id, results: Seq[Entity.Id]): Execution

  def enqueue(nodes: Seq[Node.Id]): Execution
  def readyNodes: Seq[ReadyNode]

  def state: GraphState
  def states: NodeStates

  def inferAndApplyKnowledge(inferContext: InferContext): Execution
  def updateStructure(directedGraph: DirectedGraph): Execution
}

case class PartialExecution(statefulGraph: StatefulGraph) extends Execution {
  override def enqueue(nodes: Seq[Id]): Execution = {
    copy(statefulGraph.enqueue)
  }
  override def readyNodes: Seq[ReadyNode] = {
    statefulGraph.readyNodes
  }

  override def state: GraphState = {
    statefulGraph.state
  }

  override def states: NodeStates = {
    statefulGraph.states
  }

  override def node(id: Id): Node = {
    statefulGraph.node(id)
  }

  override def nodeFinished(id: Id, results: Seq[Entity.Id]): Execution = {
    copy(statefulGraph.nodeFinished(id, results))
  }

  override def nodeFailed(id: Id, cause: Exception): Execution = {
    copy(statefulGraph.nodeFailed(id, cause))
  }

  override def nodeStarted(id: Id): Execution = {
    copy(statefulGraph.nodeStarted(id))
  }

  override def updateStructure(directedGraph: DirectedGraph): Execution = this
  override def inferAndApplyKnowledge(inferContext: InferContext): Execution = {
    copy(statefulGraph.inferAndApplyKnowledge(inferContext))
  }
}

object PartialExecution {
  def empty: PartialExecution = PartialExecution(StatefulGraph())

  def apply(directedGraph: DirectedGraph): PartialExecution =
    PartialExecution(StatefulGraph(directedGraph.nodes, directedGraph.edges))
}
