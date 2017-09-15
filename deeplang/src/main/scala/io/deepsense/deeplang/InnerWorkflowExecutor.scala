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

package io.deepsense.deeplang

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.params.custom.InnerWorkflow

trait InnerWorkflowParser {

  /**
   * Parses inner workflow.
   *
   * @param workflow string containing workflow representation.
   * @return inner workflow as object.
   */
  def parse(workflow: String): InnerWorkflow
}

trait InnerWorkflowExecutor extends InnerWorkflowParser {

  /**
   * Executes inner workflow.
   *
   * @param executionContext execution context.
   * @param workflow workflow to execute.
   * @param dataFrame input DataFrame for source node.
   * @return output DataFrame of sink node.
   */
  def execute(
      executionContext: CommonExecutionContext,
      workflow: InnerWorkflow,
      dataFrame: DataFrame): DataFrame
}
