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

import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang.doperables.machinelearning.svm.SupportVectorMachineParameters
import io.deepsense.deeplang.doperables.machinelearning.svm.classification.UntrainedSupportVectorMachineClassifier
import io.deepsense.deeplang.parameters.RegularizationType
import io.deepsense.deeplang.{ExecutionContext, UnitSpec}

class CreateSupportVectorMachineClassificationSpec
  extends UnitSpec with MockitoSugar {

  "CreateSupportVectorMachineClassification" should {
    "create UntrainedSupportVectorMachineClassifier" in {
      val createRidgeRegression =
        CreateSupportVectorMachineClassification(RegularizationType.L1, 10, 0.0, 0.6)

      val result =
        createRidgeRegression.execute(mock[ExecutionContext])(Vector.empty)
          .head.asInstanceOf[UntrainedSupportVectorMachineClassifier]

      result.svmParameters shouldBe
        SupportVectorMachineParameters(RegularizationType.L1, 10, 0.0, 0.6)
    }
  }
}
