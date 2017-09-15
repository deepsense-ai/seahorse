/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.experimentmanager

import java.util.UUID

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.dataframe.DataFrame
import io.deepsense.deeplang.doperations.{ReadDataFrame, TimestampDecomposer, WriteDataFrame}

/**
 * Object used to register all desired DOperables and DOperations.
 */
object CatalogRecorder {

  def registerDOperables(catalog: DOperableCatalog) = {
    catalog.registerDOperable[DataFrame]()
  }

  def registerDOperations(catalog: DOperationsCatalog) = {
    catalog.registerDOperation[ReadDataFrame](
      UUID.randomUUID(),
      DOperationCategories.IO,
      "Reads DataFrame from HDFS")

    catalog.registerDOperation[WriteDataFrame](
      UUID.randomUUID(),
      DOperationCategories.IO,
      "Writes DataFrame to HDFS")

    catalog.registerDOperation[TimestampDecomposer](
      UUID.randomUUID(),
      DOperationCategories.Utils,
      "Decomposes selected columns from timestamp to numeric")
  }

}
