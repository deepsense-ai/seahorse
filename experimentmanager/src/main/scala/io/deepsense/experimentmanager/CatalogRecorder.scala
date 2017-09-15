/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.experimentmanager

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.file.File
import io.deepsense.deeplang.doperations._

/**
 * Object used to register all desired [[io.deepsense.deeplang.DOperable]]s
 * and [[io.deepsense.deeplang.DOperation]]s.
 */
object CatalogRecorder {

  def registerDOperables(catalog: DOperableCatalog) = {
    catalog.registerDOperable[File]()
    catalog.registerDOperable[DataFrame]()
    catalog.registerDOperable[UntrainedRidgeRegression]()
    catalog.registerDOperable[TrainedRidgeRegression]()
    catalog.registerDOperable[Report]()
  }

  def registerDOperations(catalog: DOperationsCatalog) = {

    catalog.registerDOperation[FileToDataFrame](
      DOperationCategories.IO,
      "Converts a file to a DataFrame"
    )

    catalog.registerDOperation[ReadFile](
      DOperationCategories.IO,
      "Reads file from HDFS")

    catalog.registerDOperation[ReadDataFrame](
      DOperationCategories.IO,
      "Reads DataFrame from HDFS"
    )

    catalog.registerDOperation[WriteDataFrame](
      DOperationCategories.IO,
      "Writes DataFrame to HDFS"
    )

    catalog.registerDOperation[DataFrameSplitter](
      DOperationCategories.Utils,
      "Splits DataFrame into two DataFrames"
    )

    catalog.registerDOperation[Join](
      DOperationCategories.Utils,
      "Joins two DataFrames to a DataFrame"
    )

    catalog.registerDOperation[OneHotEncoder](
      DOperationCategories.Utils,
      "One-hot encodes categorical columns"
    )

    catalog.registerDOperation[ProjectColumns](
      DOperationCategories.Utils,
      "Projects columns...FIXME"
    )

    catalog.registerDOperation[TimestampDecomposer](
      DOperationCategories.Utils,
      "Decomposes selected columns from timestamp to numeric"
    )

    catalog.registerDOperation[CreateRidgeRegression](
      DOperationCategories.ML.Regression,
      "Creates untrained ridge regression model"
    )

    catalog.registerDOperation[EvaluateRegression](
      DOperationCategories.ML.Regression,
      "Evaluates regression...FIXME"
    )

    catalog.registerDOperation[TrainRegressor](
      DOperationCategories.ML.Regression,
      "Trains linear regression model"
    )

    catalog.registerDOperation[ScoreRegressor](
      DOperationCategories.ML.Regression,
      "Scores trained linear regression model"
    )

    catalog.registerDOperation[CrossValidateRegressor](
      DOperationCategories.ML.Regression,
      "Cross-validates regression model"
    )

    catalog.registerDOperation[EvaluateRegression](
      DOperationCategories.ML,
      "Evaluate Regression"
    )

    catalog.registerDOperation[ApplyTransformation](
      DOperationCategories.Utils,
      "Apply Transformation"
    )
  }
}
