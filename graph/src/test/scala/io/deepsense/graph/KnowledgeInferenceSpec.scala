/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graph

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

  val ctx: InferContext = new InferContext(hierarchy, fullInference = false)

  "Graph" should {
    "infer type knowledge" when {
      "graph is valid" in {
        val graph = validGraph
        setParamsValid(graph)
        val graphKnowledge = graph.inferKnowledge(ctx)

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
        val graphKnowledge = graph.inferKnowledge(ctx)

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
        val graphKnowledge = graph.inferKnowledge(ctx)

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
        val graphKnowledge = graph.inferKnowledge(ctx)

        val graphKnowledgeExpected = Map(
          idCreateA1 -> NodeInferenceResult(Vector(knowledgeA1)),
          idAToA1A2 -> NodeInferenceResult(Vector(knowledgeA1, knowledgeA2)),
          idA1A2ToFirst -> NodeInferenceResult(
            Vector(knowledgeA12),
            errors = Vector(DOperationA1A2ToFirst.parameterInvalidError))
        )
        graphKnowledge.resultsMap should contain theSameElementsAs graphKnowledgeExpected
      }
      "not all input knowledge accords to input type qualifiers and params are invalid" in {
        val graph = graphWithNotAccordingTypes
        setParamsInvalid(graph)
        val graphKnowledge = graph.inferKnowledge(ctx)

        val graphKnowledgeExpected = Map(
          idCreateA1 -> NodeInferenceResult(Vector(knowledgeA1)),
          idA1ToA -> NodeInferenceResult(Vector(knowledgeA12)),
          idAToA1A2 -> NodeInferenceResult(Vector(knowledgeA1, knowledgeA2)),
          idA1A2ToFirst -> NodeInferenceResult(
            Vector(knowledgeA12),
            warnings = InferenceWarnings(SomeTypesNotCompilableWarning(portIndex = 1)),
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
        val graphKnowledge = graph.inferKnowledge(ctx)

        val graphKnowledgeExpected = Map(
          idCreateA1 -> NodeInferenceResult(Vector(knowledgeA1)),
          idA1A2ToFirst -> NodeInferenceResult(
            Vector(knowledgeA12),
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
        val graphKnowledge = graph.inferKnowledge(ctx)

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
        val graphKnowledge = graph.inferKnowledge(ctx)

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
        val graphKnowledge = graph.inferKnowledge(ctx)

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

    "throw an exception" when {
      "graph contains cycle" in {
        intercept[CyclicGraphException] {
          graphWithCycle.inferKnowledge(ctx)
        }
        ()
      }
    }
  }

  it should {
    "infer knowledge up to given output port" in {
      val graph = validGraph
      setParamsValid(graph)

      val node1Result = graph.inferKnowledge(idCreateA1, 0, ctx)
      val node2Port0Result = graph.inferKnowledge(idAToA1A2, 0, ctx)
      val node2Port1Result = graph.inferKnowledge(idAToA1A2, 1, ctx)
      val node3Result = graph.inferKnowledge(idA1A2ToFirst, 0, ctx)

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

    def setParamsValid(): Unit = doNothing.when(parameters).validate

    def setParamsInvalid(): Unit = doThrow(parameterInvalidError).when(parameters).validate

    private var inferenceShouldThrow = false

    def setInferenceErrorThrowing(): Unit = inferenceShouldThrow = true

    override protected def _inferTypeKnowledge(
      context: InferContext)(
      k0: DKnowledge[A1],
      k1: DKnowledge[A2]): (DKnowledge[A], InferenceWarnings) = {
      if (inferenceShouldThrow) {
        throw inferenceError
      }
      (DKnowledge(A1()), InferenceWarnings(warning))
    }
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
