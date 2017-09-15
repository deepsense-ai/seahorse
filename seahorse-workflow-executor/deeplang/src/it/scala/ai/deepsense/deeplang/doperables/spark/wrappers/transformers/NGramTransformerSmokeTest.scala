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

class NGramTransformerSmokeTest
  extends AbstractTransformerWrapperSmokeTest[NGramTransformer]
  with MultiColumnTransformerWrapperTestSupport {

  override def transformerWithParams: NGramTransformer = {
    val inPlace = NoInPlaceChoice()
      .setOutputColumn("ngrams")

    val single = SingleColumnChoice()
      .setInputColumn(NameSingleColumnSelection("as"))
      .setInPlace(inPlace)

    val transformer = new NGramTransformer()
    transformer.set(Seq(
      transformer.singleOrMultiChoiceParam -> single,
      transformer.n -> 2
    ): _*)
  }

  override def testValues: Seq[(Any, Any)] = {
    val strings = Seq(
      Array("a", "b", "c"),
      Array("d", "e", "f")
    )

    val ngrams = Seq(
      Array("a b", "b c"),
      Array("d e", "e f")
    )
    strings.zip(ngrams)
  }

  override def inputType: DataType = new ArrayType(StringType, true)

  override def outputType: DataType = new ArrayType(StringType, false)
}
