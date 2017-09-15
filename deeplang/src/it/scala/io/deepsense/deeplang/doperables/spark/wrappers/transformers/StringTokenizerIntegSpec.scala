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

import org.apache.spark.sql.types.{ArrayType, StringType, DataType}

import io.deepsense.deeplang.doperables.MultiColumnTransformerTestSupport
import io.deepsense.deeplang.doperables.multicolumn.{SingleColumnTransformerParams, MultiColumnTransformerParams}
import MultiColumnTransformerParams.SingleOrMultiColumnChoices.SingleColumnChoice
import SingleColumnTransformerParams.SingleTransformInPlaceChoices.NoInPlaceChoice
import io.deepsense.deeplang.params.ParamPair
import io.deepsense.deeplang.params.selections.NameSingleColumnSelection

class StringTokenizerIntegSpec
  extends AbstractTransformerWrapperSmokeTest
  with MultiColumnTransformerTestSupport {

  override def className: String = "StringTokenizer"

  override val transformer: StringTokenizer = new StringTokenizer()

  override val transformerParams: Seq[ParamPair[_]] = {
     val inPlace = NoInPlaceChoice()
      .setColumnName("tokenized")

    val single = SingleColumnChoice()
      .setInputColumn(NameSingleColumnSelection("s"))
      .setInPlace(inPlace)

    Seq(
      transformer.singleOrMultiChoiceParam -> single
    )
  }

  override def transformerName: String = "StringTokenizer"

  override def testValues: Seq[(Any, Any)] = {
    val strings = Seq(
      "this is a test",
      "this values should be separated",
      "Bla bla bla!"
    )

    val tokenized = strings.map { _.toLowerCase.split("\\s") }
    strings.zip(tokenized)
  }

  override def inputType: DataType = StringType

  override def outputType: DataType = new ArrayType(StringType, true)
}
