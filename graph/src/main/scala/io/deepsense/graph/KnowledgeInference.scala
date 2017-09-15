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

import io.deepsense.graph.GraphKnowledge.InferenceErrors

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang._
import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}
import io.deepsense.graph.TypesAccordance.TypesAccordance

case class SinglePortKnowledgeInferenceResult(
  knowledge: DKnowledge[DOperable],
  warnings: InferenceWarnings,
  errors: InferenceErrors)

trait KnowledgeInference {
  self: Graph =>

  /**
   * @return A graph knowledge with inferred results for every node.
   */
  def inferKnowledge(context: InferContext): GraphKnowledge = {
    val sorted = topologicallySorted.getOrElse(throw new CyclicGraphException())
    sorted
      .foldLeft(GraphKnowledge())((knowledge, node) => inferKnowledge(node, context, knowledge))
  }

  /**
   * @return A graph knowledge with knowledge inferred up to given node and port.
   */
  def inferKnowledge(
      nodeId: Node.Id,
      outPortIndex: Int,
      context: InferContext): SinglePortKnowledgeInferenceResult = {
    val subgraphNodes = allPredecessorsOf(nodeId) + nodeById(nodeId)
    val subgraphEdges = edges.filter(edge =>
      subgraphNodes.contains(nodeById(edge.from.nodeId)) &&
        subgraphNodes.contains(nodeById(edge.to.nodeId)))
    val inferenceResult =
      Graph(subgraphNodes, subgraphEdges).inferKnowledge(context).getResult(nodeId)
    SinglePortKnowledgeInferenceResult(
      inferenceResult.ports(outPortIndex),
      inferenceResult.warnings,
      inferenceResult.errors)
  }

  /**
   * @return Graph knowledge with inferred result for the given node.
   */
  private def inferKnowledge(
      node: Node,
      context: InferContext,
      graphKnowledge: GraphKnowledge): GraphKnowledge = {

    val NodeInferenceResult(inKnowledge, warnings, errors) =
      inputInferenceForNode(node, context, graphKnowledge)

    val inferenceResult = try {
      node.operation.parameters.validate
      val (outKnowledge, inferWarnings) =
        node.operation.inferKnowledge(context)(inKnowledge.toVector)
      NodeInferenceResult(outKnowledge, warnings ++ inferWarnings, errors)
    } catch {
      case exception: DeepLangException =>
        val outKnowledge = defaultOutputKnowledge(context, node.operation)
        NodeInferenceResult(outKnowledge, warnings, errors :+ exception)
    }
    graphKnowledge.addInference(node.id, inferenceResult)
  }

  private def inputInferenceForNode(
      node: Node,
      context: InferContext,
      graphKnowledge: GraphKnowledge): NodeInferenceResult = {

    (0 until predecessors(node.id).size).foldLeft(NodeInferenceResult.empty) {
      case (NodeInferenceResult(knowledge, warnings, errors), portIndex) =>
        val (portKnowledge, accordance) =
          inputKnowledgeAndAccordanceForInputPort(node, context, graphKnowledge, portIndex)
        NodeInferenceResult(
          knowledge :+ portKnowledge, warnings ++ accordance.warnings, errors ++ accordance.errors)
    }
  }

  /**
   * @return Input knowledge to be provided for the given input port
   *         and type accordance for edge incoming to this port.
   * @param node Node that contains input port.
   * @param context InferContext for inference.
   * @param graphKnowledge Contains inference results computed so far. This method assumes that
   *                       graphKnowledge contains all required data.
   */
  private def inputKnowledgeAndAccordanceForInputPort(
      node: Node,
      context: InferContext,
      graphKnowledge: GraphKnowledge,
      portIndex: Int): (DKnowledge[DOperable], TypesAccordance) = {
    val inPortType = node.operation.inPortTypes(portIndex).asInstanceOf[ru.TypeTag[DOperable]]
    predecessors(node.id)(portIndex) match {
      case None => (defaultKnowledge(context, inPortType), TypesAccordance.NotProvided(portIndex))
      case Some(predecessor) =>
        val outPortIndex = getSuccessorPort(predecessor.nodeId, Endpoint(node.id, portIndex)).get
        val predecessorKnowledge = graphKnowledge.getKnowledge(predecessor.nodeId)(outPortIndex)
        inputKnowledgeAndAccordanceForInputPort(
          context,
          predecessorKnowledge,
          portIndex,
          inPortType)
    }
  }

  /**
   * @return Input knowledge to be provided for the given input port
   *         and type accordance for edge incoming to this port.
   *         If some (but not all) types matches input port type accordance, only matching types are
   *         placed in that port.
   *         If no types matches port, default knowledge is placed in this port.
   * @param predecessorKnowledge Inferred knowledge incoming to port.
   * @param portIndex Index of input port.
   * @param inPortType Type of input port.
   */
  private def inputKnowledgeAndAccordanceForInputPort(
      context: InferContext,
      predecessorKnowledge: DKnowledge[DOperable],
      portIndex: Int,
      inPortType: ru.TypeTag[DOperable]): (DKnowledge[DOperable], TypesAccordance) = {
    val filteredTypes = predecessorKnowledge.filterTypes(inPortType.tpe)
    val filteredSize = filteredTypes.size
    if (filteredSize == predecessorKnowledge.size) {
      (filteredTypes, TypesAccordance.All())
    } else if (filteredSize == 0) {
      (defaultKnowledge(context, inPortType), TypesAccordance.None(portIndex))
    } else {
      (filteredTypes, TypesAccordance.Some(portIndex))
    }
  }

  /**
   * @return Knowledge vector for output ports if no additional information is provided.
   */
  private def defaultOutputKnowledge(
      context: InferContext,
      operation: DOperation): Vector[DKnowledge[DOperable]] =
    for (outPortType <- operation.outPortTypes) yield defaultKnowledge(context, outPortType)

  /**
   * @return Knowledge for port if no additional information is provided.
   */
  private def defaultKnowledge(
      context: InferContext,
      portType: ru.TypeTag[_]): DKnowledge[DOperable] = {
    val castedType = portType.asInstanceOf[ru.TypeTag[DOperable]]
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances(castedType))
  }

  /**
   * @return Port index which contains the given successor.
   */
  private def getSuccessorPort(of: Node.Id, successorEndpoint: Endpoint): Option[Int] = {
    val successorIndex = successors(of).indexWhere(_.contains(successorEndpoint))
    if (successorIndex != -1) Some(successorIndex) else None
  }
}
