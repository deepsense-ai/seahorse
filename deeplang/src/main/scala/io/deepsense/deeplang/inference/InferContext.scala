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

package io.deepsense.deeplang.inference

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.dataframe.DataFrameBuilder
import io.deepsense.entitystorage.EntityStorageClient

/**
 * Holds information needed by DOperations and DMethods during knowledge inference.
 * @param dOperableCatalog object responsible for registering and validating the type hierarchy
 * @param fullInference if set to true, infer metadata and types; otherwise infer the types only
 */
class InferContext(
    val dOperableCatalog: DOperableCatalog,
    val fullInference: Boolean = false) {

  var dataFrameBuilder: DataFrameBuilder = _
  var entityStorageClient: EntityStorageClient = _
  var tenantId: String = _
}

object InferContext {

  def apply(dOperableCatalog: DOperableCatalog, fullInference: Boolean = false): InferContext =
    new InferContext(dOperableCatalog, fullInference)

  def apply(baseContext: InferContext, fullInference: Boolean): InferContext = {
    val context = new InferContext(baseContext.dOperableCatalog, fullInference)
    context.dataFrameBuilder = baseContext.dataFrameBuilder
    context.entityStorageClient = baseContext.entityStorageClient
    context.tenantId = baseContext.tenantId
    context
  }
}
