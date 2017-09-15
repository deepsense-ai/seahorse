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

import org.apache.spark.mllib.regression.RidgeRegressionWithSGD

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.machinelearning.ridgeregression.UntrainedRidgeRegression
import io.deepsense.deeplang.parameters.{NumericParameter, ParametersSchema, RangeValidator}

case class CreateRidgeRegression() extends DOperation0To1[UntrainedRidgeRegression] {
  @transient
  override lazy val tTagTO_0: ru.TypeTag[UntrainedRidgeRegression] =
    ru.typeTag[UntrainedRidgeRegression]

  override val name = "Create Ridge Regression"

  override val id: Id = "0643f308-f2fa-11e4-b9b2-1697f925ec7b"

  val regularizationParameter = NumericParameter(
    description = "Regularization parameter",
    default = Some(0.0),
    required = true,
    validator = RangeValidator(begin = 0.0, end = Double.PositiveInfinity))

  val iterationsNumberParameter = NumericParameter(
    description = "Number of iterations to perform",
    default = Some(1.0),
    required = true,
    validator = RangeValidator(begin = 1.0, end = 1000000, step = Some(1.0)))

  val miniBatchFractionParameter = NumericParameter(
    description = "Mini batch fraction",
    default = Some(1.0),
    required = true,
    validator = RangeValidator(begin = 0.0, end = 1.0, beginIncluded = false))

  override val parameters = ParametersSchema(
    "regularization" -> regularizationParameter,
    "iterations number" -> iterationsNumberParameter,
    "mini batch fraction" -> miniBatchFractionParameter)

  override protected def _execute(context: ExecutionContext)(): UntrainedRidgeRegression = {
    val regParam = regularizationParameter.value.get
    val numberOfIterations = iterationsNumberParameter.value.get
    val miniBatchFraction = miniBatchFractionParameter.value.get

    def createModelInstance(): RidgeRegressionWithSGD = {
      val model = new RidgeRegressionWithSGD
      model
        .setIntercept(true)
        .setValidateData(false)
        .optimizer
        .setStepSize(1.0)
        .setRegParam(regParam)
        .setNumIterations(numberOfIterations.toInt)
        .setMiniBatchFraction(miniBatchFraction)

      model
    }

    // We're passing a factory method here, instead of constructed object,
    // because the resulting UntrainedRidgeRegression could be used multiple times
    // in a workflow and its underlying Spark model is mutable
    UntrainedRidgeRegression(createModelInstance)
  }
}

object CreateRidgeRegression {
  def apply(regularization: Double, iterationsNumber: Int,
      miniBatchFraction: Double = 1.0): CreateRidgeRegression = {
    val createRidgeRegression = CreateRidgeRegression()
    createRidgeRegression.regularizationParameter.value = Some(regularization)
    createRidgeRegression.iterationsNumberParameter.value = Some(iterationsNumber.toDouble)
    createRidgeRegression.miniBatchFractionParameter.value = Some(miniBatchFraction)
    createRidgeRegression
  }
}
