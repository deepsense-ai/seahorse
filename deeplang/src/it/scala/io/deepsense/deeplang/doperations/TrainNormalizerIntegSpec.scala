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

package io.deepsense.deeplang.doperations

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.Normalizer
import io.deepsense.deeplang.doperables.dataframe.DataFrame

class TrainNormalizerIntegSpec extends DeeplangIntegTestSupport {

  val trainNormalizerOperation = TrainNormalizer(Set("A"), Set(1))

  val schema = StructType(Seq(
    StructField("A", DoubleType),
    StructField("B", DoubleType),
    StructField("C", StringType)
  ))

  val data = Seq(
    Row(1.0, 1.1, "a"),
    Row(10.0, 1.2, "b"),
    Row(100.0, 1.3, "c"),
    Row(1000.0, 1.4, "d"),
    Row(10000.0, 1.5, "e"),
    Row(100000.0, 1.6, "f"),
    Row(1000000.0, 1.7, "g"),
    Row(10000000.0, 1.8, "h")
  )

  val dataFrame = createDataFrame(data, schema)

  val expectedData = Array(
    Row(-0.397223682964733, -1.4288690166235198, "a"),
    Row(-0.39722110895338836, -1.0206207261596574, "b"),
    Row(-0.39719536883994194, -0.612372435695794, "c"),
    Row(-0.39693796770547796, -0.20412414523193165, "d"),
    Row(-0.3943639563608382, 0.20412414523193165, "e"),
    Row(-0.3686238429144407, 0.6123724356957949, "f"),
    Row(-0.11122270845046557, 1.0206207261596574, "g"),
    Row(2.462788636189286, 1.4288690166235207, "h")
  )

  "TrainNormalizer" should {
    "create normalizer and normalize input DF" in {
      val results = trainNormalizerOperation.execute(executionContext)(Vector(dataFrame))

      val normalizedDF: DataFrame = results.head.asInstanceOf[DataFrame]
      val normalizer: Normalizer = results(1).asInstanceOf[Normalizer]

      val normalizedOutside = normalizer.normalize.apply(executionContext)(())(dataFrame)
      rowsAlmostEqual(normalizedDF.sparkDataFrame.sort("C").collect(), expectedData)
      assertDataFramesEqual(normalizedOutside, normalizedDF)
    }
  }

  private def rowsAlmostEqual(a: Array[Row], b: Array[Row]): Unit = {
    a.zip(b).foreach {
      case (row1, row2) => row1.toSeq.zip(row2.toSeq).foreach {
        case (d1: Double, d2: Double) =>
          DoubleUtils.double2String(d1) shouldBe DoubleUtils.double2String(d2)
        case (v1, v2) =>  v1 shouldBe v2
      }
    }
  }
}
