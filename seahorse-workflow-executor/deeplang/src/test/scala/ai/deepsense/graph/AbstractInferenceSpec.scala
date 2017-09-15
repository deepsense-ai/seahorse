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

import org.scalatest.{Matchers, WordSpec}

import ai.deepsense.deeplang._
import ai.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import ai.deepsense.deeplang.exceptions.{DeepLangException, DeepLangMultiException}
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarning, InferenceWarnings}
import ai.deepsense.deeplang.params.exceptions.ValidationException
import ai.deepsense.graph.DClassesForDOperations._
import ai.deepsense.graph.DOperationTestClasses._
import ai.deepsense.graph.DeeplangGraph.DeeplangNode


class AbstractInferenceSpec
  extends WordSpec
  with DeeplangTestSupport
  with Matchers {

  val hierarchy = new DOperableCatalog
  hierarchy.registerDOperable[A1]()
  hierarchy.registerDOperable[A2]()

  val knowledgeA1: DKnowledge[DOperable] = DKnowledge(A1())
  val knowledgeA2: DKnowledge[DOperable] = DKnowledge(A2())
  val knowledgeA12: DKnowledge[DOperable] = DKnowledge(A1(), A2())

    val inferenceCtx: InferContext = createInferContext(hierarchy)

  /**
   * This operation can be set to:
   *  - have invalid parameters
   *  - throw inference errors
   * By default it infers A1 on its output port.
   */
  case class DOperationA1A2ToFirst()
      extends DOperation2To1[A1, A2, A]
      with DOperationBaseFields {
    import DOperationA1A2ToFirst._

    override protected def execute(t1: A1, t2: A2)(context: ExecutionContext): A = ???

    override def validateParams: Vector[DeepLangException] = {
      if (paramsValid) Vector.empty else Vector(parameterInvalidError)
    }

    private var paramsValid: Boolean = _
    def setParamsValid(): Unit = paramsValid = true
    def setParamsInvalid(): Unit = paramsValid = false

    private var inferenceShouldThrow = false
    private var multiException = false

    def setInferenceErrorThrowing(): Unit = inferenceShouldThrow = true

    def setInferenceErrorThrowingMultiException(): Unit = {
      inferenceShouldThrow = true
      multiException = true
    }

    override protected def inferKnowledge(
        k0: DKnowledge[A1],
        k1: DKnowledge[A2])(
        context: InferContext): (DKnowledge[A], InferenceWarnings) = {
      if (inferenceShouldThrow) {
        if (multiException) {
          throw multiInferenceError
        } else {
          throw inferenceError
        }
      }
      (k0, InferenceWarnings(warning))
    }

    override lazy val tTagTI_0: ru.TypeTag[A1] = ru.typeTag[A1]
    override lazy val tTagTO_0: ru.TypeTag[A] = ru.typeTag[A]
    override lazy val tTagTI_1: ru.TypeTag[A2] = ru.typeTag[A2]
  }

  object DOperationA1A2ToFirst {
    val parameterInvalidError = new ValidationException("") {}
    val inferenceError = new DeepLangException("") {}
    val multiInferenceError = DeepLangMultiException(
      Vector(mock[DeepLangException], mock[DeepLangException]))
    val warning = mock[InferenceWarning]
  }

  val idCreateA1 = Node.Id.randomId
  val idA1ToA = Node.Id.randomId
  val idAToA1A2 = Node.Id.randomId
  val idA1A2ToFirst = Node.Id.randomId

  protected def nodeCreateA1 = Node(idCreateA1, DOperationCreateA1())
  protected def nodeA1ToA = Node(idA1ToA, DOperationA1ToA())
  protected def nodeAToA1A2 = Node(idAToA1A2, DOperationAToA1A2())
  protected def nodeA1A2ToFirst = Node(idA1A2ToFirst, DOperationA1A2ToFirst())

  def validGraph: DeeplangGraph = DeeplangGraph(
    nodes = Set(nodeCreateA1, nodeAToA1A2, nodeA1A2ToFirst),
    edges = Set(
      Edge(nodeCreateA1, 0, nodeAToA1A2, 0),
      Edge(nodeAToA1A2, 0, nodeA1A2ToFirst, 0),
      Edge(nodeAToA1A2, 1, nodeA1A2ToFirst, 1))
  )

  def setParametersValid(node: DeeplangNode): Unit = {
    node.value.asInstanceOf[DOperationA1A2ToFirst].setParamsValid()
  }

  def setInferenceErrorThrowing(node: DeeplangNode): Unit = {
    node.value.asInstanceOf[DOperationA1A2ToFirst].setInferenceErrorThrowing()
  }

  def setInferenceErrorMultiThrowing(node: DeeplangNode): Unit = {
    node.value.asInstanceOf[DOperationA1A2ToFirst].setInferenceErrorThrowingMultiException()
  }

  def setParametersInvalid(node: DeeplangNode): Unit = {
    node.value.asInstanceOf[DOperationA1A2ToFirst].setParamsInvalid()
  }

  def setParametersValid(graph: DeeplangGraph): Unit = setInGraph(graph, _.setParamsValid())

  def setInGraph(graph: DeeplangGraph, f: DOperationA1A2ToFirst => Unit): Unit = {
    val node = graph.node(idA1A2ToFirst)
    f(node.value.asInstanceOf[DOperationA1A2ToFirst])
  }
}
