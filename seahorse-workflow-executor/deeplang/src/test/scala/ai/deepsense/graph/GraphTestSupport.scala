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

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar

import ai.deepsense.commons.datetime.DateTimeConverter
import ai.deepsense.commons.exception.FailureDescription
import ai.deepsense.commons.models.Entity
import ai.deepsense.deeplang._
import ai.deepsense.graph.DeeplangGraph.DeeplangNode

trait GraphTestSupport {
  self: MockitoSugar =>

  val op0To1 = {
    val m = mock[DOperation0To1[DOperable]]
    when(m.sameAs(any())).thenReturn(true)
    m
  }

  val op1To1 = createOp1To1

  def createOp1To1: DOperation1To1[DOperable, DOperable] = {
    val m = mock[DOperation1To1[DOperable, DOperable]]
    when(m.sameAs(any())).thenReturn(true)
    m
  }

  val op2To2 = {
    val m = mock[DOperation2To2[DOperable, DOperable, DOperable, DOperable]]
    when(m.sameAs(any())).thenReturn(true)
    m
  }

  /**
   * Creates edges for a graph like this one:
   *   A -(1)-> B -(2)-> C -(3)-> D
   *     \      \
   *     \      (5)
   *     \       \
   *     \        ->
   *      ---(4)---> E
   * To each node assigns the specified Id.
   */

  val nodesSeq = generateNodes(op0To1, op1To1, op1To1, op1To1, op2To2)
  val nodeSet = nodesSeq.map(_._2).toSet
  val idA :: idB :: idC :: idD :: idE :: Nil = nodesSeq.map(_._1).toList
  val nodeA :: nodeB :: nodeC :: nodeD :: nodeE :: Nil = nodesSeq.map(_._2).toList

  val edgeList: List[Edge] = edges(idA, idB, idC, idD, idE)
  val edge1 :: edge2 :: edge3 :: edge4 :: edge5 :: Nil = edgeList
  val edgeSet = edgeList.toSet
  val nodeIds = Seq(idA, idB, idC, idD, idE)

  val results = Map(
    idA -> Seq(mock[Entity.Id]),
    idB -> Seq(mock[Entity.Id]),
    idC -> Seq(mock[Entity.Id]),
    idD -> Seq(mock[Entity.Id]),
    idE -> Seq(mock[Entity.Id], mock[Entity.Id])
  )


  private def edges(
      idA: Node.Id,
      idB: Node.Id,
      idC: Node.Id,
      idD: Node.Id,
      idE: Node.Id): List[Edge] = {
    List(
      Edge(Endpoint(idA, 0), Endpoint(idB, 0)),
      Edge(Endpoint(idB, 0), Endpoint(idC, 0)),
      Edge(Endpoint(idC, 0), Endpoint(idD, 0)),
      Edge(Endpoint(idA, 0), Endpoint(idE, 0)),
      Edge(Endpoint(idB, 0), Endpoint(idE, 1))
    )
  }

  protected def generateNodes(ops: DOperation*): Seq[(Node.Id, DeeplangNode)] = {
    val nodes = ops.map { o => Node(Node.Id.randomId, o)}
    nodes.map(n => n.id -> n)
  }

  protected def nodeRunning: nodestate.Running = nodestate.Running(DateTimeConverter.now)

  protected def nodeFailed: nodestate.Failed =
    nodestate.Running(DateTimeConverter.now).fail(mock[FailureDescription])

  protected def nodeCompleted: nodestate.Completed = {
    val date = DateTimeConverter.now
    nodestate.Completed(date, date.plusMinutes(1), Seq())
  }

  protected def nodeCompletedId(nodeId: Entity.Id): nodestate.Completed = {
    val date = DateTimeConverter.now
    nodestate.Completed(date, date.plusMinutes(1), results(nodeId))
  }
}
