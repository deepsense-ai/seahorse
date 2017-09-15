/**
 * Copyright (c) 2015, CodiLime Inc.
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

      val categoricalMetadata = CategoricalMetadata(categorized)
      categoricalMetadata.isCategorical(name) shouldBe false
      categoricalMetadata.isCategorical(cat1) shouldBe true
      categoricalMetadata.isCategorical(cat2) shouldBe true

      def expectedCategories(value: String) = {
        val categories = value.split("_")
        val category2 = if (categories(1) == "null") null else categories(1)
        (categories(0), category2)
      }

      categorized.sparkDataFrame.collect().foreach(r => {
        def mapIdToName(column: Int) =
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
