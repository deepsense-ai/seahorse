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

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.inference.exceptions.{AllTypesNotCompilableException, NoInputEdgesException}
import io.deepsense.deeplang.inference.warnings.SomeTypesNotCompilableWarning
import io.deepsense.deeplang.inference.{InferContext, InferenceWarning, InferenceWarnings}
import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.deeplang.parameters.exceptions.ValidationException
import io.deepsense.deeplang.{DKnowledge, DOperable, DOperation2To1, ExecutionContext}

class KnowledgeInferenceSpec
  extends WordSpec
  with MockitoSugar
  with Matchers {

  import io.deepsense.graph.DClassesForDOperations._
  import io.deepsense.graph.DOperationTestClasses._

  val hierarchy = new DOperableCatalog
  hierarchy.registerDOperable[A1]()
  hierarchy.registerDOperable[A2]()

  val knowledgeA1: DKnowledge[DOperable] = DKnowledge(A1())
  val knowledgeA2: DKnowledge[DOperable] = DKnowledge(A2())
  val knowledgeA12: DKnowledge[DOperable] = DKnowledge(A1(), A2())

  val typeInferenceCtx: InferContext = new InferContext(hierarchy, fullInference = false)
  val fullInferenceCtx: InferContext = new InferContext(hierarchy, fullInference = true)

  "Graph" should {
    "infer type knowledge" when {
      "graph is valid" in {
        val graph = validGraph
        setParamsValid(graph)
        val graphKnowledge = graph.inferKnowledge(typeInferenceCtx)

        val graphKnowledgeExpected = Map(
          idCreateA1 -> NodeInferenceResult(Vector(knowledgeA1)),
          idAToA1A2 -> NodeInferenceResult(Vector(knowledgeA1, knowledgeA2)),
          idA1A2ToFirst -> NodeInferenceResult(
            Vector(knowledgeA1),
            warnings = InferenceWarnings(DOperationA1A2ToFirst.warning))
        )
        graphKnowledge.resultsMap should contain theSameElementsAs graphKnowledgeExpected
      }
      "not all input knowledge accord to input type qualifiers" in {
        val graph = graphWithNotAccordingTypes
        setParamsValid(graph)
        val graphKnowledge = graph.inferKnowledge(typeInferenceCtx)

        val graphKnowledgeExpected = Map(
          idCreateA1 -> NodeInferenceResult(Vector(knowledgeA1)),
          idA1ToA -> NodeInferenceResult(Vector(knowledgeA12)),
          idAToA1A2 -> NodeInferenceResult(Vector(knowledgeA1, knowledgeA2)),
          idA1A2ToFirst -> NodeInferenceResult(
            Vector(knowledgeA1),
            warnings = InferenceWarnings(
              SomeTypesNotCompilableWarning(portIndex = 1),
              DOperationA1A2ToFirst.warning
            ),
            errors = Vector(AllTypesNotCompilableException(portIndex = 0)))
        )
        graphKnowledge.resultsMap should contain theSameElementsAs graphKnowledgeExpected
      }
      "knowledge was not provided for some inputs" in {
        val graph = graphWithNotProvidedInputs
        setParamsValid(graph)
        val graphKnowledge = graph.inferKnowledge(typeInferenceCtx)

        val graphKnowledgeExpected = Map(
          idCreateA1 -> NodeInferenceResult(Vector(knowledgeA1)),
          idA1A2ToFirst -> NodeInferenceResult(
            Vector(knowledgeA1),
            warnings = InferenceWarnings(DOperationA1A2ToFirst.warning),
            errors = Vector(NoInputEdgesException(portIndex = 1))
          )
        )
        graphKnowledge.resultsMap should contain theSameElementsAs graphKnowledgeExpected
      }
      "graph is valid but params are invalid" in {
        val graph = validGraph
        setParamsInvalid(graph)
        val graphKnowledge = graph.inferKnowledge(typeInferenceCtx)

        val graphKnowledgeExpected = Map(
          idCreateA1 -> NodeInferenceResult(Vector(knowledgeA1)),
          idAToA1A2 -> NodeInferenceResult(Vector(knowledgeA1, knowledgeA2)),
          idA1A2ToFirst -> NodeInferenceResult(
            Vector(knowledgeA1),
            warnings = InferenceWarnings(DOperationA1A2ToFirst.warning),
            errors = Vector(DOperationA1A2ToFirst.parameterInvalidError))
        )
        graphKnowledge.resultsMap should contain theSameElementsAs graphKnowledgeExpected
      }
      "not all input knowledge accords to input type qualifiers and params are invalid" in {
        val graph = graphWithNotAccordingTypes
        setParamsInvalid(graph)
        val graphKnowledge = graph.inferKnowledge(typeInferenceCtx)

        val graphKnowledgeExpected = Map(
          idCreateA1 -> NodeInferenceResult(Vector(knowledgeA1)),
          idA1ToA -> NodeInferenceResult(Vector(knowledgeA12)),
          idAToA1A2 -> NodeInferenceResult(Vector(knowledgeA1, knowledgeA2)),
          idA1A2ToFirst -> NodeInferenceResult(
            Vector(knowledgeA1),
            warnings = InferenceWarnings(
              SomeTypesNotCompilableWarning(portIndex = 1),
              DOperationA1A2ToFirst.warning),
            errors = Vector(
              AllTypesNotCompilableException(portIndex = 0),
              DOperationA1A2ToFirst.parameterInvalidError
            )
          )
        )
        graphKnowledge.resultsMap should contain theSameElementsAs graphKnowledgeExpected
      }
      "knowledge was not provided for some inputs and params are invalid" in {
        val graph = graphWithNotProvidedInputs
        setParamsInvalid(graph)
        val graphKnowledge = graph.inferKnowledge(typeInferenceCtx)

        val graphKnowledgeExpected = Map(
          idCreateA1 -> NodeInferenceResult(Vector(knowledgeA1)),
          idA1A2ToFirst -> NodeInferenceResult(
            Vector(knowledgeA1),
            warnings = InferenceWarnings(DOperationA1A2ToFirst.warning),
            errors = Vector(
              NoInputEdgesException(portIndex = 1),
              DOperationA1A2ToFirst.parameterInvalidError
            )
          )
        )
        graphKnowledge.resultsMap should contain theSameElementsAs graphKnowledgeExpected
      }
      "graph is valid and inference throws an error" in {
        val graph = validGraph
        setThrowingError(graph)
        val graphKnowledge = graph.inferKnowledge(typeInferenceCtx)

        val graphKnowledgeExpected = Map(
          idCreateA1 -> NodeInferenceResult(Vector(knowledgeA1)),
          idAToA1A2 -> NodeInferenceResult(Vector(knowledgeA1, knowledgeA2)),
          idA1A2ToFirst -> NodeInferenceResult(
            Vector(knowledgeA12),
            errors = Vector(DOperationA1A2ToFirst.inferenceError))
        )
        graphKnowledge.resultsMap should contain theSameElementsAs graphKnowledgeExpected
      }
      "not all input knowledge accords to input type qualifiers and inference throws an error" in {
        val graph = graphWithNotAccordingTypes
        setThrowingError(graph)
        val graphKnowledge = graph.inferKnowledge(typeInferenceCtx)

        val graphKnowledgeExpected = Map(
          idCreateA1 -> NodeInferenceResult(Vector(knowledgeA1)),
          idA1ToA -> NodeInferenceResult(Vector(knowledgeA12)),
          idAToA1A2 -> NodeInferenceResult(Vector(knowledgeA1, knowledgeA2)),
          idA1A2ToFirst -> NodeInferenceResult(
            Vector(knowledgeA12),
            warnings = InferenceWarnings(SomeTypesNotCompilableWarning(portIndex = 1)),
            errors = Vector(
              AllTypesNotCompilableException(portIndex = 0),
              DOperationA1A2ToFirst.inferenceError)
          )
        )
        graphKnowledge.resultsMap should contain theSameElementsAs graphKnowledgeExpected
      }
      "knowledge was not provided for some inputs and inference throws an error" in {
        val graph = graphWithNotProvidedInputs
        setThrowingError(graph)
        val graphKnowledge = graph.inferKnowledge(typeInferenceCtx)

        val graphKnowledgeExpected = Map(
          idCreateA1 -> NodeInferenceResult(Vector(knowledgeA1)),
          idA1A2ToFirst -> NodeInferenceResult(
            Vector(knowledgeA12),
            errors = Vector(
              NoInputEdgesException(portIndex = 1),
              DOperationA1A2ToFirst.inferenceError)
          )
        )
        graphKnowledge.resultsMap should contain theSameElementsAs graphKnowledgeExpected
      }
    }
    "not inference types (only infer default knowledge) when parameters are not valid " +
      "for fullInference = true" in {
        val graph = validGraph
        setParamsInvalid(graph)
        val graphKnowledge = graph.inferKnowledge(fullInferenceCtx)

        val graphKnowledgeExpected = Map(
          idCreateA1 -> NodeInferenceResult(Vector(knowledgeA1)),
          idAToA1A2 -> NodeInferenceResult(Vector(knowledgeA1, knowledgeA2)),
          idA1A2ToFirst -> NodeInferenceResult(
            Vector(knowledgeA12),
            errors = Vector(DOperationA1A2ToFirst.parameterInvalidError))
        )
        graphKnowledge.resultsMap should contain theSameElementsAs graphKnowledgeExpected
    }

    "throw an exception" when {
      "graph contains cycle" in {
        intercept[CyclicGraphException] {
          graphWithCycle.inferKnowledge(typeInferenceCtx)
        }
        ()
      }
    }
  }

  it should {
    "infer knowledge up to given output port" in {
      val graph = validGraph
      setParamsValid(graph)

      val node1Result = graph.inferKnowledge(idCreateA1, 0, typeInferenceCtx)
      val node2Port0Result = graph.inferKnowledge(idAToA1A2, 0, typeInferenceCtx)
      val node2Port1Result = graph.inferKnowledge(idAToA1A2, 1, typeInferenceCtx)
      val node3Result = graph.inferKnowledge(idA1A2ToFirst, 0, typeInferenceCtx)

      node1Result.knowledge shouldBe knowledgeA1
      node2Port0Result.knowledge shouldBe knowledgeA1
      node2Port1Result.knowledge shouldBe knowledgeA2
      node3Result.knowledge shouldBe knowledgeA1
    }
  }

  /**
   * This operation can be set to:
   *  - have invalid parameters
   *  - throw inference errors
   * By default it infers A1 on its output port.
   */
  case class DOperationA1A2ToFirst() extends DOperation2To1[A1, A2, A] with DOperationBaseFields {
    import DOperationA1A2ToFirst._

    override val parameters = mock[ParametersSchema]

    override protected def _execute(context: ExecutionContext)(t1: A1, t2: A2): A = ???

    def setParamsValid(): Unit = doReturn(Vector.empty).when(parameters).validate

    def setParamsInvalid(): Unit = doReturn(Vector(parameterInvalidError)).when(parameters).validate

    private var inferenceShouldThrow = false

    def setInferenceErrorThrowing(): Unit = inferenceShouldThrow = true

    override protected def _inferTypeKnowledge(
      context: InferContext)(
      k0: DKnowledge[A1],
      k1: DKnowledge[A2]): (DKnowledge[A], InferenceWarnings) = {
      if (inferenceShouldThrow) {
        throw inferenceError
      }
      (k0, InferenceWarnings(warning))
    }

    override lazy val tTagTI_0: ru.TypeTag[A1] = ru.typeTag[A1]
    override lazy val tTagTO_0: ru.TypeTag[A] = ru.typeTag[A]
    override lazy val tTagTI_1: ru.TypeTag[A2] = ru.typeTag[A2]
  }

  object DOperationA1A2ToFirst {
    val parameterInvalidError = mock[ValidationException]
    val inferenceError = mock[DeepLangException]
    val warning = mock[InferenceWarning]
  }

  val idCreateA1 = Node.Id.randomId
  val idA1ToA = Node.Id.randomId
  val idAToA1A2 = Node.Id.randomId
  val idA1A2ToFirst = Node.Id.randomId

  private def nodeCreateA1 = Node(idCreateA1, DOperationCreateA1())
  private def nodeA1ToA = Node(idA1ToA, DOperationA1ToA())
  private def nodeAToA1A2 = Node(idAToA1A2, DOperationAToA1A2())
  private def nodeA1A2ToFirst = Node(idA1A2ToFirst, DOperationA1A2ToFirst())

  def validGraph: Graph = Graph(
    nodes = Set(nodeCreateA1, nodeAToA1A2, nodeA1A2ToFirst),
    edges = Set(
      Edge(nodeCreateA1, 0, nodeAToA1A2, 0),
      Edge(nodeAToA1A2, 0, nodeA1A2ToFirst, 0),
      Edge(nodeAToA1A2, 1, nodeA1A2ToFirst, 1))
  )

  def graphWithNotAccordingTypes: Graph = Graph(
    nodes = Set(nodeCreateA1, nodeA1ToA, nodeAToA1A2, nodeA1A2ToFirst),
    edges = Set(
      Edge(nodeCreateA1, 0, nodeA1ToA, 0),
      Edge(nodeCreateA1, 0, nodeAToA1A2, 0),
      Edge(nodeAToA1A2, 1, nodeA1A2ToFirst, 0),
      Edge(nodeA1ToA, 0, nodeA1A2ToFirst, 1))
  )

  def graphWithNotProvidedInputs: Graph = Graph(
    nodes = Set(nodeCreateA1, nodeA1A2ToFirst),
    edges = Set(Edge(nodeCreateA1, 0, nodeA1A2ToFirst, 0))
  )

  def graphWithCycle: Graph = new Graph() {
    override def topologicallySorted: Option[List[Node]] = None
  }

  def setParamsValid(graph: Graph): Unit = setInGraph(graph, _.setParamsValid())

  def setParamsInvalid(graph: Graph): Unit = setInGraph(graph, _.setParamsInvalid())

  def setThrowingError(graph: Graph): Unit = setInGraph(graph, _.setInferenceErrorThrowing())

  def setInGraph(graph: Graph, f: DOperationA1A2ToFirst => Unit): Unit = {
    val node = graph.node(idA1A2ToFirst)
    f(node.operation.asInstanceOf[DOperationA1A2ToFirst])
  }
}
