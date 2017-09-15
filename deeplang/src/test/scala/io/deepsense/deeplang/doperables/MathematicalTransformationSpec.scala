/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import org.mockito.Mockito._

import io.deepsense.deeplang.doperations.transformations.MathematicalTransformation
import io.deepsense.deeplang.{DKnowledge, UnitSpec}
import io.deepsense.deeplang.doperables.dataframe.{DataFrameBuilder, CommonColumnMetadata, DataFrameMetadata}
import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}
import io.deepsense.deeplang.parameters.ColumnType

class MathematicalTransformationSpec extends UnitSpec {

  "MathematicalTransformation" should {
    "infer result DataFrame" in {

      val transformation = new MathematicalTransformation()

      val metadata = DataFrameMetadata(
        isExact = true,
        isColumnCountExact = true,
        columns = Map(
          "num_col" -> CommonColumnMetadata("num_col", Some(0), Some(ColumnType.numeric)))
      )

      val expectedOutputMetadata = DataFrameMetadata(
        isExact = false,
        isColumnCountExact = false,
        columns = Map(
          "num_col" -> CommonColumnMetadata("num_col", Some(0), Some(ColumnType.numeric))
        )
      )

      val inferContext = mock[InferContext]
      when(inferContext.fullInference).thenReturn(true)

      val dKnowledge = DKnowledge(DataFrameBuilder.buildDataFrameForInference(metadata))
      val (outputKnowledge, warnings) = transformation.transform.infer(inferContext)(())(dKnowledge)

      val expectedOutputDKnowledge = DKnowledge(
        DataFrameBuilder.buildDataFrameForInference(expectedOutputMetadata))

      outputKnowledge shouldBe expectedOutputDKnowledge
      warnings shouldBe InferenceWarnings.empty
    }
  }

}
