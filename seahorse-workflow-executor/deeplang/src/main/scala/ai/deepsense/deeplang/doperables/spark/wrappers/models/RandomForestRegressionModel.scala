/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.deeplang.doperables.spark.wrappers.models

import org.apache.spark.ml.regression.{RandomForestRegressionModel => SparkRFRModel, RandomForestRegressor => SparkRFR}

import ai.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import ai.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.PredictorParams
import ai.deepsense.deeplang.doperables.{LoadableWithFallback, SparkModelWrapper}
import ai.deepsense.deeplang.params.Param
import ai.deepsense.sparkutils.ML

class RandomForestRegressionModel
  extends SparkModelWrapper[SparkRFRModel, SparkRFR]
  with LoadableWithFallback[SparkRFRModel, SparkRFR]
  with PredictorParams {

  override val params: Array[Param[_]] = Array(
    featuresColumn,
    predictionColumn)

  override def report(extended: Boolean = true): Report = {
    val summary =
      List(
        SparkSummaryEntry(
          name = "number of features",
          value = sparkModel.numFeatures,
          description = "Number of features the model was trained on."),
        SparkSummaryEntry(
          name = "feature importances",
          value = sparkModel.featureImportances,
          description = "Estimate of the importance of each feature."
        ))

    val numTrees = ML.ModelParams.numTreesFromRandomForestRegressionModel(sparkModel)
    super.report(extended)
      .withReportName(
        s"${this.getClass.getSimpleName} with $numTrees trees")
      .withAdditionalTable(CommonTablesGenerators.modelSummary(summary))
      .withAdditionalTable(
        CommonTablesGenerators.decisionTree(
          sparkModel.treeWeights,
          sparkModel.trees),
        2)
  }

  override def tryToLoadModel(path: String): Option[SparkRFRModel] = ML.ModelLoading.randomForestRegression(path)
}
