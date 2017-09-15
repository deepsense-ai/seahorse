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

import ai.deepsense.sparkutils.Linalg.Vectors
import org.apache.spark.sql.types.DataType

import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.SingleColumnChoice
import ai.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.NoInPlaceChoice
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

class PolynomialExpanderSmokeTest
  extends AbstractTransformerWrapperSmokeTest[PolynomialExpander]
  with MultiColumnTransformerWrapperTestSupport {

  override def transformerWithParams: PolynomialExpander = {
    val inPlace = NoInPlaceChoice()
      .setOutputColumn("polynomial")

    val single = SingleColumnChoice()
      .setInputColumn(NameSingleColumnSelection("v"))
      .setInPlace(inPlace)

    val transformer = new PolynomialExpander()
    transformer.set(Seq(
      transformer.singleOrMultiChoiceParam -> single,
      transformer.degree -> 3
    ): _*)
  }

  override def testValues: Seq[(Any, Any)] = {
    val input = Seq(
      Vectors.dense(1.0),
      Vectors.dense(1.0, 2.0)
    )
    val inputAfterDCT = Seq(
      // x, x^2, x^3
      Vectors.dense(1.0, 1.0, 1.0),
      // x, x^2, x^3, y, x * y, x^2 * y, x * y^2, y^2, y^3
      Vectors.dense(1.0, 1.0, 1.0, 2.0, 2.0, 2.0, 4.0, 4.0, 8.0)
    )
    input.zip(inputAfterDCT)
  }

  override def inputType: DataType = new ai.deepsense.sparkutils.Linalg.VectorUDT

  override def outputType: DataType = new ai.deepsense.sparkutils.Linalg.VectorUDT
}
