/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import org.mockito.Mockito.{verify, when}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.Transformation
import io.deepsense.deeplang.doperables.dataframe.DataFrame

class ApplyTransformationSpec extends WordSpec with Matchers with MockitoSugar {

  "ApplyTransformation" should {
    "execute transform on transformation with dataframe as an argument" in {
      val applyTransformation = new ApplyTransformation()
      val context = mock[ExecutionContext]
      val transformation = mock[Transformation]
      val inputDataFrame = mock[DataFrame]
      val resultDataFrame = mock[DataFrame]
      when(transformation.transform(inputDataFrame)).thenReturn(resultDataFrame)

      val result = applyTransformation.execute(context)(Vector(transformation, inputDataFrame))

      verify(transformation).transform(inputDataFrame)
      result shouldBe Vector(resultDataFrame)
    }
  }
}
