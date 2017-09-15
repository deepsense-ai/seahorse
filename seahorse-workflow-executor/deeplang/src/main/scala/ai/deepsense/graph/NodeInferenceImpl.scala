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

import scala.reflect.runtime.{universe => ru}

import ai.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import ai.deepsense.deeplang.exceptions.DeepLangException
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.{DKnowledge, DOperable, DOperation}
import ai.deepsense.graph.DeeplangGraph.DeeplangNode
import ai.deepsense.graph.DefaultKnowledgeService._
import ai.deepsense.graph.TypesAccordance.TypesAccordance

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
        context.dOperableCatalog,
        node.value,
        warnings,
        (errors ++ parametersValidationErrors ++ additionalErrors).distinct)

    if (parametersValidationErrors.nonEmpty) {
      defaultInferenceResult()
    } else {
      try {
        val (outKnowledge, inferWarnings) =
          node.value.inferKnowledgeUntyped(inKnowledge)(context)
        NodeInferenceResult(outKnowledge, warnings ++ inferWarnings, errors)
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
            context.dOperableCatalog,
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
   * @param catalog Catalog of registered DOperables.
   * @param graphKnowledge Contains inference results computed so far. This method assumes that
   *                       graphKnowledge contains all required data.
   */
  private def inputKnowledgeAndAccordanceForInputPort(
      node: DeeplangNode,
      catalog: DOperableCatalog,
      graphKnowledge: GraphKnowledge,
      portIndex: Int,
      predecessorEndpointOption: Option[Endpoint]): (DKnowledge[DOperable], TypesAccordance) = {
    val inPortType = node.value.inPortTypes(portIndex).asInstanceOf[ru.TypeTag[DOperable]]
    predecessorEndpointOption match {
      case None => (defaultKnowledge(catalog, inPortType), TypesAccordance.NotProvided(portIndex))
      case Some(predecessorEndpoint) =>
        val outPortIndex = predecessorEndpoint.portIndex
        val predecessorKnowledge = graphKnowledge.getKnowledge(
          predecessorEndpoint.nodeId)(outPortIndex)
        inputKnowledgeAndAccordanceForInputPort(
          catalog,
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
   * @param catalog Catalog of registered DOperables.
   */
  private def inputKnowledgeAndAccordanceForInputPort(
      catalog: DOperableCatalog,
      predecessorKnowledge: DKnowledge[DOperable],
      portIndex: Int,
      inPortType: ru.TypeTag[DOperable]): (DKnowledge[DOperable], TypesAccordance) = {
    val filteredTypes = predecessorKnowledge.filterTypes(inPortType.tpe)
    val filteredSize = filteredTypes.size
    if (filteredSize == predecessorKnowledge.size) {
      (filteredTypes, TypesAccordance.All())
    } else if (filteredSize == 0) {
      (defaultKnowledge(catalog, inPortType), TypesAccordance.None(portIndex))
    } else {
      (filteredTypes, TypesAccordance.Some(portIndex))
    }
  }

  private def createDefaultKnowledge(
      catalog: DOperableCatalog,
      operation: DOperation,
      warnings: InferenceWarnings,
      errors: Vector[DeepLangException]): NodeInferenceResult = {
    val outKnowledge = defaultOutputKnowledge(catalog, operation)
    NodeInferenceResult(outKnowledge, warnings, errors)
  }
}
