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

package io.deepsense.graph

case class DirectedGraph(
    nodes: Set[Node] = Set(),
    edges: Set[Edge] = Set())
  extends TopologicallySortable
  with KnowledgeInference
  with NodeInferenceImpl {

  private val nonExistingNodes: Set[Node.Id] = checkEdgesEndsExist(nodes, edges)

  require(nonExistingNodes.isEmpty,
    s"There are connections that point to non-existent nodes ${nonExistingNodes.mkString(", ")}")

  private val idToNode = nodes.map(n => n.id -> n).toMap
  private val _predecessors = preparePredecessors
  private val _successors = prepareSuccessors
  private val _containsCycle = new TopologicalSort(this).isSorted

  def topologicallySorted: Option[List[Node]] = new TopologicalSort(this).sortedNodes
  def node(id: Node.Id): Node = idToNode(id)
  def predecessors(id: Node.Id): IndexedSeq[Option[Endpoint]] = _predecessors(id)
  def successors(id: Node.Id): IndexedSeq[Set[Endpoint]] = _successors(id)
  def containsCycle: Boolean = _containsCycle

  def allPredecessorsOf(id: Node.Id): Set[Node] = {
    predecessors(id).foldLeft(Set[Node]())((acc: Set[Node], predecessor: Option[Endpoint]) =>
      predecessor match {
        case None => acc
        case Some(endpoint) => (acc + node(endpoint.nodeId)) ++
          allPredecessorsOf(endpoint.nodeId)
      })
  }

  def size: Int = nodes.size

  def subgraph(nodes: Set[Node.Id]): DirectedGraph = {
    def collectNodesEdges(
        previouslyCollectedNodes: Set[Node.Id],
        previouslyCollectedEdges: Set[Edge],
        toProcess: Set[Node.Id]): (Set[Node.Id], Set[Edge]) = {
      val nodesPredecessors = predecessorsOf(toProcess)
      val nextNodes = previouslyCollectedNodes ++ nodesPredecessors
      val nextEdges = previouslyCollectedEdges ++ edgesOf(toProcess)

      if (toProcess.isEmpty) {
        (nextNodes, nextEdges)
      } else {
        collectNodesEdges(nextNodes, nextEdges, nodesPredecessors)
      }
    }

    val (n, e) = collectNodesEdges(nodes, Set(), nodes)
    DirectedGraph(n.map(node), e)
  }

  def rootNodes: Iterable[Node] = {
    topologicallySorted.get.filter(n => predecessors(n.id).flatten.isEmpty)
  }

  def predecessorsOf(nodes: Set[Node.Id]): Set[Node.Id] = {
    nodes.flatMap {
      node => predecessors(node).flatten.map { _.nodeId }
    }
  }

  def successorsOf(node: Node.Id): Set[Node.Id] =
   successors(node).flatMap(endpoints => endpoints.map(_.nodeId)).toSet

  private def edgesOf(nodes: Set[Node.Id]): Set[Edge] = nodes.flatMap(edgesTo)

  private def edgesTo(node: Node.Id): Set[Edge] = edges.filter(edge => edge.to.nodeId == node)

  private def preparePredecessors: Map[Node.Id, IndexedSeq[Option[Endpoint]]] = {
    import scala.collection.mutable
    val mutablePredecessors: mutable.Map[Node.Id, mutable.IndexedSeq[Option[Endpoint]]] =
      mutable.Map()

    nodes.foreach(node => {
      mutablePredecessors +=
        node.id -> mutable.IndexedSeq.fill(node.operation.inArity)(None)
    })
    edges.foreach(edge => {
      mutablePredecessors(edge.to.nodeId)(edge.to.portIndex) = Some(edge.from)
    })
    mutablePredecessors.mapValues(_.toIndexedSeq).toMap
  }

  private def prepareSuccessors: Map[Node.Id, IndexedSeq[Set[Endpoint]]] = {
    import scala.collection.mutable
    val mutableSuccessors: mutable.Map[Node.Id, IndexedSeq[mutable.Set[Endpoint]]] =
      mutable.Map()

    nodes.foreach(node => {
      mutableSuccessors += node.id -> Vector.fill(node.operation.outArity)(mutable.Set())
    })
    edges.foreach(edge => {
      mutableSuccessors(edge.from.nodeId)(edge.from.portIndex) += edge.to
    })
    mutableSuccessors.mapValues(_.map(_.toSet)).toMap
  }

  private def checkEdgesEndsExist(nodes: Set[Node], edges: Set[Edge]): Set[Node.Id] = {
    val nodesIds = nodes.map(_.id)
    edges.flatMap { edge => Set(edge.from.nodeId, edge.to.nodeId)}.collect {
      case id if !nodesIds.contains(id) => id
    }
  }
}

