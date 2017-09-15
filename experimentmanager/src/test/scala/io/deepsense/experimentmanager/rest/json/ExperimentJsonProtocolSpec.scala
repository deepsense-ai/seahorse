/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest.json

import scala.reflect.runtime.{universe => ru}

import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito._
import spray.json._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.deeplang.{DKnowledge, DOperable, DOperation, InferContext}
import io.deepsense.graph.{Edge, Endpoint, Graph, Node}
import io.deepsense.graphjson.GraphJsonProtocol.GraphReader
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.experiments.Experiment.State

class ExperimentJsonProtocolSpec
  extends StandardSpec
  with UnitTestSupport
  with ExperimentJsonProtocol
  with HierarchyDescriptorJsonProtocol {

  val operable = mock[DOperable]

  val dOperableCatalog = mock[DOperableCatalog]
  when(dOperableCatalog.concreteSubclassesInstances(
    any(classOf[ru.TypeTag[DOperable]]))).thenReturn(Set(operable))

  override val inferContext: InferContext = mock[InferContext]
  when(inferContext.dOperableCatalog).thenReturn(dOperableCatalog)

  override val graphReader: GraphReader = mock[GraphReader]

  val operation1 = mockOperation(0, 1, DOperation.Id.randomId, "name1", "version1")
  val operation2 = mockOperation(1, 1, DOperation.Id.randomId, "name2", "version2")
  val operation3 = mockOperation(1, 1, DOperation.Id.randomId, "name3", "version3")
  val operation4 = mockOperation(2, 1, DOperation.Id.randomId, "name4", "version4")

  val node1 = Node(Node.Id.randomId, operation1)
  val node2 = Node(Node.Id.randomId, operation2)
  val node3 = Node(Node.Id.randomId, operation3)
  val node4 = Node(Node.Id.randomId, operation4)
  val nodes = Set(node1, node2, node3, node4)
  val preEdges = Set(
    (node1, node2, 0, 0),
    (node1, node3, 0, 0),
    (node2, node4, 0, 0),
    (node3, node4, 0, 1))
  val edges = preEdges.map(n => Edge(Endpoint(n._1.id, n._3), Endpoint(n._2.id, n._4)))
  val graph = Graph(nodes, edges)

  val experimentId = Experiment.Id.randomId
  val tenantId = "tenantId"
  val name = "testName"
  val description = "testDescription"
  val created = new DateTime(2015, 6, 5, 11, 25, DateTimeConverter.zone)
  val updated = new DateTime(2015, 6, 5, 12, 54, DateTimeConverter.zone)
  val experiment = Experiment(
    experimentId,
    tenantId,
    name,
    graph,
    created,
    updated,
    description,
    State.failed(FailureDescription(
      DeepSenseFailure.Id.randomId,
      FailureCode.UnexpectedError,
      "Error title",
      Some("This is a description of an error"))))

  "Experiment" should {
    "be properly transformed to Json" in {
      val experimentJson = experiment.toJson.asJsObject
      val graphJson = graph.toJson
      experimentJson.fields("name").convertTo[String] shouldBe name
      experimentJson.fields("description").convertTo[String] shouldBe description
      experimentJson.fields("graph") shouldBe graphJson

      DateTime.parse(experimentJson.fields("created").convertTo[String]) shouldBe created
      DateTime.parse(experimentJson.fields("updated").convertTo[String]) shouldBe updated

      val state = experimentJson.fields("state").asJsObject
      val status = state.fields("status").asInstanceOf[JsString].value
      status shouldBe experiment.state.status.toString
      val experimentError: FailureDescription = experiment.state.error.get
      state.fields("error") shouldBe JsObject(
        "id" -> JsString(experimentError.id.toString),
        "code" -> JsNumber(experimentError.code.id),
        "title" -> JsString(experimentError.title),
        "message" -> JsString(experimentError.message.get),
        "details" -> JsObject()
      )

      val nodeStatuses = state.fields("nodes").asJsObject
      graph.nodes.foreach(node => {
        nodeStatuses.fields(node.id.value.toString) shouldBe node.state.toJson
      })

      val typeKnowledge = experimentJson.fields("typeKnowledge").asJsObject
      val graphKnowledge = graph.inferKnowledge(null) // Null is OK because of mocked Operations.
      typeKnowledge.fields.foreach(keyValue => {
        val (nodeId, knowledgeJson) = keyValue
        knowledgeJson shouldBe graphKnowledge.getKnowledge(Node.Id.fromString(nodeId)).toJson
      })
    }
  }

  def mockOperation(
    inArity: Int,
    outArity: Int,
    id: DOperation.Id,
    name: String,
    version: String): DOperation = {
    val dOperation = mock[DOperation]
    when(dOperation.id).thenReturn(id)
    when(dOperation.name).thenReturn(name)
    when(dOperation.version).thenReturn(version)
    when(dOperation.inArity).thenReturn(inArity)
    when(dOperation.outArity).thenReturn(outArity)
    when(dOperation.inPortTypes).thenReturn(
      Vector.fill(inArity)(implicitly[ru.TypeTag[DOperable]]))
    when(dOperation.outPortTypes).thenReturn(
      Vector.fill(outArity)(implicitly[ru.TypeTag[DOperable]]))
    val knowledge = mock[DKnowledge[DOperable]]
    when(knowledge.types).thenReturn(Set[DOperable](mock[DOperable]))
    when(dOperation.inferKnowledge(anyObject())(anyObject())).thenReturn(
      Vector.fill(outArity)(knowledge))
    val parametersSchema = mock[ParametersSchema]
    when(dOperation.parameters).thenReturn(parametersSchema)
    dOperation
  }
}
