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

import scala.collection.mutable

private[graph] class TopologicalSort(sortable: TopologicallySortable) {
  private val visits: mutable.Map[Node, Visits] = mutable.Map()

  private var sorted: Option[List[Node]] = Some(List.empty)
  sortable.nodes.foreach(n => sorted = topologicalSort(n, sorted))

  def isSorted: Boolean = sorted.isEmpty
  def sortedNodes: Option[List[Node]] = sorted

  private def topologicalSort(
    node: Node,
    sortedSoFar: Option[List[Node]]): Option[List[Node]] = {

    visits.get(node) match {
      case Some(Visited) => sortedSoFar
      case Some(InProgress) => None // we are revisiting a node; it is a cycle!
      case None => // node not yet visited
        markInProgress(node)
        var l = sortedSoFar
        sortable
          .successors(node.id)
          .foreach(_.map(_.nodeId).foreach(s => l = topologicalSort(sortable.node(s), l)))
        markVisited(node)
        l.map(node::_)
    }
  }

  private def markInProgress(node: Node): Unit = {
    visits(node) = InProgress
  }

  private def markVisited(node: Node): Unit = {
    visits(node) = Visited
  }

  private sealed abstract class Visits()
  private case object InProgress extends Visits()
  private case object Visited extends Visits()
}


