/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.deeplang.doperables.dataframe.types.categorical

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import io.deepsense.deeplang.DeeplangIntegTestSupport

class CategoricalMapperIntegSpec extends DeeplangIntegTestSupport {

  val rows: Seq[Row] = Seq(
    Row("x_abc", "x", "abc"),
    Row("y_abc", "y", "abc"),
    Row("z_abc", "z", "abc"),
    Row("x_ABC", "x", "ABC"),
    Row("y_null", "y", null),
    Row("z_ABC", "z", "ABC")
  )

  "CategoricalMapper" should {
    "categorize columns in DataFrame" in {
      val name = "name"
      val cat1 = "category1"
      val cat2 = "category2"
      val manualSchema = StructType(List(
        StructField(name, StringType),
        StructField(cat1, StringType),
        StructField(cat2, StringType))
      )
      val df = createDataFrame(rows, manualSchema)
      val categorized = CategoricalMapper(df, executionContext.dataFrameBuilder)
        .categorized(cat1, cat2)

      val categoricalMetadata = CategoricalMetadata(categorized.sparkDataFrame)
      categoricalMetadata.isCategorical(name) shouldBe false
      categoricalMetadata.isCategorical(cat1) shouldBe true
      categoricalMetadata.isCategorical(cat2) shouldBe true

      def expectedCategories(value: String): (String, String) = {
        val categories = value.split("_")
        val category2 = if (categories(1) == "null") null else categories(1)
        (categories(0), category2)
      }

      categorized.sparkDataFrame.collect().foreach(r => {
        def mapIdToName(column: Int): String =
          if (r.isNullAt(column)) {
            null
          } else {
            categoricalMetadata.mapping(column).idToValue(r.getInt(column))
          }

        val (expectedCat1, expectedCat2) = expectedCategories(r.getString(0))
        mapIdToName(1) shouldBe expectedCat1
        mapIdToName(2) shouldBe expectedCat2
      })
    }
  }
}
