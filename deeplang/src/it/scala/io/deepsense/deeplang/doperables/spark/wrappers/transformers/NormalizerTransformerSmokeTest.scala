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

package io.deepsense.deeplang.doperables.spark.wrappers.transformers

import org.apache.spark.mllib.linalg.{VectorUDT, Vectors}
import org.apache.spark.sql.types.DataType

import io.deepsense.deeplang.doperables.multicolumn.MultiColumnTransformerParams.SingleOrMultiColumnChoices.SingleColumnChoice
import io.deepsense.deeplang.doperables.multicolumn.SingleColumnTransformerParams.SingleTransformInPlaceChoices.NoInPlaceChoice
import io.deepsense.deeplang.params.selections.NameSingleColumnSelection

class NormalizerTransformerSmokeTest
  extends AbstractTransformerWrapperSmokeTest[NormalizerTransformer]
  with MultiColumnTransformerWrapperTestSupport {

  override def transformerWithParams: NormalizerTransformer = {
    val inPlace = NoInPlaceChoice()
      .setColumnName("normalize")

    val single = SingleColumnChoice()
      .setInputColumn(NameSingleColumnSelection("v"))
      .setInPlace(inPlace)

    val transformer = new NormalizerTransformer()
    transformer.set(Seq(
      transformer.singleOrMultiChoiceParam -> single,
      transformer.p -> 1.0
    ): _*)
  }

  override def testValues: Seq[(Any, Any)] = {
    val input = Seq(
      Vectors.dense(0.0, 100.0, 100.0),
      Vectors.dense(1.0, 1.0, 0.0),
      Vectors.dense(-3.0, 3.0, 0.0)
    )
    val inputAfterNormalize = Seq(
      Vectors.dense(0.0, 0.5, 0.5),
      Vectors.dense(0.5, 0.5, 0.0),
      Vectors.dense(-0.5, 0.5, 0.0)
    )
    input.zip(inputAfterNormalize)
  }

  override def inputType: DataType = new VectorUDT

  override def outputType: DataType = new VectorUDT
}
