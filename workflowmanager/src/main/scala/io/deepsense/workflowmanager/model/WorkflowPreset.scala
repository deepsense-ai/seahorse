/**
 * Copyright 2016, deepsense.ai
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

package io.deepsense.workflowmanager.model

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.models.workflows.Workflow

case class WorkflowPreset(id: Workflow.Id, presetId: Long)

trait WorkflowPresetJsonProtocol extends DefaultJsonProtocol
  with IdJsonProtocol
  with SprayJsonSupport {
  implicit val workflowPresetJsonFormat = jsonFormat2(WorkflowPreset)
}

object WorkflowPresetJsonProtocol extends WorkflowPresetJsonProtocol

