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

package ai.deepsense.deeplang.params

import spray.json.{JsString, _}
import ai.deepsense.deeplang.DOperationCategories
import ai.deepsense.deeplang.catalogs.SortPriority
import ai.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import ai.deepsense.deeplang.doperations.custom.{Sink, Source}
import ai.deepsense.deeplang.params.custom.InnerWorkflow
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

class WorkflowParamSpec extends AbstractParamSpec[InnerWorkflow, WorkflowParam] {

  override def className: String = "WorkflowParam"

  override def graphReader: GraphReader = {
    val catalog = DOperationsCatalog()
    catalog.registerDOperation(DOperationCategories.IO, () => Source(), SortPriority.coreDefault)
    catalog.registerDOperation(DOperationCategories.IO, () => Sink(), SortPriority.coreDefault)
    new GraphReader(catalog)
  }

  override def paramFixture: (WorkflowParam, JsValue) = {
    val description = "Workflow parameter description"
    val param = WorkflowParam(
      name = "Workflow parameter name",
      description = Some(description))
    val expectedJson = JsObject(
      "type" -> JsString("workflow"),
      "name" -> JsString(param.name),
      "description" -> JsString(description),
      "isGriddable" -> JsFalse,
      "default" -> JsNull
    )
    (param, expectedJson)
  }

  override def valueFixture: (InnerWorkflow, JsValue) = {
    val innerWorkflow = InnerWorkflow.empty
    val sourceNode = JsObject(
      "id" -> JsString(innerWorkflow.source.id.toString),
      "operation" -> JsObject(
        "id" -> JsString(Source.id.toString),
        "name" -> JsString("Source")
      ),
    "parameters" -> JsObject()
    )
    val sinkNode = JsObject(
      "id" -> JsString(innerWorkflow.sink.id.toString),
      "operation" -> JsObject(
        "id" -> JsString(Sink.id.toString),
        "name" -> JsString("Sink")
      ),
      "parameters" -> JsObject()
    )
    val workflow = JsObject(
      "nodes" -> JsArray(sourceNode, sinkNode),
      "connections" -> JsArray()
    )
    val value = JsObject(
      "workflow" -> workflow,
      "thirdPartyData" -> JsObject(),
      "publicParams" -> JsArray()
    )
    (innerWorkflow, value)
  }
}
