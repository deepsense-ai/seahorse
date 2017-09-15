/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang

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
    val fullInference: Boolean = false)
  extends CommonContext

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
