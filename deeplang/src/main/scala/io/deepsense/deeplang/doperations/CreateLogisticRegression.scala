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

import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS

import io.deepsense.deeplang.doperables.UntrainedLogisticRegression
import io.deepsense.deeplang.parameters.{NumericParameter, ParametersSchema, RangeValidator}
import io.deepsense.deeplang.{DOperation, DOperation0To1, ExecutionContext}

case class CreateLogisticRegression() extends DOperation0To1[UntrainedLogisticRegression] {
  @transient
  override lazy val tTagTO_0: ru.TypeTag[UntrainedLogisticRegression] =
    ru.typeTag[UntrainedLogisticRegression]

  import CreateLogisticRegression._
  override val name = "Create Logistic Regression"

  override val id: DOperation.Id = "ed20e602-ff91-11e4-a322-1697f925ec7b"

  override val parameters = ParametersSchema(
    IterationsNumberKey -> NumericParameter(
      description = "Max number of iterations to perform",
      default = Some(1.0),
      required = true,
      validator = RangeValidator(begin = 1.0, end = EndOfRange, step = Some(1.0))),
    Tolerance -> NumericParameter(
      description = "The convergence tolerance of iterations for LBFGS. " +
        "Smaller value will lead to higher accuracy at a cost of more iterations.",
      default = Some(0.0001),
      required = true,
      validator = RangeValidator(begin = 0.0, end = EndOfRange, beginIncluded = false)))

  override protected def _execute(context: ExecutionContext)(): UntrainedLogisticRegression = {
    val iterationsParam = parameters.getDouble(IterationsNumberKey).get
    val toleranceParam = parameters.getDouble(Tolerance).get

    def createModelInstance(): LogisticRegressionWithLBFGS = {
      val model = new LogisticRegressionWithLBFGS
      model
        .setIntercept(true)
        .setNumClasses(2)
        .setValidateData(false)
        .optimizer
        .setRegParam(0.0)
        .setNumIterations(iterationsParam.toInt)
        .setConvergenceTol(toleranceParam)

      model
    }

    // We're passing a factory method here, instead of constructed object,
    // because the resulting UntrainedLogisticRegression could be used multiple times
    // in a workflow and its underlying Spark model is mutable
    UntrainedLogisticRegression(createModelInstance)
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
