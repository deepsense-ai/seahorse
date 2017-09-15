/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.e2etests

import spray.json._

import ai.deepsense.commons.utils.{Logging, Version}
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.json.workflow.WorkflowVersionUtil
import ai.deepsense.models.workflows.WorkflowWithVariables
import ai.deepsense.workflowmanager.rest.CurrentBuild

class WorkflowJsonConverter(override val graphReader: GraphReader)
    extends Logging
    with WorkflowVersionUtil {
  override def currentVersion: Version = CurrentBuild.version

  def parseWorkflow(raw: String): WorkflowWithVariables = {
    raw.parseJson.convertTo[WorkflowWithVariables](versionedWorkflowWithVariablesReader)
  }

  def printWorkflow(
      workflowWithVariables: WorkflowWithVariables,
      prettyPrint: Boolean = false): String = {
    val json = workflowWithVariables.toJson
    if (prettyPrint) {
      json.prettyPrint
    } else {
      json.compactPrint
    }
  }
}
