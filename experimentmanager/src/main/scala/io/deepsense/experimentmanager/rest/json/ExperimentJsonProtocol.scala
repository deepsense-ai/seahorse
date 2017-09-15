/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.rest.json

import java.util.UUID

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.json.{ExceptionsJsonProtocol, IdJsonProtocol}
import io.deepsense.deeplang.InferContext
import io.deepsense.experimentmanager.models.{Experiment, InputExperiment}
import io.deepsense.graph.Graph
import io.deepsense.graphjson.GraphJsonProtocol.{GraphReader, GraphWriter}
import io.deepsense.graphjson._

trait ExperimentJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with NodeJsonProtocol
  with NodeStateJsonProtocol
  with GraphKnowledgeJsonProtocol
  with ActionsJsonProtocol
  with IdJsonProtocol
  with ExceptionsJsonProtocol {

  val graphReader: GraphReader
  val inferContext: InferContext

  implicit val graphFormat: JsonFormat[Graph] = new JsonFormat[Graph] {
    override def read(json: JsValue): Graph = json.convertTo[Graph](graphReader)
    override def write(obj: Graph): JsValue = obj.toJson(GraphWriter)
  }

  implicit object ExperimentFormat extends RootJsonFormat[Experiment] {

    val Id = "id"
    val TenantId = "tenantId"
    val Name = "name"
    val Description = "description"
    val Graph = "graph"
    val State = "state"
    val Nodes = "nodes"
    val TypeKnowledge = "typeKnowledge"

    override def read(json: JsValue): Experiment = json match {
      case JsObject(fields) =>
        val id = UUID.fromString(fields(Id).convertTo[String])
        val tenantId = fields(TenantId).convertTo[String]
        val name = fields(Name).convertTo[String]
        val description = fields(Description).convertTo[String]
        val graph = fields(Graph).convertTo[Graph]
        Experiment(id, tenantId, name, graph, description)
      case x => throw new DeserializationException("Could not read experiment. " +
        s"Expected JsObject but got $x")
    }

    override def write(experiment: Experiment): JsValue = {
      val knowledge = experiment.graph.inferKnowledge(inferContext)
      JsObject(
        Id -> experiment.id.value.toString.toJson,
        TenantId -> experiment.tenantId.toJson,
        Name -> experiment.name.toJson,
        Description -> experiment.description.toJson,
        Graph -> experiment.graph.toJson(graphFormat),
        State -> JsObject(
          // TODO "status" -> ...
          Nodes -> JsObject(
            experiment.graph.nodes.map(node => {
              node.id.value.toString -> node.state.toJson
            }).toMap
          )
        ),
        TypeKnowledge -> JsObject(
          experiment.graph.nodes.map(node => {
            node.id.value.toString -> knowledge.getKnowledge(node.id).toJson
          }).toMap
        )
      )
    }
  }

  implicit val inputExperimentFormat = jsonFormat3(InputExperiment.apply)
}
