/**
 * Copyright 2015, CodiLime Inc.
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
import io.deepsense.deeplang.doperables.UntrainedRidgeRegression
import io.deepsense.deeplang.doperations.CreateRidgeRegression._
import io.deepsense.deeplang.parameters.{NumericParameter, ParametersSchema, RangeValidator}

case class CreateRidgeRegression() extends DOperation0To1[UntrainedRidgeRegression] {
  @transient
  override lazy val tTagTO_0: ru.TypeTag[UntrainedRidgeRegression] = ru.typeTag[UntrainedRidgeRegression]

  override val name = "Ridge Regression"

  override val id: Id = "0643f308-f2fa-11e4-b9b2-1697f925ec7b"

  override val parameters = ParametersSchema(
    RegularizationKey -> NumericParameter(
      description = "Regularization parameter",
      default = Some(0.0),
      required = true,
      validator = RangeValidator(begin = 0.0, end = Double.PositiveInfinity)),
    IterationsNumberKey -> NumericParameter(
      description = "Number of iterations to perform",
      default = Some(1.0),
      required = true,
      validator = RangeValidator(begin = 1.0, end = 1000000, step = Some(1.0))))

  override protected def _execute(context: ExecutionContext)(): UntrainedRidgeRegression = {
    val regParam = parameters.getDouble(RegularizationKey).get
    val numberOfIterations = parameters.getDouble(IterationsNumberKey).get
    val model = new RidgeRegressionWithSGD()
    model.setIntercept(true)
    model.setValidateData(false)
    model.optimizer
      .setStepSize(1.0)
      .setRegParam(regParam)
      .setNumIterations(numberOfIterations.toInt)
    UntrainedRidgeRegression(Some(model))
  }
}

object CreateRidgeRegression {
  val RegularizationKey = "regularization"
  val IterationsNumberKey = "iterations number"

  def apply(regularization: Double, iterationsNumber: Int): CreateRidgeRegression = {
    val createRidgeRegression = CreateRidgeRegression()
    createRidgeRegression.parameters.getNumericParameter(
      CreateRidgeRegression.RegularizationKey).value = Some(regularization)
    createRidgeRegression.parameters.getNumericParameter(
      CreateRidgeRegression.IterationsNumberKey).value = Some(iterationsNumber.toDouble)
    createRidgeRegression
  }
}
