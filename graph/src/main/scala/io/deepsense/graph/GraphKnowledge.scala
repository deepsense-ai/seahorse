/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.graph

import io.deepsense.deeplang.{DKnowledge, DOperable}

/**
 * Represents knowledge about Graph which is send and used by front-end.
 * It contains mappings between nodes and their inferred knowledge on output ports.
 */
case class GraphKnowledge(
    private[graph] val knowledgeMap: Map[Node.Id, Vector[DKnowledge[DOperable]]]) {

  def addKnowledge(
      id: Node.Id,
      knowledge: Vector[DKnowledge[DOperable]]): GraphKnowledge = {
    GraphKnowledge(knowledgeMap + (id -> knowledge))
  }

  def getKnowledge(id: Node.Id): Vector[DKnowledge[DOperable]] = knowledgeMap(id)
}

object GraphKnowledge {
  def apply(): GraphKnowledge =
    GraphKnowledge(Map[Node.Id, Vector[DKnowledge[DOperable]]]())
}
