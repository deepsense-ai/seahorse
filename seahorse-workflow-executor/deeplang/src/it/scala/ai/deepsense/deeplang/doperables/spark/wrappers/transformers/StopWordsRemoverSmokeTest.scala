/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperables.spark.wrappers.transformers

import org.apache.spark.sql.types.{ArrayType, DataType, StringType}

import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.SingleColumnChoice
import ai.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.NoInPlaceChoice
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

class StopWordsRemoverSmokeTest
    extends AbstractTransformerWrapperSmokeTest[StopWordsRemover]
    with MultiColumnTransformerWrapperTestSupport  {

  override def transformerWithParams: StopWordsRemover = {
    val inPlace = NoInPlaceChoice()
      .setOutputColumn("stopWordsRemoverOutput")
    val single = SingleColumnChoice()
      .setInputColumn(NameSingleColumnSelection("as"))
      .setInPlace(inPlace)

    val stopWordsRemover = new StopWordsRemover()
    stopWordsRemover.set(
      stopWordsRemover.singleOrMultiChoiceParam -> single,
      stopWordsRemover.caseSensitive -> false)
  }

  override def testValues: Seq[(Any, Any)] = {
    val inputNumbers = Seq(Array("a", "seahorse", "The", "Horseshoe", "Crab"))
    val outputNumbers = Seq(Array("seahorse", "Horseshoe", "Crab"))
    inputNumbers.zip(outputNumbers)
  }

  override def inputType: DataType = ArrayType(StringType)

  override def outputType: DataType = ArrayType(StringType)
}
