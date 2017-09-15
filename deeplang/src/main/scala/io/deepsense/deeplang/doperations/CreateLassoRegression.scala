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

import org.apache.spark.mllib.regression.{LassoModel, LassoWithSGD}

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.machinelearning.LinearRegressionParameters
import io.deepsense.deeplang.doperables.machinelearning.lassoregression.UntrainedLassoRegression

case class CreateLassoRegression()
  extends AbstractCreateLinearRegression[UntrainedLassoRegression, LassoWithSGD, LassoModel] {

  @transient
  override lazy val tTagTO_0: ru.TypeTag[UntrainedLassoRegression] =
    ru.typeTag[UntrainedLassoRegression]

  override val name = "Create Lasso Regression"

  override val id: Id = "1e4523ea-ccf6-4a66-8e4c-ebbb6c4b7272"

  override protected def _execute(context: ExecutionContext)(): UntrainedLassoRegression = {
    // We're passing a factory method here, instead of constructed object,
    // because the resulting UntrainedLassoRegression could be used multiple times
    // in a workflow and its underlying Spark model is mutable
    UntrainedLassoRegression(
      () => createModel(() => new LassoWithSGD()),
      LinearRegressionParameters(this))
  }
}

object CreateLassoRegression {
  def apply(regularization: Double, iterationsNumber: Int,
            miniBatchFraction: Double = 1.0): CreateLassoRegression = {
    val createLassoRegression = CreateLassoRegression()
    createLassoRegression.regularizationParameter.value = Some(regularization)
    createLassoRegression.iterationsNumberParameter.value = Some(iterationsNumber.toDouble)
    createLassoRegression.miniBatchFractionParameter.value = Some(miniBatchFraction)
    createLassoRegression
  }
}
