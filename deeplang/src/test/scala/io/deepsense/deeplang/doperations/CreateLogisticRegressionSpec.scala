/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.doperables.UntrainedLogisticRegression
import io.deepsense.deeplang.{ExecutionContext, UnitSpec}

class CreateLogisticRegressionSpec extends UnitSpec {
  "CreateLogisticRegression DOperation" should {
    "create LogisticRegressionModel" in {
      val createLogisticRegression = CreateLogisticRegression()
      val context = mock[ExecutionContext]
      val resultVector = createLogisticRegression.execute(context)(Vector.empty)
      val result = resultVector.head.asInstanceOf[UntrainedLogisticRegression]
      result.model shouldBe defined
    }
  }
}
