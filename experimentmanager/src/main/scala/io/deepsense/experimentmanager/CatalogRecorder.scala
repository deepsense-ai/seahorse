/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.experimentmanager

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations._

/**
 * Object used to register all desired DOperables and DOperations.
 */
object CatalogRecorder {

  def registerDOperables(catalog: DOperableCatalog) = {
    catalog.registerDOperable[DataFrame]()
    catalog.registerDOperable[UntrainedRidgeRegression]()
    catalog.registerDOperable[TrainedRidgeRegression]()
    catalog.registerDOperable[Report]()
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

    catalog.registerDOperation[DataFrameSplitter](
      DOperationCategories.Utils,
      "Splits DataFrame into two DataFrames")

    catalog.registerDOperation[CreateRidgeRegression](
      DOperationCategories.ML.Regression,
      "Creates untrained ridge regression model")

    catalog.registerDOperation[TrainRegressor](
      DOperationCategories.ML.Regression,
      "Trains linear regression model")

    catalog.registerDOperation[ScoreRegressor](
      DOperationCategories.ML.Regression,
      "Scores trained linear regression model")

    catalog.registerDOperation[CrossValidateRegressor](
      DOperationCategories.ML.Regression,
      "Cross-validates regression model")

    catalog.registerDOperation[OneHotEncoder](
      DOperationCategories.Utils,
      "One-hot encodes categorical columns")

    catalog.registerDOperation[FileToDataFrame](
      DOperationCategories.IO,
      "Converts a file to a DataFrame"
    )
  }
}
