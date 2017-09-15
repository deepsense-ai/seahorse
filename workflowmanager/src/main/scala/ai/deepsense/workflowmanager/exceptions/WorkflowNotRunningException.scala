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

package ai.deepsense.workflowmanager.exceptions

import ai.deepsense.commons.exception.FailureCode
import ai.deepsense.commons.models.Id

class WorkflowNotRunningException(workflowId: Id)
  extends WorkflowManagerException(
    FailureCode.CannotUpdateRunningWorkflow,
    "Workflow is not running and cannot be aborted",
    s"Workflow with id $workflowId is not running and can not be aborted.") {
  override protected def additionalDetails: Map[String, String] =
    Map("workflowId" -> workflowId.toString)
}

