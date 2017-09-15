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

import io.deepsense.deeplang.doperables.{RandomForestParameters, UntrainedRandomForestRegression}
import io.deepsense.deeplang.{ExecutionContext, UnitSpec}

class CreateRandomForestRegressionSpec extends UnitSpec {
  "CreateRandomForestRegression DOperation" should {
    "create UntrainedRandomForestModel" in {
      val createRandomForestRegression = CreateRandomForestRegression(1, "auto", "variance", 4, 100)
      val context = mock[ExecutionContext]
      val resultVector = createRandomForestRegression.execute(context)(Vector.empty)
      val result = resultVector.head.asInstanceOf[UntrainedRandomForestRegression]
      result.modelParameters shouldBe a [RandomForestParameters]

      result.modelParameters.numTrees shouldBe 1
      result.modelParameters.featureSubsetStrategy shouldBe "auto"
      result.modelParameters.impurity shouldBe "variance"
      result.modelParameters.maxDepth shouldBe 4
      result.modelParameters.maxBins shouldBe 100
    }
  }
}
