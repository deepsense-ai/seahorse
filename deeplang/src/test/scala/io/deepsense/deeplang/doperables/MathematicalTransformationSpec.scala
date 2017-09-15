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

package io.deepsense.deeplang.doperables

import org.mockito.Mockito._

import io.deepsense.deeplang.doperables.transformations.MathematicalTransformation
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
