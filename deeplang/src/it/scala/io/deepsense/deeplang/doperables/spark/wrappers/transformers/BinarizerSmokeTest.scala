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

import org.apache.spark.sql.types.{DataType, DoubleType}

import io.deepsense.deeplang.doperables.multicolumn.MultiColumnTransformerParams.SingleOrMultiColumnChoices.SingleColumnChoice
import io.deepsense.deeplang.doperables.multicolumn.SingleColumnTransformerParams.SingleTransformInPlaceChoices.NoInPlaceChoice
import io.deepsense.deeplang.params.selections.NameSingleColumnSelection

class BinarizerSmokeTest
    extends AbstractTransformerWrapperSmokeTest[Binarizer]
    with MultiColumnTransformerWrapperTestSupport  {

  override def transformerWithParams: Binarizer = {
    val inPlace = NoInPlaceChoice()
      .setColumnName("binarizerOutput")
    val single = SingleColumnChoice()
      .setInputColumn(NameSingleColumnSelection("d"))
      .setInPlace(inPlace)

    val binarizer = new Binarizer()
    binarizer.set(
      binarizer.singleOrMultiChoiceParam -> single,
      binarizer.threshold -> 0.5)
  }

  override def testValues: Seq[(Any, Any)] = {
    val inputNumbers = Seq(0.2, 0.5, 1.8)
    val outputNumbers = Seq(0.0, 0.0, 1.0)
    inputNumbers.zip(outputNumbers)
  }

  override def inputType: DataType = DoubleType

  override def outputType: DataType = DoubleType
}
