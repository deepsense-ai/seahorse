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

import org.apache.spark.ml.regression.{IsotonicRegression => SparkIsotonicRegression, IsotonicRegressionModel => SparkIsotonicRegressionModel}

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.SparkModelWrapper
import ai.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import ai.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import ai.deepsense.deeplang.doperables.serialization.SerializableSparkModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasFeatureIndexParam, PredictorParams}

class IsotonicRegressionModel
  extends SparkModelWrapper[SparkIsotonicRegressionModel, SparkIsotonicRegression]
  with PredictorParams
  with HasFeatureIndexParam {

  override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array(
    featureIndex,
    featuresColumn,
    predictionColumn)

  override def report(extended: Boolean = true): Report = {
    val summary =
      List(
        SparkSummaryEntry(
          name = "boundaries",
          value = sparkModel.boundaries,
          description = "Boundaries in increasing order for which predictions are known."),
        SparkSummaryEntry(
          name = "predictions",
          value = sparkModel.predictions,
          description = "Predictions associated with the boundaries at the same index, " +
            "monotone because of isotonic regression."))
    super.report(extended)
      .withAdditionalTable(CommonTablesGenerators.modelSummary(summary))
  }

  override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SparkIsotonicRegressionModel] = {
    new SerializableSparkModel(SparkIsotonicRegressionModel.load(path))
  }
}
