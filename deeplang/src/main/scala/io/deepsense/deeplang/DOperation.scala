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

package io.deepsense.deeplang

import scala.reflect.runtime.{universe => ru}

import io.deepsense.commons.models
import io.deepsense.commons.utils.{CollectionExtensions, Logging}
import io.deepsense.deeplang.DPortPosition.DPortPosition
import io.deepsense.deeplang.documentation.OperationDocumentation
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.Params
import io.deepsense.graph.{GraphKnowledge, Operation}

/**
 * DOperation that receives and returns instances of DOperable.
 * Can infer its output type based on type knowledge.
 */
@SerialVersionUID(1L)
abstract class DOperation extends Operation
    with Serializable with Logging with Params {
  import CollectionExtensions._

  val inArity: Int
  val outArity: Int
  val id: DOperation.Id
  val name: String
  val description: String
  def hasDocumentation: Boolean = false

  def inPortTypes: Vector[ru.TypeTag[_]]

  def inPortsLayout: Vector[DPortPosition] = defaultPortLayout(inPortTypes, GravitateLeft)

  def outPortTypes: Vector[ru.TypeTag[_]]

  def outPortsLayout: Vector[DPortPosition] = defaultPortLayout(outPortTypes, GravitateRight)

  private def defaultPortLayout(portTypes: Vector[ru.TypeTag[_]], gravity: Gravity): Vector[DPortPosition] = {
    import DPortPosition._
    portTypes.size match {
      case 0 => Vector.empty
      case 1 => Vector(Center)
      case 2 => gravity match {
        case GravitateLeft => Vector(Left, Center)
        case GravitateRight => Vector(Center, Right)
      }
      case 3 => Vector(Left, Center, Right)
      case other => throw new IllegalStateException(s"Unsupported number of output ports: $other")
    }
  }

  def validate(): Unit = {
    require(outPortsLayout.size == outPortTypes.size, "Every output port must be laid out")
    require(!outPortsLayout.hasDuplicates, "Output port positions must be unique")
    require(inPortsLayout.size == inPortTypes.size, "Every input port must be laid out")
    require(!inPortsLayout.hasDuplicates, "Input port positions must be unique")
    require(inPortsLayout.isSorted, "Input ports must be laid out from left to right")
    require(outPortsLayout.isSorted, "Output ports must be laid out from left to right")
  }

  def executeUntyped(l: Vector[DOperable])(context: ExecutionContext): Vector[DOperable]

  /**
   * Infers knowledge for this operation.
   *
   * @param context Infer context to be used in inference.
   * @param inputKnowledge Vector of knowledge objects to be put in input ports of this operation.
   *                       This method assumes that size of this vector is equal to [[inArity]].
   * @return A tuple consisting of:
   *          - vector of knowledge object for each of operation's output port
   *          - inference warnings for this operation
   */
  def inferKnowledgeUntyped(
      inputKnowledge: Vector[DKnowledge[DOperable]])(
      context: InferContext)
      : (Vector[DKnowledge[DOperable]], InferenceWarnings)

  def inferGraphKnowledgeForInnerWorkflow(context: InferContext): GraphKnowledge = GraphKnowledge()

  def typeTag[T : ru.TypeTag]: ru.TypeTag[T] = ru.typeTag[T]
}

object DOperation {
  type Id = models.Id
  val Id = models.Id
}
