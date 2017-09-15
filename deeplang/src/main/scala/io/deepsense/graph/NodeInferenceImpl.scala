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

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.{DKnowledge, DOperable, DOperation}
import io.deepsense.graph.DeeplangGraph.DeeplangNode
import io.deepsense.graph.TypesAccordance.TypesAccordance

trait NodeInferenceImpl extends NodeInference {
  /**
   * @return inferred result for the given node.
   */
  override def inferKnowledge(
      node: DeeplangNode,
      context: InferContext,
      inputInferenceForNode: NodeInferenceResult): NodeInferenceResult = {

    val NodeInferenceResult(inKnowledge, warnings, errors) = inputInferenceForNode
    val parametersValidationErrors = node.value.validateParams

    def defaultInferenceResult(additionalErrors: Vector[DeepLangException] = Vector.empty) =
      createDefaultKnowledge(
        context,
        node.value,
        warnings,
        (errors ++ parametersValidationErrors ++ additionalErrors).distinct)

    if (context.fullInference && parametersValidationErrors.nonEmpty) {
      defaultInferenceResult()
    } else {
      try {
        val (outKnowledge, inferWarnings) =
          node.value.inferKnowledge(context)(inKnowledge)
        NodeInferenceResult(
          outKnowledge,
          warnings ++ inferWarnings,
          errors ++ parametersValidationErrors
        )
      } catch {
        case exception: DeepLangException =>
          defaultInferenceResult(exception.toVector)
        case exception: Exception =>
          defaultInferenceResult()
      }
    }
  }

  override def inputInferenceForNode(
      node: DeeplangNode,
      context: InferContext,
      graphKnowledge: GraphKnowledge,
      nodePredecessorsEndpoints: IndexedSeq[Option[Endpoint]]): NodeInferenceResult = {

    (0 until node.value.inArity).foldLeft(NodeInferenceResult.empty) {
      case (NodeInferenceResult(knowledge, warnings, errors), portIndex) =>
        val predecessorEndpoint = nodePredecessorsEndpoints(portIndex)
        val (portKnowledge, accordance) =
          inputKnowledgeAndAccordanceForInputPort(
            node,
            context,
            graphKnowledge,
            portIndex,
            predecessorEndpoint)
        NodeInferenceResult(
          knowledge :+ portKnowledge,
          warnings ++ accordance.warnings,
          errors ++ accordance.errors
        )
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
      node: DeeplangNode,
      context: InferContext,
      graphKnowledge: GraphKnowledge,
      portIndex: Int,
      predecessorEndpointOption: Option[Endpoint]): (DKnowledge[DOperable], TypesAccordance) = {
    val inPortType = node.value.inPortTypes(portIndex).asInstanceOf[ru.TypeTag[DOperable]]
    predecessorEndpointOption match {
      case None => (defaultKnowledge(context, inPortType), TypesAccordance.NotProvided(portIndex))
      case Some(predecessorEndpoint) =>
        val outPortIndex = predecessorEndpoint.portIndex
        val predecessorKnowledge = graphKnowledge.getKnowledge(
          predecessorEndpoint.nodeId)(outPortIndex)
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
   * @param context Inference context.
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

  private def createDefaultKnowledge(
      context: InferContext,
      operation: DOperation,
      warnings: InferenceWarnings,
      errors: Vector[DeepLangException]): NodeInferenceResult = {
    val outKnowledge = defaultOutputKnowledge(context, operation)
    NodeInferenceResult(outKnowledge, warnings, errors)
  }
}
