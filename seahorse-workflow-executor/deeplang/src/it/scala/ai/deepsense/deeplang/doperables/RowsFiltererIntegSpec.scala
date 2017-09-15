/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperables

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.scalatest.Matchers

import ai.deepsense.deeplang._
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.spark.wrappers.transformers.TransformerSerialization

class RowsFiltererIntegSpec
  extends DeeplangIntegTestSupport
  with Matchers
  with TransformerSerialization {

  import DeeplangIntegTestSupport._
  import TransformerSerialization._

  val columns = Seq(
    StructField("a", DoubleType),
    StructField("b", StringType),
    StructField("c", BooleanType))

  def schema: StructType = StructType(columns)

  val row1 = Seq(1.0, "aaa", true)
  val row2 = Seq(2.0, "b", false)
  val row3 = Seq(3.3, "cc", true)
  val data = Seq(row1, row2, row3)

  "RowsFilterer" should {

    "select correct rows based on the condition" in {
      val filterer = new RowsFilterer().setCondition("a > 1 AND c = TRUE")

      val dataFrame: DataFrame = createDataFrame(data.map(Row.fromSeq), schema)
      val result =
        filterer.applyTransformationAndSerialization(tempDir, dataFrame)
      val expectedDataFrame = createDataFrame(Seq(row3).map(Row.fromSeq), schema)
      assertDataFramesEqual(result, expectedDataFrame)
    }

    "infer correct schema" in {
      val filterer = new RowsFilterer().setCondition("a > 1")
      filterer._transformSchema(schema) shouldBe Some(schema)
    }
  }
}
