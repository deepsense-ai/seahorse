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

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.doperables.machinelearning.gradientboostedtrees.regression.{UntrainedGradientBoostedTreesRegression, TrainedGradientBoostedTreesRegression}
import io.deepsense.deeplang.doperables.machinelearning.gradientboostedtrees.GradientBoostedTreesParameters

class UntrainedGradientBoostedTreesRegressionIntegSpec extends UntrainedTreeRegressionIntegSpec {

  override def constructUntrainedModel: Trainable =
    UntrainedGradientBoostedTreesRegression(
      mockUntrainedModel.asInstanceOf[GradientBoostedTreesParameters])

  private val mockUntrainedModel: GradientBoostedTreesParameters =
    GradientBoostedTreesParameters(1, "squared", "variance", 4, 100)

  override def validateResult(result: Scorable): Registration = {
    val castedResult = result.asInstanceOf[TrainedGradientBoostedTreesRegression]
    castedResult.featureColumns shouldBe Seq("column1", "column0", "column4")
    castedResult.targetColumn shouldBe "column3"
  }

  override def untrainedRegressionName: String = "UntrainedGradientBoostedTreesRegression"
}
