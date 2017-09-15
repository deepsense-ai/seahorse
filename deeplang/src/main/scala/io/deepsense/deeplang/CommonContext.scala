/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang

import io.deepsense.deeplang.doperables.dataframe.DataFrameBuilder
import io.deepsense.entitystorage.EntityStorageClient

/**
 * Base class for contexts used in experiment.
 * Contains fields that are used in all contexts.
 */
class CommonContext {
  var dataFrameBuilder: DataFrameBuilder = _
  var entityStorageClient: EntityStorageClient = _
  var tenantId: String = _
}
