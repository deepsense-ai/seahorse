/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
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



