/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS

import io.deepsense.deeplang.doperables.UntrainedLogisticRegression
import io.deepsense.deeplang.parameters.{NumericParameter, ParametersSchema, RangeValidator}
import io.deepsense.deeplang.{DOperation, DOperation0To1, ExecutionContext}

case class CreateLogisticRegression() extends DOperation0To1[UntrainedLogisticRegression] {
  import CreateLogisticRegression._
  override val name = "Logistic Regression"

  override val id: DOperation.Id = "ed20e602-ff91-11e4-a322-1697f925ec7b"

  override val parameters = ParametersSchema(
    IterationsNumberKey -> NumericParameter(
      description = "Max number of iterations to perform",
      default = Some(1.0),
      required = true,
      validator = RangeValidator(begin = 1.0, end = EndOfRange, step = Some(1.0))),
    Tolerance -> NumericParameter(
      description = "The convergence tolerance of iterations for LBFGS. " +
        "Smaller value will lead to higher accuracy with the cost of more iterations.",
      default = Some(0.0001),
      required = true,
      validator = RangeValidator(begin = 0.0, end = EndOfRange, beginIncluded = false)))

  override protected def _execute(context: ExecutionContext)(): UntrainedLogisticRegression = {
    val iterationsParam = parameters.getDouble(IterationsNumberKey).get
    val toleranceParam = parameters.getDouble(Tolerance).get
    val model = new LogisticRegressionWithLBFGS
    model
      .setIntercept(true)
      .setNumClasses(2)
      .setValidateData(false)
      .optimizer
      .setRegParam(0.0)
      .setNumIterations(iterationsParam.toInt)
      .setConvergenceTol(toleranceParam)
    UntrainedLogisticRegression(Some(model))
  }
}

object CreateLogisticRegression {
  val IterationsNumberKey = "iterations number"
  val Tolerance = "tolerance"
  val EndOfRange = 1000000.0

  def apply(numberOfIterations: Int, tolerance: Double): CreateLogisticRegression = {
    val createLogisticRegression: CreateLogisticRegression = CreateLogisticRegression()
    createLogisticRegression.parameters.getNumericParameter(
      CreateLogisticRegression.IterationsNumberKey).value = Some(numberOfIterations)
    createLogisticRegression.parameters.getNumericParameter(
      CreateLogisticRegression.Tolerance).value = Some(tolerance)
    createLogisticRegression
  }
}
