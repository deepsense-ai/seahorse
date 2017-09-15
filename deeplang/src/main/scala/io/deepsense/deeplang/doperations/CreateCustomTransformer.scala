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

package io.deepsense.deeplang.doperations

import scala.reflect.runtime.universe.TypeTag
import spray.json._

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang._
import io.deepsense.deeplang.documentation.OperationDocumentation
import io.deepsense.deeplang.doperables.CustomTransformer
import io.deepsense.deeplang.doperations.custom.{Sink, Source}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.{Param, WorkflowParam}
import io.deepsense.deeplang.utils.CustomTransformerFactory
import io.deepsense.graph.{GraphKnowledge, Node}

case class CreateCustomTransformer() extends TransformerAsFactory[CustomTransformer] with OperationDocumentation  {

  import DefaultCustomTransformerWorkflow._

  override val id: Id = CreateCustomTransformer.id
  override val name: String = "Create Custom Transformer"
  override val description: String = "Creates custom transformer"

  override val since: Version = Version(1, 0, 0)

  val innerWorkflow = WorkflowParam(
    name = "inner workflow",
    description = "Inner workflow of the Transformer.")
  setDefault(innerWorkflow, defaultWorkflow)

  // Inner workflow should be of type InnerWorkflow instead of raw json.
  // Unfortunetely it's currently impossible because of project dependency graph.
  // We have executor -> json -> deeplang.
  // Unfortunetely json parsing for parameters is in deeplang. And InnerWorkflow is parameter.
  // Current workaround is to pass innerWorkflowParser through context.
  // TODO If we stick to custom transformer in current form merge those two project
  // and use InnerWorkflow type here instead of raw json.

  def getInnerWorkflow: JsObject = $(innerWorkflow)
  def setInnerWorkflow(workflow: JsObject): this.type = set(innerWorkflow, workflow)

  override val params: Array[Param[_]] = Array(innerWorkflow)

  override protected def execute()(context: ExecutionContext): CustomTransformer =
    customTransformer(context.innerWorkflowExecutor)

  override def inferKnowledge()(context: InferContext): (DKnowledge[CustomTransformer], InferenceWarnings) = {
    val transformer = customTransformer(context.innerWorkflowParser)
    (DKnowledge[CustomTransformer](transformer), InferenceWarnings.empty)
  }

  override def inferGraphKnowledgeForInnerWorkflow(context: InferContext): GraphKnowledge = {
    val innerWorkflowValue = context.innerWorkflowParser.parse($(innerWorkflow))
    innerWorkflowValue.graph.inferKnowledge(context, GraphKnowledge())
  }

  private def customTransformer(innerWorkflowParser: InnerWorkflowParser): CustomTransformer = {
    CustomTransformerFactory.createCustomTransformer(innerWorkflowParser, $(innerWorkflow))
  }
}

object CreateCustomTransformer {
  val id: Id = "65240399-2987-41bd-ba7e-2944d60a3404"
}

object DefaultCustomTransformerWorkflow {

  private def node(operation: DOperation, nodeId: Node.Id): JsObject =
    JsObject(
      "id" -> JsString(nodeId.toString),
      "operation" -> JsObject(
        "id" -> JsString(operation.id.toString),
        "name" -> JsString(operation.name)
      ),
      "parameters" -> JsObject()
    )

  private def connection(from: Node.Id, to: Node.Id): JsObject =
    JsObject(
      "from" -> JsObject(
        "nodeId" -> JsString(from.toString),
        "portIndex" -> JsNumber(0)
      ),
      "to" -> JsObject(
        "nodeId" -> JsString(to.toString),
        "portIndex" -> JsNumber(0)
      )
    )

  private val sourceNodeId: Node.Id = "2603a7b5-aaa9-40ad-9598-23f234ec5c32"
  private val sinkNodeId: Node.Id = "d7798d5e-b1c6-4027-873e-a6d653957418"

  val defaultWorkflow = JsObject(
    "workflow" -> JsObject(
      "nodes" -> JsArray(node(Source(), sourceNodeId), node(Sink(), sinkNodeId)),
      "connections" -> JsArray(connection(sourceNodeId, sinkNodeId))
    ),
    "thirdPartyData" -> JsObject(),
    "publicParams" -> JsArray()
  )
}
