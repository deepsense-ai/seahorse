/**
 * Copyright 2015, deepsense.ai
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

package ai.deepsense.deeplang.inference

import ai.deepsense.commons.rest.client.datasources.DatasourceClient
import ai.deepsense.deeplang.InnerWorkflowParser
import ai.deepsense.deeplang.catalogs.CatalogPair
import ai.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import ai.deepsense.deeplang.doperables.dataframe.DataFrameBuilder
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

/**
 * Holds information needed by DOperations and DMethods during knowledge inference.
 * @param dOperableCatalog object responsible for registering and validating the type hierarchy
 */
case class InferContext(
    dataFrameBuilder: DataFrameBuilder,
    catalogPair: CatalogPair,
    datasourceClient: DatasourceClient) {
  def dOperableCatalog = catalogPair.dOperableCatalog
  def dOperationCatalog = catalogPair.dOperationsCatalog

  def graphReader = new GraphReader(dOperationCatalog)
}
