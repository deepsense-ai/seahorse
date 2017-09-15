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

import org.mockito.Mockito._

import io.deepsense.deeplang.doperables.dataframe.{DataFrameBuilder, CommonColumnMetadata, DataFrame, DataFrameMetadata}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.parameters.ColumnType
import io.deepsense.deeplang.{DKnowledge, UnitSpec}

abstract class ScorableSpec[T <: Scorable] extends UnitSpec {

  def scorableName: String

  def scorable: Scorable

  scorableName should {
    "infer metadata with column appended" in {

      val metadata = DataFrameMetadata(
        isExact = false,
        isColumnCountExact = false,
        columns = Map(
          "num_col" -> CommonColumnMetadata("num_col", Some(0), Some(ColumnType.numeric)))
      )

      val columnName = "prediction"

      val expectedOutputMetadata = DataFrameMetadata(
        isExact = false,
        isColumnCountExact = false,
        columns = Map(
          "num_col" -> CommonColumnMetadata("num_col", Some(0), Some(ColumnType.numeric)),
          columnName -> CommonColumnMetadata(columnName, None, Some(ColumnType.numeric))
        )
      )

      val inferContext = mock[InferContext]
      when(inferContext.fullInference).thenReturn(true)

      val dKnowledge = DKnowledge(DataFrameBuilder.buildDataFrameForInference(metadata))
      val (outputKnowledge, warnings) = scorable.score.infer(inferContext)(columnName)(dKnowledge)

      val expectedOutputDKnowledge = DKnowledge(
        DataFrameBuilder.buildDataFrameForInference(expectedOutputMetadata))

      outputKnowledge shouldBe expectedOutputDKnowledge
      warnings shouldBe InferenceWarnings.empty
    }
  }
}
