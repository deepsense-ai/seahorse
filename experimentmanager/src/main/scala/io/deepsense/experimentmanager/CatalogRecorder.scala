/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.experimentmanager

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.dataframe.DataFrame
import io.deepsense.deeplang.doperations._

/**
 * Object used to register all desired DOperables and DOperations.
 */
object CatalogRecorder {

  def registerDOperables(catalog: DOperableCatalog) = {
    catalog.registerDOperable[DataFrame]()
  }

  def registerDOperations(catalog: DOperationsCatalog) = {
    catalog.registerDOperation[ReadDataFrame](
      DOperationCategories.IO,
      "Reads DataFrame from HDFS")

    catalog.registerDOperation[WriteDataFrame](
      DOperationCategories.IO,
      "Writes DataFrame to HDFS")

    catalog.registerDOperation[TimestampDecomposer](
      DOperationCategories.Utils,
      "Decomposes selected columns from timestamp to numeric")

    catalog.registerDOperation[DataFrameSpliter](
      DOperationCategories.Utils,
      "Splits DataFrame into two DataFrames")
  }

}
