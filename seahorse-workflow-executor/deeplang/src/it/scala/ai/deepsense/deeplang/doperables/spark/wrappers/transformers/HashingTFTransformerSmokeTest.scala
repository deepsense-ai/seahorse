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

import org.apache.spark.ml.feature.{HashingTF => SparkHashingTF}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.SingleColumnChoice
import ai.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.NoInPlaceChoice
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection
import ai.deepsense.sparkutils.Linalg

class HashingTFTransformerSmokeTest
  extends AbstractTransformerWrapperSmokeTest[HashingTFTransformer]
  with MultiColumnTransformerWrapperTestSupport {

  import HashingTFTransformerSmokeTest.NumFeatures

  override def transformerWithParams: HashingTFTransformer = {
     val inPlace = NoInPlaceChoice()
      .setOutputColumn("mapped")

    val single = SingleColumnChoice()
      .setInputColumn(NameSingleColumnSelection("as"))
      .setInPlace(inPlace)

    val transformer = new HashingTFTransformer()
    transformer.set(Seq(
      transformer.singleOrMultiChoiceParam -> single,
      transformer.numFeatures -> NumFeatures
    ): _*)
  }

  override def testValues: Seq[(Any, Any)] = {
    val arrays = Seq(
      Array("John", "likes", "to", "watch", "movies", "John"),
      Array("Mary", "likes", "movies", "too"),
      Array("guitar", "guitar", "guitar", "guitar")
    )

    val outputArray = {
      // unfortunately, we cannot write outputs explicitly, because the behaviour changes between Spark 1.6 and 2.0
      val inputCol = "test_input"
      val outputCol = "test_output"
      val sparkHashingTF = new SparkHashingTF()
        .setNumFeatures(NumFeatures.toInt)
        .setInputCol(inputCol)
        .setOutputCol(outputCol)
      val inputDF = sparkSQLSession.createDataFrame(
        sparkContext.parallelize(arrays.map(Row(_))),
        StructType(Seq(StructField(inputCol, dataType = ArrayType(StringType)))))
      val outputDF = sparkHashingTF.transform(inputDF).select(outputCol)
      outputDF.rdd.map(r => r.getAs[Linalg.Vector](0)).collect
    }
    arrays.zip(outputArray)
  }

  override def inputType: DataType = ArrayType(StringType)

  override def outputType: DataType = new ai.deepsense.sparkutils.Linalg.VectorUDT
}

object HashingTFTransformerSmokeTest {
  val NumFeatures = 20.0
}
