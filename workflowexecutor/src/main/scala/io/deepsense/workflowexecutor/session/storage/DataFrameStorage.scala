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

package io.deepsense.workflowexecutor.session.storage

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowexecutor.session.storage.DataFrameStorage.DataFrameName

trait ReadOnlyDataFrameStorage {

  /**
   * Returns stored dataframe.
   *
   * @param workflowId workflow id.
   * @param dataFrameName dataframe name.
   * @return stored dataframe.
   */
  def get(
      workflowId: Workflow.Id,
      dataFrameName: DataFrameName): Option[DataFrame]

  /**
   * Lists dataframes stored for workflow with specified id.
   *
   * @param workflowId workflow id.
   * @return names of stored dataframes.
   */
  def listDataFrameNames(workflowId: Workflow.Id): Iterable[DataFrameName]
}

trait DataFrameStorage extends ReadOnlyDataFrameStorage {

  /**
   * Stores dataframe under specified name.
   *
   * @param workflowId workflow id.
   * @param dataFrameName dataframe name.
   * @param dataFrame dataframe.
   */
  def put(
      workflowId: Workflow.Id,
      dataFrameName: DataFrameName,
      dataFrame: DataFrame): Unit
}

object DataFrameStorage {
  type DataFrameName = String
  type DataFrameId = (Workflow.Id, DataFrameName)
}
