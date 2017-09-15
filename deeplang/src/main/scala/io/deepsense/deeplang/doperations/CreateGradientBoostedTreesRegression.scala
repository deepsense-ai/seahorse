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

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.machinelearning.gradientboostedtrees.regression.UntrainedGradientBoostedTreesRegression
import io.deepsense.deeplang.{ExecutionContext, DOperation0To1}

case class CreateGradientBoostedTreesRegression()
    extends {
      override val impurityOptions = Seq("variance")
      override val lossOptions = Seq("squared", "absolute")
    }
    with DOperation0To1[UntrainedGradientBoostedTreesRegression]
    with GradientBoostedTreesParams {

  override val name = "Create Gradient Boosted Trees Regression"

  override val id: Id = "736b57f2-1cfa-43b0-9467-dc0f30b724ea"

  override protected def _execute(context: ExecutionContext)()
    : UntrainedGradientBoostedTreesRegression = {
    UntrainedGradientBoostedTreesRegression(modelParameters)
  }

  @transient
  override lazy val tTagTO_0: ru.TypeTag[UntrainedGradientBoostedTreesRegression] =
    ru.typeTag[UntrainedGradientBoostedTreesRegression]
}

object CreateGradientBoostedTreesRegression {

  def apply(numIterations: Int,
            loss: String,
            impurity: String,
            maxDepth: Int,
            maxBins: Int): CreateGradientBoostedTreesRegression = {
    val createGradientBoostedTreesRegression = CreateGradientBoostedTreesRegression()
    createGradientBoostedTreesRegression.setParameters(
      numIterations, loss, impurity, maxDepth, maxBins)
    createGradientBoostedTreesRegression
  }
}
