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

import org.apache.spark.ml.regression.{LinearRegression => SparkLinearRegression, LinearRegressionModel => SparkLinearRegressionModel}

import io.deepsense.deeplang.doperables.SparkModelWrapper
import io.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import io.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.PredictorParams
import io.deepsense.deeplang.params.Param


class LinearRegressionModel
  extends SparkModelWrapper[
    SparkLinearRegressionModel,
    SparkLinearRegression]
  with PredictorParams {

  override val params: Array[Param[_]] = declareParams(
    featuresColumn,
    predictionColumn)

  override def report: Report = {
    val coefficients =
      SparkSummaryEntry(
        name = "coefficients",
        value = model.coefficients,
        description = "Weights computed for every feature.")

    val summary = if (model.hasSummary) {
      List(
        SparkSummaryEntry(
          name = "explained variance",
          value = model.summary.explainedVariance,
          description = "Explained variance regression score."),
        SparkSummaryEntry(
          name = "mean absolute error",
          value = model.summary.meanAbsoluteError,
          description = "Mean absolute error is a risk function corresponding to the " +
            "expected value of the absolute error loss or l1-norm loss."),
        SparkSummaryEntry(
          name = "mean squared error",
          value = model.summary.meanSquaredError,
          description = "Mean squared error is a risk function corresponding to the " +
            "expected value of the squared error loss or quadratic loss."),
        SparkSummaryEntry(
          name = "root mean squared error",
          value = model.summary.rootMeanSquaredError,
          description = "Root mean squared error is defined as the square root " +
            "of the mean squared error."),
        SparkSummaryEntry(
          name = "R^2^",
          value = model.summary.r2,
          description = "R^2^ is the coefficient of determination."),
        SparkSummaryEntry(
          name = "objective history",
          value = model.summary.objectiveHistory,
          description = "Objective function (scaled loss + regularization) at each iteration."),
        SparkSummaryEntry(
          name = "total iterations",
          value = model.summary.totalIterations,
          description = "Number of training iterations until termination."),
        SparkSummaryEntry(
          name = "number of instances",
          value = model.summary.numInstances,
          description = "Number of instances in DataFrame predictions."),
        SparkSummaryEntry(
          name = "deviance residuals",
          value = model.summary.devianceResiduals,
          description = "The weighted residuals, the usual residuals " +
            "rescaled by the square root of the instance weights."),
        SparkSummaryEntry(
          name = "coefficient standard errors",
          value = model.summary.coefficientStandardErrors,
          description = "Standard error of estimated coefficients and intercept."),
        SparkSummaryEntry(
          name = "t-values",
          value = model.summary.tValues,
          description = "T-statistic of estimated coefficients and intercept."),
        SparkSummaryEntry(
          name = "p-values",
          value = model.summary.pValues,
          description = "Two-sided p-value of estimated coefficients and intercept.")
      )
    } else {
      Nil
    }

    super.report
      .withAdditionalTable(CommonTablesGenerators.modelSummary(List(coefficients) ++ summary))
  }
}
