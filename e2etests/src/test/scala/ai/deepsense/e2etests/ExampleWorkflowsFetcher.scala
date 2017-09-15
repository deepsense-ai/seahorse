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

import scala.concurrent.Future

import ai.deepsense.models.workflows.WorkflowInfo

trait ExampleWorkflowsFetcher {
  self: SeahorseIntegrationTestDSL =>

  import scala.concurrent.ExecutionContext.Implicits.global

  def fetchExampleWorkflows(): Future[Seq[WorkflowInfo]] = {
    val workflowsFut = wmclient.fetchWorkflows()
    val exampleWorkflows = workflowsFut.map(_.filter(_.ownerName == "seahorse"))
    // Order matters because of
    // - `Write Transformer` in Example 1
    // - `Read Transformer` in Example 4
    exampleWorkflows.map(_.sortBy(_.name))
  }
}
