/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import org.apache.spark.ml.regression.{AFTSurvivalRegression => SparkAFTSurvivalRegression, AFTSurvivalRegressionModel => SparkAFTSurvivalRegressionModel}

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.SparkModelWrapper
import ai.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import ai.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import ai.deepsense.deeplang.doperables.serialization.SerializableSparkModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.AFTSurvivalRegressionParams
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.PredictorParams
import ai.deepsense.deeplang.params.Param


class AFTSurvivalRegressionModel
  extends SparkModelWrapper[
    SparkAFTSurvivalRegressionModel,
    SparkAFTSurvivalRegression]
  with PredictorParams
  with AFTSurvivalRegressionParams {

  override val params: Array[Param[_]] = Array(
    featuresColumn,
    predictionColumn,
    quantileProbabilities,
    optionalQuantilesColumn)

  override def report(extended: Boolean = true): Report = {
    val summary =
      List(
        SparkSummaryEntry(
          name = "coefficients",
          value = sparkModel.coefficients,
          description = "Regression coefficients vector of the beta parameter."),
        SparkSummaryEntry(
          name = "intercept",
          value = sparkModel.intercept,
          description = "Intercept of the beta parameter."),
        SparkSummaryEntry(
          name = "scale",
          value = sparkModel.scale,
          description = "The log of scale parameter - log(sigma).")
      )

    super.report(extended)
      .withAdditionalTable(CommonTablesGenerators.modelSummary(summary))
  }

  override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SparkAFTSurvivalRegressionModel] = {
    new SerializableSparkModel(SparkAFTSurvivalRegressionModel.load(path))
  }
}
