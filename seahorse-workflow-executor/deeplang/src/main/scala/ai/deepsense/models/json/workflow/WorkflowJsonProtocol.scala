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

package ai.deepsense.models.json.workflow

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

import ai.deepsense.commons.exception.json.FailureDescriptionJsonProtocol
import ai.deepsense.commons.json.{DateTimeJsonProtocol, EnumerationSerializer, IdJsonProtocol}
import ai.deepsense.models.json.graph.{DKnowledgeJsonProtocol, NodeJsonProtocol, NodeStatusJsonProtocol}
import ai.deepsense.models.workflows.{Workflow, WorkflowMetadata, WorkflowType}

trait WorkflowJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with NodeJsonProtocol
  with NodeStatusJsonProtocol
  with DKnowledgeJsonProtocol
  with ActionsJsonProtocol
  with IdJsonProtocol
  with FailureDescriptionJsonProtocol
  with DateTimeJsonProtocol
  with InferenceErrorJsonProtocol
  with InferenceWarningJsonProtocol
  with WorkflowInfoJsonProtocol
  with GraphJsonProtocol {

  implicit val workflowTypeFormat = EnumerationSerializer.jsonEnumFormat(WorkflowType)

  implicit val workflowMetadataFormat = jsonFormat(WorkflowMetadata, "type", "apiVersion")

  implicit val workflowFormat = jsonFormat(Workflow.apply, "metadata", "workflow", "thirdPartyData")

}
