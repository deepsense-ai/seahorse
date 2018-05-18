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

import org.apache.spark.ml.classification.{LogisticRegressionTrainingSummary, LogisticRegression => SparkLogisticRegression, LogisticRegressionModel => SparkLogisticRegressionModel}

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.SparkModelWrapper
import ai.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import ai.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import ai.deepsense.deeplang.doperables.serialization.SerializableSparkModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasThreshold, ProbabilisticClassifierParams}
import ai.deepsense.deeplang.params.Param

class LogisticRegressionModel
  extends SparkModelWrapper[
    SparkLogisticRegressionModel,
    SparkLogisticRegression]
  with ProbabilisticClassifierParams
  with HasThreshold {

  override val params: Array[Param[_]] = Array(
    featuresColumn,
    probabilityColumn,
    rawPredictionColumn,
    predictionColumn,
    threshold)

  override def report(extended: Boolean = true): Report = {
    val coefficients =
      SparkSummaryEntry(
        name = "coefficients",
        value = sparkModel.coefficients,
        description = "Weights computed for every feature.")

    val summary = if (sparkModel.hasSummary) {
      val modelSummary: LogisticRegressionTrainingSummary = sparkModel.summary
      List(
        SparkSummaryEntry(
          name = "objective history",
          value = modelSummary.objectiveHistory,
          description = "Objective function (scaled loss + regularization) at each iteration."),
        SparkSummaryEntry(
          name = "total iterations",
          value = modelSummary.totalIterations,
          description = "Number of training iterations until termination."))
    } else {
      Nil
    }

    super.report(extended)
      .withAdditionalTable(CommonTablesGenerators.modelSummary(List(coefficients) ++ summary))
  }

  override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SparkLogisticRegressionModel] = {
    new SerializableSparkModel(SparkLogisticRegressionModel.load(path))
  }
}
