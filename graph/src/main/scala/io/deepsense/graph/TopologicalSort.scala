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

package io.deepsense.graph

import scala.collection.mutable

import io.deepsense.graph.Visits.Visits


private[graph] class TopologicalSort(graph: Graph) {
  private val visits: mutable.Map[Node, Visits] = mutable.Map()

  private var sorted: Option[List[Node]] = Some(List.empty)
  graph.nodes.foreach(n => sorted = topologicalSort(n, sorted))

  def isSorted: Boolean = !sorted.isDefined
  def sortedNodes: Option[List[Node]] = sorted

  private def topologicalSort(
    node: Node,
    sortedSoFar: Option[List[Node]]): Option[List[Node]] = {

    visits.get(node) match {
      case Some(Visits.Visited) => sortedSoFar
      case Some(Visits.InProgress) => None // we are revisiting a node; it is a cycle!
      case None => // node not yet visited
        markInProgress(node)
        var l = sortedSoFar
        graph
          .successors(node.id)
          .foreach(_.map(_.nodeId).foreach(s => l = topologicalSort(graph.node(s), l)))
        markVisited(node)
        l.map(node::_)
    }
  }

  private def markInProgress(node: Node): Unit = {
    visits(node) = Visits.InProgress
  }

  private def markVisited(node: Node): Unit = {
    visits(node) = Visits.Visited
  }
}

/**
 * Colours of node. Used for cycle checking.
 */
private[graph] object Visits extends Enumeration {
  type Visits = Value
  val InProgress, Visited = Value
}



