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

import io.deepsense.deeplang.doperables.UntrainedLogisticRegression
import io.deepsense.deeplang.{ExecutionContext, UnitSpec}

class CreateLogisticRegressionSpec extends UnitSpec {
  "CreateLogisticRegression DOperation" should {
    "create LogisticRegressionModel" in {
      val createLogisticRegression = CreateLogisticRegression(1, 0.0001)
      val context = mock[ExecutionContext]
      val resultVector = createLogisticRegression.execute(context)(Vector.empty)
      val result = resultVector.head.asInstanceOf[UntrainedLogisticRegression]
      result.model shouldBe defined
    }
  }
}
