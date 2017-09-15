/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.graph

abstract class DirectedGraph[T <: Operation, G <: DirectedGraph[T, G]](
    val nodes: Set[Node[T]] = Set[Node[T]](),
    val edges: Set[Edge] = Set())
  extends TopologicallySortable[T]
  with Serializable {

  val validEdges = filterValidEdges(nodes, edges)

  private val idToNode = nodes.map(n => n.id -> n).toMap
  private val _predecessors = preparePredecessors
  private val _successors = prepareSuccessors
  private val _containsCycle = new TopologicalSort(this).isSorted

  def topologicallySorted: Option[List[Node[T]]] = new TopologicalSort(this).sortedNodes
  def node(id: Node.Id): Node[T] = idToNode(id)
  def predecessors(id: Node.Id): IndexedSeq[Option[Endpoint]] = _predecessors(id)
  def successors(id: Node.Id): IndexedSeq[Set[Endpoint]] = _successors(id)
  def containsCycle: Boolean = _containsCycle

  def allPredecessorsOf(id: Node.Id): Set[Node[T]] = {
    predecessors(id).foldLeft(Set[Node[T]]())((acc: Set[Node[T]], predecessor: Option[Endpoint]) =>
      predecessor match {
        case None => acc
        case Some(endpoint) => (acc + node(endpoint.nodeId)) ++
          allPredecessorsOf(endpoint.nodeId)
      })
  }

  def size: Int = nodes.size

  def rootNodes: Iterable[Node[T]] = {
    topologicallySorted.get.filter(n => predecessors(n.id).flatten.isEmpty)
  }

  def predecessorsOf(nodes: Set[Node.Id]): Set[Node.Id] = {
    nodes.flatMap {
      node => predecessors(node).flatten.map { _.nodeId }
    }
  }

  def successorsOf(node: Node.Id): Set[Node.Id] =
   successors(node).flatMap(endpoints => endpoints.map(_.nodeId)).toSet

  def subgraph(nodes: Set[Node.Id]): G = {
    def collectNodesEdges(
        previouslyCollectedNodes: Set[Node.Id],
        previouslyCollectedEdges: Set[Edge],
        toProcess: Set[Node.Id]): (Set[Node.Id], Set[Edge]) = {
      // Do not revisit nodes (in case of a cycle).
      val nodesPredecessors = predecessorsOf(toProcess) -- previouslyCollectedNodes
      val nextNodes = previouslyCollectedNodes ++ nodesPredecessors
      val nextEdges = previouslyCollectedEdges ++ edgesOf(toProcess)

      if (toProcess.isEmpty) {
        (nextNodes, nextEdges)
      } else {
        collectNodesEdges(nextNodes, nextEdges, nodesPredecessors)
      }
    }

    val (n, e) = collectNodesEdges(nodes, Set(), nodes)
    subgraph(n.map(node), e)
  }

  def subgraph(nodes: Set[Node[T]], edges: Set[Edge]): G

  def getValidEdges: Set[Edge] = validEdges

  private def edgesOf(nodes: Set[Node.Id]): Set[Edge] = nodes.flatMap(edgesTo)

  private def edgesTo(node: Node.Id): Set[Edge] = validEdges.filter(edge => edge.to.nodeId == node)

  private def preparePredecessors: Map[Node.Id, IndexedSeq[Option[Endpoint]]] = {
    import scala.collection.mutable
    val mutablePredecessors: mutable.Map[Node.Id, mutable.IndexedSeq[Option[Endpoint]]] =
      mutable.Map()

    nodes.foreach(node => {
      mutablePredecessors +=
        node.id -> mutable.IndexedSeq.fill(node.value.inArity)(None)
    })
    validEdges.foreach(edge => {
      mutablePredecessors(edge.to.nodeId)(edge.to.portIndex) = Some(edge.from)
    })
    mutablePredecessors.mapValues(_.toIndexedSeq).toMap
  }

  private def prepareSuccessors: Map[Node.Id, IndexedSeq[Set[Endpoint]]] = {
    import scala.collection.mutable
    val mutableSuccessors: mutable.Map[Node.Id, IndexedSeq[mutable.Set[Endpoint]]] =
      mutable.Map()

    nodes.foreach(node => {
      mutableSuccessors += node.id -> Vector.fill(node.value.outArity)(mutable.Set())
    })
    validEdges.foreach(edge => {
      mutableSuccessors(edge.from.nodeId)(edge.from.portIndex) += edge.to
    })
    mutableSuccessors.mapValues(_.map(_.toSet)).toMap
  }

  private def filterValidEdges(nodes: Set[Node[T]], edges: Set[Edge]): Set[Edge] = {
    val nodesIds = nodes.map(_.id)
    edges.filter(edge => {
      val inNodeOpt = nodes.find(n => n.id == edge.from.nodeId)
      val outNodeOpt = nodes.find(n => n.id == edge.to.nodeId)
      for (inNode <- inNodeOpt; outNode <- outNodeOpt) yield {
        edge.from.portIndex < inNode.value.outArity && edge.to.portIndex < outNode.value.inArity
      }}.getOrElse(false)
    )
  }
}

