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

import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.inference.InferenceWarnings
import io.deepsense.deeplang.inference.exceptions.{NoInputEdgesException, AllTypesNotCompilableException}
import io.deepsense.deeplang.inference.warnings.SomeTypesNotCompilableWarning
import io.deepsense.deeplang.{DKnowledge, DOperable}
import io.deepsense.graph.GraphKnowledge.InferenceErrors

/**
 * Represents inferred information about Graph which is send and used by front-end.
 * It contains mapping from nodes to their inferred knowledge on output ports,
 * as well as inferred warnings and errors.
 */
case class GraphKnowledge(
    private[graph] val resultsMap: Map[Node.Id, NodeInferenceResult]) {

  def addInference(
      id: Node.Id,
      inferenceResult: NodeInferenceResult): GraphKnowledge = {
    GraphKnowledge(resultsMap + (id -> inferenceResult))
  }

  def getResult(id: Node.Id): NodeInferenceResult = resultsMap(id)

  def getKnowledge(id: Node.Id): Vector[DKnowledge[DOperable]] = getResult(id).ports

  def results: Map[Node.Id, NodeInferenceResult] = resultsMap

  /**
   * Map from node ids to their errors. Contains only nodes that have errors.
   */
  lazy val errors: Map[Node.Id, InferenceErrors] = {
    val pairs = for {
      (nodeId, result) <- resultsMap
      errors = result.errors
      if errors.nonEmpty
    } yield (nodeId, errors)
    pairs.toMap
  }
}

object GraphKnowledge {
  def apply(): GraphKnowledge = GraphKnowledge(Map.empty)

  type InferenceErrors = Vector[DeepLangException]
}

case class NodeInferenceResult(
    ports: Vector[DKnowledge[DOperable]],
    warnings: InferenceWarnings = InferenceWarnings.empty,
    errors: InferenceErrors = Vector.empty)

object NodeInferenceResult {
  def empty: NodeInferenceResult = NodeInferenceResult(Vector.empty)
}

/**
 * Represents degree of accordance of given DKnowledge with an input port of some operation.
 * DKnowledge tells about objects that can be potentially put into a port.
 * This port has a type qualifier. Thus, we can consider following cases:
 *  * all types of objects in DKnowledge can be put into port
 *  * not all, but some types of objects can be put into port
 *  * none of types can be put into port
 *  * DKnowledge was not provided for the port
 * In each of this cases different errors and warnings can be returned.
 */
object TypesAccordance {

  trait TypesAccordance {
    def errors: InferenceErrors = Vector.empty

    def warnings: InferenceWarnings = InferenceWarnings.empty
  }

  /**
   * All of types injected to port meets this port's type qualifier requirements.
   */
  case class All() extends TypesAccordance

  /**
   * Some (but not all) of types injected to port meets this port's type qualifier requirements.
   */
  case class Some(portIndex: Int) extends TypesAccordance {
    override def warnings: InferenceWarnings =
      InferenceWarnings(SomeTypesNotCompilableWarning(portIndex))
  }

  /**
   * None of types injected to port meets this port's type qualifier requirements.
   */
  case class None(portIndex: Int) extends TypesAccordance {
    override def errors: InferenceErrors = Vector(AllTypesNotCompilableException(portIndex))
  }

  /**
   * No knowledge was provided to port.
   */
  case class NotProvided(portIndex: Int) extends TypesAccordance {
    override def errors: InferenceErrors = Vector(NoInputEdgesException(portIndex))
  }
}
