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

class RegexTokenizerSmokeTest
  extends AbstractTransformerWrapperSmokeTest[RegexTokenizer]
  with MultiColumnTransformerWrapperTestSupport {

  override def transformerWithParams: RegexTokenizer = {
    val inPlace = NoInPlaceChoice()
      .setOutputColumn("tokenized")

    val single = SingleColumnChoice()
      .setInputColumn(NameSingleColumnSelection("s"))
      .setInPlace(inPlace)

    val transformer = new RegexTokenizer()
    transformer.set(Seq(
      transformer.singleOrMultiChoiceParam -> single,
      transformer.gaps -> false,
      transformer.minTokenLength -> 1,
      transformer.pattern -> "\\d+"
    ): _*)
  }

  override def testValues: Seq[(Any, Any)] = {
    val strings = Seq(
      "100 200 300",
      "400 500 600",
      "700 800 900"
    )

    val tokenized = strings.map { _.toLowerCase.split(" ") }
    strings.zip(tokenized)
  }

  override def inputType: DataType = StringType

  override def outputType: DataType = new ArrayType(StringType, true)
}
