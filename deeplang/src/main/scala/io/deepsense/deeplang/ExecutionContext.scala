/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import io.deepsense.deeplang.dataframe.DataFrameBuilder
import org.apache.spark.sql.SQLContext

import io.deepsense.entitystorage.EntityStorageClient

/** Holds information needed by DOperations and DMethods during execution. */
class ExecutionContext {
  var sqlContext: SQLContext = _
  var dataFrameBuilder: DataFrameBuilder = _
  var entityStorageClient: EntityStorageClient = _
  var tenantId: String = _
}
