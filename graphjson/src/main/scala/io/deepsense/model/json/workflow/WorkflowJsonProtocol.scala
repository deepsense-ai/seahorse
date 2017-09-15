/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.model.json.workflow

import org.joda.time.DateTime
import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.commons.json.envelope.EnvelopeJsonFormat
import io.deepsense.commons.json.{DateTimeJsonProtocol, ExceptionsJsonProtocol, IdJsonProtocol}
import io.deepsense.deeplang.DOperable.AbstractMetadata
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.{DKnowledge, DOperable}
import io.deepsense.graph.Graph
import io.deepsense.model.json.graph.GraphJsonProtocol.{GraphReader, GraphWriter}
import io.deepsense.model.json.graph.{GraphKnowledgeJsonProtocol, NodeJsonProtocol, NodeStateJsonProtocol}
import io.deepsense.models.workflows.{Count, InputWorkflow, Workflow, WorkflowsList}

trait WorkflowJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with NodeJsonProtocol
  with NodeStateJsonProtocol
  with GraphKnowledgeJsonProtocol
  with ActionsJsonProtocol
  with IdJsonProtocol
  with ExceptionsJsonProtocol
  with DateTimeJsonProtocol
  with AbstractMetadataJsonProtocol
  with MetadataInferenceResultJsonProtocol
  with InferenceErrorJsonProtocol
  with InferenceWarningJsonProtocol {

  val graphReader: GraphReader
  val inferContext: InferContext

  implicit val graphFormat: JsonFormat[Graph] = new JsonFormat[Graph] {
    override def read(json: JsValue): Graph = json.convertTo[Graph](graphReader)
    override def write(obj: Graph): JsValue = obj.toJson(GraphWriter)
  }

  implicit val experimentErrorFormat = jsonFormat5(FailureDescription.apply)

  implicit object WorkflowFormat extends RootJsonFormat[Workflow] {

    val Id = "id"
    val TenantId = "tenantId"
    val Name = "name"
    val Description = "description"
    val Graph = "graph"
    val State = "state"
    val Status = "status"
    val StateError = "error"
    val Nodes = "nodes"
    val Created = "created"
    val Updated = "updated"

    val Knowledge = "knowledge"

    val TypeKnowledge = "typeKnowledge"
    val Metadata = "metadata"
    val Warnings = "warnings"
    val Errors = "errors"

    override def read(json: JsValue): Workflow = json match {
      case JsObject(fields) =>
        val id = Workflow.Id.fromString(fields(Id).convertTo[String])
        val tenantId = fields(TenantId).convertTo[String]
        val name = fields(Name).convertTo[String]
        val description = fields(Description).convertTo[String]
        val graph = fields(Graph).convertTo[Graph]
        val created = fields(Created).convertTo[DateTime]
        val updated = fields(Updated).convertTo[DateTime]
        Workflow(id, tenantId, name, graph, created, updated, description)
      case x => throw new DeserializationException("Could not read experiment. " +
        s"Expected JsObject but got $x")
    }

    override def write(experiment: Workflow): JsValue = {
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
          StateError -> experiment.state.error.toJson,
          Nodes -> JsObject(
            experiment.graph.nodes.map {node =>
              node.id.value.toString -> node.state.toJson
            }.toMap)
        ),
        Knowledge -> JsObject(
          experiment.graph.nodes.map {node =>
            val inferenceResult = knowledge.getResult(node.id)
            node.id.value.toString ->
              JsObject(
                TypeKnowledge -> inferenceResult.knowledge.toJson,
                Metadata -> serializeMetadata(extractMetadata(inferenceResult.knowledge)),
                Warnings -> inferenceResult.warnings.warnings.toJson,
                Errors -> inferenceResult.errors.toJson
              )
          }.toMap
        )
      )
    }
  }

  private def serializeMetadata(metadata: Vector[Seq[Option[AbstractMetadata]]]) = {
    JsArray(metadata.map(_.toJson))
  }

  private def extractMetadata(nodeKnowledge: Vector[DKnowledge[DOperable]])
      : Vector[Seq[Option[AbstractMetadata]]] = {

    nodeKnowledge.map(
      knowledge =>
        knowledge.types.toSeq.map(operable => operable.inferredMetadata))
  }

  implicit val inputExperimentFormat = jsonFormat3(InputWorkflow.apply)
  implicit val countFormat = jsonFormat2(Count)
  implicit val experimentsListFormat = jsonFormat2(WorkflowsList)
  val experimentEnvelopeLabel = "experiment"
  implicit val experimentEnvelopeFormat =
    new EnvelopeJsonFormat[Workflow](experimentEnvelopeLabel)
  implicit val inputExperimentEnvelopeFormat =
    new EnvelopeJsonFormat[InputWorkflow](experimentEnvelopeLabel)
}
