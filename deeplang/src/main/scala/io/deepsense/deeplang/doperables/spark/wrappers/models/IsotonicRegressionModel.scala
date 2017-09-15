/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang.doperables.spark.wrappers.models

import org.apache.spark.ml.regression.{IsotonicRegression => SparkIsotonicRegression, IsotonicRegressionModel => SparkIsotonicRegressionModel}

import io.deepsense.deeplang.doperables.CommonTablesGenerators.SummaryEntry
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasFeatureIndexParam, PredictorParams}
import io.deepsense.deeplang.doperables.{CommonTablesGenerators, Report, SparkModelWrapper}

class IsotonicRegressionModel
  extends SparkModelWrapper[SparkIsotonicRegressionModel, SparkIsotonicRegression]
  with PredictorParams
  with HasFeatureIndexParam {

  override val params = declareParams(
    featureIndex,
    featuresColumn,
    predictionColumn)

  override def report: Report = {
    val summary =
      List(
        SummaryEntry(
          name = "boundaries",
          value = model.boundaries.toString,
          description = "Boundaries in increasing order for which predictions are known."),
        SummaryEntry(
          name = "predictions",
          value = model.predictions.toString,
          description = "Predictions associated with the boundaries at the same index, " +
            "monotone because of isotonic regression."))
    super.report
      .withAdditionalTable(CommonTablesGenerators.modelSummary(summary))
  }
}
