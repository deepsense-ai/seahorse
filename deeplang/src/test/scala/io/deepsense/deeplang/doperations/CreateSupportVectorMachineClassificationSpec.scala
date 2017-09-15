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
