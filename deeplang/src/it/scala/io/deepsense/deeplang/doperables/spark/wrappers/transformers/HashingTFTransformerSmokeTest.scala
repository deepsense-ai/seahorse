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
import org.apache.spark.sql.types._

import io.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.SingleColumnChoice
import io.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.NoInPlaceChoice
import io.deepsense.deeplang.params.selections.NameSingleColumnSelection

class HashingTFTransformerSmokeTest
  extends AbstractTransformerWrapperSmokeTest[HashingTFTransformer]
  with MultiColumnTransformerWrapperTestSupport {

  override def transformerWithParams: HashingTFTransformer = {
     val inPlace = NoInPlaceChoice()
      .setOutputColumn("mapped")

    val single = SingleColumnChoice()
      .setInputColumn(NameSingleColumnSelection("as"))
      .setInPlace(inPlace)

    val transformer = new HashingTFTransformer()
    transformer.set(Seq(
      transformer.singleOrMultiChoiceParam -> single,
      transformer.numFeatures -> 20.0
    ): _*)
  }

  override def testValues: Seq[(Any, Any)] = {
    val arrays = Seq(
      Array("John", "likes", "to", "watch", "movies", "John"),
      Array("Mary", "likes", "movies", "too"),
      Array("guitar", "guitar", "guitar", "guitar")
    )

    arrays.zip(Seq(
      Vectors.sparse(20, Array(3, 7, 15, 16, 19), Array(1.0, 1.0, 1.0, 1.0, 2.0)),
      Vectors.sparse(20, Array(3, 8, 16, 19), Array(1.0, 1.0, 1.0, 1.0)),
      Vectors.sparse(20, Array(6), Array(4.0))))
  }

  override def inputType: DataType = ArrayType(StringType)

  override def outputType: DataType = new VectorUDT
}
