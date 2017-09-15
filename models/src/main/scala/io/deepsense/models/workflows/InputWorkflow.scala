/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.models.workflows

import org.joda.time.DateTime

import io.deepsense.commons.auth.HasTenantId
import io.deepsense.graph.Graph

/**
 * An experiment's representation used when creating a new experiment.
 * During creation of a new experiment API's client does not have to (and can not)
 * specify all fields that an experiment has. This class purpose is to avoid
 * optional fields in the experiment.
 */
case class InputWorkflow(name: String, description: String = "", graph: Graph)
  extends BaseWorkflow(name, description, graph) {

  /**
   * Creates an experiment upon the input experiment.
   * @param owner The owner of the created experiment.
   * @return An experiment owned by the owner.
   */
  def toWorkflowOf(owner: HasTenantId, creationDateTime: DateTime): Workflow = {
    Workflow(
      Workflow.Id.randomId  ,
      owner.tenantId,
      name,
      graph,
      creationDateTime,
      creationDateTime,
      description)
  }
}
