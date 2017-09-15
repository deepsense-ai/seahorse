/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.rest.json

import java.util.UUID

import org.joda.time.DateTime
import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.json.{DateTimeJsonProtocol, ExceptionsJsonProtocol, IdJsonProtocol}
import io.deepsense.commons.json.envelope.EnvelopeJsonFormat
import io.deepsense.deeplang.InferContext
import io.deepsense.experimentmanager.models.{Count, ExperimentsList}
import io.deepsense.graph.Graph
import io.deepsense.graphjson.GraphJsonProtocol.{GraphReader, GraphWriter}
import io.deepsense.graphjson._
import io.deepsense.models.experiments.{Experiment, InputExperiment}


trait ExperimentJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with NodeJsonProtocol
  with NodeStateJsonProtocol
  with GraphKnowledgeJsonProtocol
  with ActionsJsonProtocol
  with IdJsonProtocol
  with ExceptionsJsonProtocol
  with DateTimeJsonProtocol {

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
    val Status = "status"
    val Error = "error"
    val Nodes = "nodes"
    val Created = "created"
    val Updated = "updated"
    val TypeKnowledge = "typeKnowledge"

    override def read(json: JsValue): Experiment = json match {
      case JsObject(fields) =>
        val id = UUID.fromString(fields(Id).convertTo[String])
        val tenantId = fields(TenantId).convertTo[String]
        val name = fields(Name).convertTo[String]
        val description = fields(Description).convertTo[String]
        val graph = fields(Graph).convertTo[Graph]
        val created = fields(Created).convertTo[DateTime]
        val updated = fields(Updated).convertTo[DateTime]
        Experiment(id, tenantId, name, graph, created, updated, description)
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
        Created -> experiment.created.toJson,
        Updated -> experiment.updated.toJson,
        State -> JsObject(
          Status -> JsString(experiment.state.status.toString),
          Error -> experiment.state.error.map(JsString(_)).getOrElse(JsNull),
          Nodes -> JsObject(
            experiment.graph.nodes.map(node => {
              node.id.value.toString -> node.state.toJson
            }).toMap)
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
  implicit val countFormat = jsonFormat2(Count)
  implicit val experimentsListFormat = jsonFormat2(ExperimentsList)
  val experimentEnvelopeLabel = "experiment"
  implicit val experimentEnvelopeFormat =
    new EnvelopeJsonFormat[Experiment](experimentEnvelopeLabel)
  implicit val inputExperimentEnvelopeFormat =
    new EnvelopeJsonFormat[InputExperiment](experimentEnvelopeLabel)
}
