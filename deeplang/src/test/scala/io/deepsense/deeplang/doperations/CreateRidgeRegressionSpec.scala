/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.doperables.UntrainedRidgeRegression
import io.deepsense.deeplang.{ExecutionContext, UnitSpec}

class CreateRidgeRegressionSpec extends UnitSpec {
  "CreateRidgeRegression DOperation" should {
    "create RidgeRegressionModel" in {
      val createRidgeRegression = CreateRidgeRegression(16.5, 1)
      val context = mock[ExecutionContext]
      val resultVector = createRidgeRegression.execute(context)(Vector.empty)
      val result = resultVector.head.asInstanceOf[UntrainedRidgeRegression]
      result.model shouldBe defined
    }
  }
}
