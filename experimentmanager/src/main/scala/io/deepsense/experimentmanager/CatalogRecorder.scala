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
import io.deepsense.deeplang.doperations.transformations.MathematicalTransformation

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
    catalog.registerDOperable[UntrainedLogisticRegression]()
    catalog.registerDOperable[MathematicalTransformation]()
    catalog.registerDOperable[TrainedLogisticRegression]()
    catalog.registerDOperable[Report]()
  }

  def registerDOperations(catalog: DOperationsCatalog) = {

    catalog.registerDOperation[FileToDataFrame](
      DOperationCategories.IO,
      "Converts a file to a DataFrame"
    )

    catalog.registerDOperation[MathematicalOperation](
      DOperationCategories.Transformation,
      "Creates a Transformation that creates a new column based on a mathematical formula."
    )

    catalog.registerDOperation[ReadFile](
      DOperationCategories.IO,
      "Reads file from HDFS")

    catalog.registerDOperation[LoadDataFrame](
      DOperationCategories.IO,
      "Loads DataFrame from HDFS"
    )

    catalog.registerDOperation[SaveDataFrame](
      DOperationCategories.IO,
      "Save DataFrame to HDFS"
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
      "Projects columns of dataframe"
    )

    catalog.registerDOperation[TimestampDecomposer](
      DOperationCategories.Utils,
      "Decomposes selected columns from timestamp to numeric"
    )

    catalog.registerDOperation[CreateRidgeRegression](
      DOperationCategories.ML.Regression,
      "Creates untrained ridge regression model"
    )

    catalog.registerDOperation[TrainRegressor](
      DOperationCategories.ML.Regression,
      "Trains regression model"
    )

    catalog.registerDOperation[ScoreRegressor](
      DOperationCategories.ML.Regression,
      "Scores trained regression model"
    )

    catalog.registerDOperation[CrossValidateRegressor](
      DOperationCategories.ML.Regression,
      "Cross-validates regression model"
    )

    catalog.registerDOperation[EvaluateRegression](
      DOperationCategories.ML.Regression,
      "Evaluate regression model"
    )

    catalog.registerDOperation[CreateLogisticRegression](
      DOperationCategories.ML.Regression,
      "Creates untrained logistic regression model"
    )

    catalog.registerDOperation[TrainClassifier](
      DOperationCategories.ML.Classification,
      "Trained classification model"
    )

    catalog.registerDOperation[ScoreClassifier](
      DOperationCategories.ML.Classification,
      "Scores trained classification model"
    )

    catalog.registerDOperation[EvaluateClassification](
      DOperationCategories.ML.Classification,
      "Evaluate classification model"
    )

    catalog.registerDOperation[ApplyTransformation](
      DOperationCategories.Utils,
      "Apply Transformation"
    )

    catalog.registerDOperation[SelectImportantFeatures](
      DOperationCategories.ML.FeatureSelection,
      "Selects most important features of dataframe"
    )

    catalog.registerDOperation[ConvertType](
      DOperationCategories.DataManipulation,
      "Converts columns to a different type"
    )
  }
}
