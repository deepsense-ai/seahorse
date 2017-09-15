/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.dataframe.DataFrameBuilder
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.entitystorage.{EntityStorageClient, UniqueFilenameUtil}

/** Holds information needed by DOperations and DMethods during execution. */
class ExecutionContext(
    override val dOperableCatalog: DOperableCatalog)
  extends InferContext(dOperableCatalog, fullInference = true) {

  var sparkContext: SparkContext = _
  var sqlContext: SQLContext = _
  var hdfsClient: DSHdfsClient = _

  def uniqueHdfsFileName(entityCategory: String): String =
    UniqueFilenameUtil.getUniqueHdfsFilename(tenantId, entityCategory)
}
