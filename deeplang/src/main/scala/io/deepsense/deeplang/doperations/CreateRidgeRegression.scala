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

package io.deepsense.deeplang.doperations

import scala.reflect.runtime.{universe => ru}

import org.apache.spark.mllib.regression.{RidgeRegressionModel, RidgeRegressionWithSGD}

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.machinelearning.LinearRegressionParameters
import io.deepsense.deeplang.doperables.machinelearning.ridgeregression.UntrainedRidgeRegression

case class CreateRidgeRegression()
    extends AbstractCreateLinearRegression
      [UntrainedRidgeRegression, RidgeRegressionWithSGD, RidgeRegressionModel] {

  @transient
  override lazy val tTagTO_0: ru.TypeTag[UntrainedRidgeRegression] =
    ru.typeTag[UntrainedRidgeRegression]

  override val name = "Create Ridge Regression"

  override val id: Id = "0643f308-f2fa-11e4-b9b2-1697f925ec7b"

  override protected def _execute(context: ExecutionContext)(): UntrainedRidgeRegression = {
    // We're passing a factory method here, instead of constructed object,
    // because the resulting UntrainedRidgeRegressiuon could be used multiple times
    // in a workflow and its underlying Spark model is mutable
    UntrainedRidgeRegression(
      () => createModel(() => new RidgeRegressionWithSGD()),
      LinearRegressionParameters(this))
  }
}

object CreateRidgeRegression {
  def apply(regularization: Double, iterationsNumber: Int,
      miniBatchFraction: Double = 1.0): CreateRidgeRegression = {
    val createRidgeRegression = CreateRidgeRegression()
    createRidgeRegression.regularizationParameter.value = regularization
    createRidgeRegression.iterationsNumberParameter.value = iterationsNumber.toDouble
    createRidgeRegression.miniBatchFractionParameter.value = miniBatchFraction
    createRidgeRegression
  }
}
