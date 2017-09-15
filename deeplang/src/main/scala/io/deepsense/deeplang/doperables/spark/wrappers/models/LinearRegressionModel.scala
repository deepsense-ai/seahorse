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
import io.deepsense.deeplang.doperables.report.CommonTablesGenerators.SummaryEntry
import io.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import io.deepsense.deeplang.doperables.spark.wrappers.params.LinearRegressionParams
import io.deepsense.deeplang.params.Param


class LinearRegressionModel
  extends SparkModelWrapper[
    SparkLinearRegressionModel,
    SparkLinearRegression]
  with LinearRegressionParams {

  override val params: Array[Param[_]] = declareParams(
    elasticNetParam,
    fitIntercept,
    maxIterations,
    regularizationParam,
    tolerance,
    standardization,
    featuresColumn,
    labelColumn,
    predictionColumn)

  override def report: Report = {
    val coefficients =
      SummaryEntry(
        name = "coefficients",
        value = model.coefficients.toString,
        description = "Model coefficients.")

    val summary = if (model.hasSummary) {
      List(
        SummaryEntry(
          name = "explained variance",
          value = model.summary.explainedVariance.toString,
          description = "Explained variance regression score."),
        SummaryEntry(
          name = "mean absolute error",
          value = model.summary.meanAbsoluteError.toString,
          description = "Mean absolute error is a risk function corresponding to the " +
            "expected value of the absolute error loss or l1-norm loss."),
        SummaryEntry(
          name = "mean squared error",
          value = model.summary.meanSquaredError.toString,
          description = "Mean squared error is a risk function corresponding to the " +
            "expected value of the squared error loss or quadratic loss."),
        SummaryEntry(
          name = "root mean squared error",
          value = model.summary.rootMeanSquaredError.toString,
          description = "Root mean squared error is defined as the square root " +
            "of the mean squared error."),
        SummaryEntry(
          name = "R^2^",
          value = model.summary.r2.toString,
          description = "R^2^ is the coefficient of determination."),
        SummaryEntry(
          name = "objective history",
          value = model.summary.objectiveHistory.mkString("[", ", ", "]"),
          description = "objective function (scaled loss + regularization) at each iteration."),
        SummaryEntry(
          name = "total iterations",
          value = model.summary.totalIterations.toString,
          description = "Number of training iterations until termination."),
        SummaryEntry(
          name = "number of instances",
          value = model.summary.numInstances.toString,
          description = "Number of instances in DataFrame predictions."),
        SummaryEntry(
          name = "deviance residuals",
          value = model.summary.devianceResiduals.mkString("[", ", ", "]"),
          description = "The weighted residuals, the usual residuals " +
            "rescaled by the square root of the instance weights."),
        SummaryEntry(
          name = "coefficient standard errors",
          value = model.summary.coefficientStandardErrors.mkString("[", ", ", "]"),
          description = "Standard error of estimated coefficients and intercept."),
        SummaryEntry(
          name = "t-values",
          value = model.summary.tValues.mkString("[", ", ", "]"),
          description = "T-statistic of estimated coefficients and intercept."),
        SummaryEntry(
          name = "p-values",
          value = model.summary.pValues.mkString("[", ", ", "]"),
          description = "Two-sided p-value of estimated coefficients and intercept.")
      )
    } else {
      Nil
    }

    super.report
      .withAdditionalTable(CommonTablesGenerators.modelSummary(List(coefficients) ++ summary))
  }
}
