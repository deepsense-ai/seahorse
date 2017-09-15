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

package io.deepsense.deeplang.doperables.spark.wrappers.estimators

import org.apache.spark.ml.classification.{LogisticRegression => SparkLogisticRegression, LogisticRegressionModel => SparkLogisticRegressionModel}

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.spark.wrappers.models.LogisticRegressionModel
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasLabelColumnParam, HasThreshold, ProbabilisticClassifierParams}
import io.deepsense.deeplang.doperables.{Report, SparkEstimatorWrapper}
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.validators.RangeValidator
import io.deepsense.deeplang.params.wrappers.spark._

class LogisticRegression
  extends SparkEstimatorWrapper[
    SparkLogisticRegressionModel,
    SparkLogisticRegression,
    LogisticRegressionModel]
  with ProbabilisticClassifierParams
  with HasLabelColumnParam
  with HasThreshold {

  val elasticNetParameter = new DoubleParamWrapper[SparkLogisticRegression](
    name = "elastic net parameter",
    description = "ElasticNet mixing parameter, in range [0, 1]. " +
      "For alpha = 0, the penalty is an L2 penalty. For alpha = 1, it is an L1 penalty.",
    sparkParamGetter = _.elasticNetParam,
    validator = RangeValidator(0.0, 1.0))
  setDefault(elasticNetParameter, 0.0)

  val fitIntercept = new BooleanParamWrapper[SparkLogisticRegression](
    name = "fit intercept",
    description = "Whether to fit an intercept term",
    sparkParamGetter = _.fitIntercept)
  setDefault(fitIntercept, true)

  val maxIterations = new IntParamWrapper[SparkLogisticRegression](
    name = "max iterations",
    description = "Maximum number of iterations (>= 0)",
    sparkParamGetter = _.maxIter,
    validator = RangeValidator.positiveIntegers)
  setDefault(maxIterations, 100.0)

  val regularizationParameter = new DoubleParamWrapper[SparkLogisticRegression](
    name = "regularization parameter",
    description = "Regularization parameter (>= 0)",
    sparkParamGetter = _.regParam,
    validator = RangeValidator.positiveIntegers)
  setDefault(regularizationParameter, 0.0)

  val tolerance = new DoubleParamWrapper[SparkLogisticRegression](
    name = "tolerance",
    description = "Convergence tolerance",
    sparkParamGetter = _.tol)
  setDefault(tolerance, 1E-6)

  val standardization = new BooleanParamWrapper[SparkLogisticRegression](
    name = "standardization",
    description = "Whether to standardize the training features before fitting the model",
    sparkParamGetter = _.standardization)

  override def report(executionContext: ExecutionContext): Report = Report()

  override val params: Array[Param[_]] = declareParams(
    elasticNetParameter,
    fitIntercept,
    maxIterations,
    regularizationParameter,
    tolerance,
    standardization,
    featuresColumn,
    labelColumn,
    probabilityColumn,
    rawPredictionColumn,
    predictionColumn,
    threshold)
}
