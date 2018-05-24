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

package ai.deepsense.deeplang.doperations

import java.util.UUID

import scala.reflect.runtime.universe.TypeTag

import spray.json._

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang._
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.CustomTransformer
import ai.deepsense.deeplang.doperations.custom.{Sink, Source}
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.custom.InnerWorkflow
import ai.deepsense.deeplang.params.{Param, WorkflowParam}
import ai.deepsense.deeplang.utils.CustomTransformerFactory
import ai.deepsense.graph._
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.json.workflow.InnerWorkflowJsonReader


case class CreateCustomTransformer() extends TransformerAsFactory[CustomTransformer] with OperationDocumentation  {

  override val id: Id = CreateCustomTransformer.id
  override val name: String = "Create Custom Transformer"
  override val description: String = "Creates custom transformer"

  override val since: Version = Version(1, 0, 0)

  val innerWorkflow = WorkflowParam(
    name = "inner workflow",
    description = None)
  setDefault(innerWorkflow, CreateCustomTransformer.default)

  def getInnerWorkflow: InnerWorkflow = $(innerWorkflow)
  def setInnerWorkflow(workflow: JsObject, graphReader: GraphReader): this.type =
    set(innerWorkflow, InnerWorkflowJsonReader.toInner(workflow, graphReader))
  def setInnerWorkflow(workflow: InnerWorkflow): this.type = set(innerWorkflow, workflow)

  override val specificParams: Array[Param[_]] = Array(innerWorkflow)

  override lazy val tTagTO_0: TypeTag[CustomTransformer] = typeTag

  override def getDatasourcesIds: Set[UUID] = getInnerWorkflow.getDatasourcesIds

  override protected def execute()(context: ExecutionContext): CustomTransformer =
    CustomTransformerFactory.createCustomTransformer($(innerWorkflow))

  override def inferKnowledge()(context: InferContext): (DKnowledge[CustomTransformer], InferenceWarnings) = {
    val transformer = CustomTransformerFactory.createCustomTransformer($(innerWorkflow))
    (DKnowledge[CustomTransformer](transformer), InferenceWarnings.empty)
  }

  override def inferGraphKnowledgeForInnerWorkflow(context: InferContext): GraphKnowledge = {
    val innerWorkflowValue = getInnerWorkflow
    innerWorkflowValue.graph.inferKnowledge(context, GraphKnowledge())
  }

}

object CreateCustomTransformer {
  val id: Id = "65240399-2987-41bd-ba7e-2944d60a3404"

  private val sourceNodeId: Node.Id = "2603a7b5-aaa9-40ad-9598-23f234ec5c32"
  private val sinkNodeId: Node.Id = "d7798d5e-b1c6-4027-873e-a6d653957418"

  val default = InnerWorkflow(
    DeeplangGraph(
      nodes = Set(Node(sourceNodeId, Source()), Node(sinkNodeId, Sink())),
      edges = Set(Edge(Endpoint(sourceNodeId, 0), Endpoint(sinkNodeId, 0)))),
    JsObject())
}
