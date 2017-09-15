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
      "Reads a file from HDFS")

    catalog.registerDOperation[LoadDataFrame](
      DOperationCategories.IO,
      "Loads a DataFrame from HDFS"
    )

    catalog.registerDOperation[SaveDataFrame](
      DOperationCategories.IO,
      "Saves a DataFrame to HDFS"
    )

    catalog.registerDOperation[Split](
      DOperationCategories.DataManipulation,
      "Splits a DataFrame into two DataFrames"
    )

    catalog.registerDOperation[Join](
      DOperationCategories.DataManipulation,
      "Joins two DataFrames to a DataFrame"
    )

    catalog.registerDOperation[OneHotEncoder](
      DOperationCategories.DataManipulation,
      "One-hot encodes categorical columns of a DataFrame"
    )

    catalog.registerDOperation[ProjectColumns](
      DOperationCategories.DataManipulation,
      "Projects selected columns of a DataFrame"
    )

    catalog.registerDOperation[DecomposeDatetime](
      DOperationCategories.DataManipulation,
      "Extracts Numeric fields (year, month, etc.) from a Timestamp"
    )

    catalog.registerDOperation[CreateRidgeRegression](
      DOperationCategories.ML.Regression,
      "Creates an untrained ridge regression model"
    )

    catalog.registerDOperation[TrainRegressor](
      DOperationCategories.ML.Regression,
      "Trains a regression model"
    )

    catalog.registerDOperation[ScoreRegressor](
      DOperationCategories.ML.Regression,
      "Scores a trained regression model"
    )

    catalog.registerDOperation[CrossValidateRegressor](
      DOperationCategories.ML.Regression,
      "Cross-validates a regression model"
    )

    catalog.registerDOperation[EvaluateRegression](
      DOperationCategories.ML.Regression,
      "Evaluates a regression model"
    )

    catalog.registerDOperation[CreateLogisticRegression](
      DOperationCategories.ML.Classification,
      "Creates an untrained logistic regression model"
    )

    catalog.registerDOperation[TrainClassifier](
      DOperationCategories.ML.Classification,
      "Trains a classification model"
    )

    catalog.registerDOperation[ScoreClassifier](
      DOperationCategories.ML.Classification,
      "Scores a trained classification model"
    )

    catalog.registerDOperation[EvaluateClassification](
      DOperationCategories.ML.Classification,
      "Evaluates a classification model"
    )

    catalog.registerDOperation[ApplyTransformation](
      DOperationCategories.Transformation,
      "Applies a Transformation to a DataFrame"
    )

    catalog.registerDOperation[SelectImportantFeatures](
      DOperationCategories.ML.FeatureSelection,
      "Selects most important features of a DataFrame"
    )

    catalog.registerDOperation[ConvertType](
      DOperationCategories.DataManipulation,
      "Converts selected columns of a DataFrame to a different type"
    )
  }
}
