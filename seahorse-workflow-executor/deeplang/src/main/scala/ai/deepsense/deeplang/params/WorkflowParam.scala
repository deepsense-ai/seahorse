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

import spray.json._
import spray.json.DefaultJsonProtocol._

import ai.deepsense.deeplang.exceptions.DeepLangException
import ai.deepsense.deeplang.params.custom.InnerWorkflow
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.json.workflow.{InnerWorkflowJsonReader, WriteInnerWorkflowJsonProtocol}

case class WorkflowParam(
    override val name: String,
    override val description: Option[String])
  extends Param[InnerWorkflow]
  with WriteInnerWorkflowJsonProtocol {

  override val parameterType = ParameterType.Workflow

  override def valueToJson(value: InnerWorkflow): JsValue = value.toJson


  override def valueFromJson(jsValue: JsValue, graphReader: GraphReader): InnerWorkflow =
    InnerWorkflowJsonReader.toInner(jsValue, graphReader)

  override def validate(value: InnerWorkflow): Vector[DeepLangException] = {
    super.validate(value)
  }

  override def replicate(name: String): WorkflowParam = copy(name = name)
}
