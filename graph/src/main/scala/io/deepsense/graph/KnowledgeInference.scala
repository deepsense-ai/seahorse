/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graph

import scala.reflect.runtime.{universe => ru}
import io.deepsense.deeplang.{DOperable, DKnowledge, InferContext}

trait KnowledgeInference {
  self: Graph =>

  /** Returns graph knowledge with knowledge inferred for the given node. */
  def inferKnowledge(
      node: Node,
      context: InferContext,
      graphKnowledge: GraphKnowledge): GraphKnowledge = {
    val knowledge = for (portIndex <- 0 until predecessors(node.id).size)
      yield inputKnowledgeForInputPort(node, context, graphKnowledge, portIndex)
    val inferredKnowledge = node.operation.inferKnowledge(context)(knowledge.toVector)
    graphKnowledge.addKnowledge(node.id, inferredKnowledge)
  }

  /** Returns a graph knowledge with inferred knowledge for every node. */
  def inferKnowledge(context: InferContext): GraphKnowledge = {
    val sorted = topologicallySorted.get
    sorted
      .foldLeft(GraphKnowledge())((knowledge, node) => inferKnowledge(node, context, knowledge))
  }

  /** Returns suitable input knowledge for the given input port index. */
  private def inputKnowledgeForInputPort(
      node: Node,
      context: InferContext,
      graphKnowledge: GraphKnowledge,
      portIndex: Int): DKnowledge[DOperable] = {
    // TODO: find a way to delete this cast
    val inPortType = node.operation.inPortTypes(portIndex).asInstanceOf[ru.TypeTag[DOperable]]
    predecessors(node.id)(portIndex) match {
      case None => DKnowledge(context
        .dOperableCatalog
        .concreteSubclassesInstances(inPortType))
      case Some(predecessor) =>
        val outPortIndex = getSuccessorPort(predecessor.nodeId, node).get
        val predecessorKnowledge = graphKnowledge.getKnowledge(predecessor.nodeId)(outPortIndex)
        predecessorKnowledge.filterTypes(inPortType.tpe)
    }
  }

  /** Returns port index which contains the given successor. */
  private def getSuccessorPort(of: Node.Id, successor: Node): Option[Int] = {
    val successorIndex = successors(of).indexWhere(_.exists(_.nodeId == successor.id))
    if (successorIndex != -1) Some(successorIndex) else None
  }

}
