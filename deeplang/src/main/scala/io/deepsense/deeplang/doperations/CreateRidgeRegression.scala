/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import org.apache.spark.mllib.regression.RidgeRegressionWithSGD

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.UntrainedRidgeRegression
import io.deepsense.deeplang.doperations.CreateRidgeRegression._
import io.deepsense.deeplang.parameters.{NumericParameter, ParametersSchema, RangeValidator}

case class CreateRidgeRegression() extends DOperation0To1[UntrainedRidgeRegression] {

  override val name = "Ridge Regression"

  override val id: Id = "0643f308-f2fa-11e4-b9b2-1697f925ec7b"

  override val parameters = ParametersSchema(
    RegularizationKey -> NumericParameter(
      description = "Regularization parameter",
      default = Some(0.0),
      required = true,
      validator = RangeValidator(begin = 0.0, end = Double.PositiveInfinity)))

  override protected def _execute(context: ExecutionContext)(): UntrainedRidgeRegression = {
    val regParam = parameters.getDouble(RegularizationKey).get
    val model = new RidgeRegressionWithSGD()
    model.setIntercept(true)
    model.optimizer
      .setRegParam(regParam)
      .setNumIterations(IterationsNumber)
    UntrainedRidgeRegression(Some(model))
  }
}

object CreateRidgeRegression {
  val RegularizationKey = "regularization"
  val IterationsNumber = 1000
}
