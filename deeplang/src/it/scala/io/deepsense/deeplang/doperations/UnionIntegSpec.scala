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
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoricalMetadata
import io.deepsense.deeplang.doperations.exceptions.SchemaMismatchException

class UnionIntegSpec extends DeeplangIntegTestSupport {

  val schema1 = StructType(List(
    StructField("column1", DoubleType),
    StructField("column2", DoubleType)))

  val rows1_1 = Seq(
    Row(1.0, 2.0),
    Row(2.0, 3.0)
  )

  "Union" should {
    "return a union of two DataFrames" in {
      val rows1_2 = Seq(
        Row(2.0, 4.0),
        Row(4.0, 6.0)
      )

      val df1 = createDataFrame(rows1_1, schema1)
      val df2 = createDataFrame(rows1_2, schema1)

      val merged = Union()
        .execute(executionContext)(Vector(df1, df2))
        .head.asInstanceOf[DataFrame]

      assertDataFramesEqual(
        merged, createDataFrame(rows1_1 ++ rows1_2, schema1))
    }

    "return a union of two DataFrames containing categorical columns" in {
      val schema = StructType(List(
        StructField("column1", DoubleType),
        StructField("column2", StringType),
        StructField("column3", DoubleType)))

      val rows2_1 = Seq(
        Row(33.1, "a", 2.0),
        Row(99.1, "b", 3.0)
      )

      val rows2_2 = Seq(
        Row(21.1, "a", 7.0),
        Row(99.1, "c", 9.0)
      )

      val df1 = createDataFrame(rows2_1, schema, Seq("column1", "column2"))
      val df2 = createDataFrame(rows2_2, schema, Seq("column1", "column2"))

      val merged = Union()
        .execute(executionContext)(Vector(df1, df2))
        .head.asInstanceOf[DataFrame]

      val column1FinalMapping =
        CategoricalMetadata(df1).mapping("column1").mergeWith(
          CategoricalMetadata(df2).mapping("column1")
        ).finalMapping

      val column2FinalMapping =
        CategoricalMetadata(df1).mapping("column2").mergeWith(
          CategoricalMetadata(df2).mapping("column2")
        ).finalMapping

      val mergedRows = merged.sparkDataFrame.rdd.collect()
      mergedRows shouldBe Array(
        Row(column1FinalMapping.valueToId("33.1"), column2FinalMapping.valueToId("a"), 2.0),
        Row(column1FinalMapping.valueToId("99.1"), column2FinalMapping.valueToId("b"), 3.0),
        Row(column1FinalMapping.valueToId("21.1"), column2FinalMapping.valueToId("a"), 7.0),
        Row(column1FinalMapping.valueToId("99.1"), column2FinalMapping.valueToId("c"), 9.0))

      CategoricalMetadata(merged).mapping("column1") shouldBe column1FinalMapping
      CategoricalMetadata(merged).mapping("column2") shouldBe column2FinalMapping
    }

    "throw for identical schemas and one DF having a categorical column" in {
      val rows2_1 = Seq(
        Row(1.1, 1.0),
        Row(1.1, 1.0)
      )

      val df1 = createDataFrame(rows1_1, schema1, Seq("column1"))
      val df2 = createDataFrame(rows2_1, schema1)

      a [SchemaMismatchException] should be thrownBy {
        Union().execute(executionContext)(Vector(df1, df2))
      }
    }

    "throw for mismatching types in DataFrames" in {
      val schema2 = StructType(List(
        StructField("column1", StringType),
        StructField("column2", DoubleType)))

      val rows2_1 = Seq(
        Row("a", 1.0),
        Row("b", 1.0)
      )

      val df1 = createDataFrame(rows1_1, schema1)
      val df2 = createDataFrame(rows2_1, schema2)

      a [SchemaMismatchException] should be thrownBy {
        Union().execute(executionContext)(Vector(df1, df2))
      }
    }

    "throw for mismatching column names in DataFrames" in {
      val schema2 = StructType(List(
        StructField("column1", DoubleType),
        StructField("different_column_name", DoubleType)))

      val rows2_1 = Seq(
        Row(1.1, 1.0),
        Row(1.1, 1.0)
      )

      val df1 = createDataFrame(rows1_1, schema1)
      val df2 = createDataFrame(rows2_1, schema2)

      a [SchemaMismatchException] should be thrownBy {
        Union().execute(executionContext)(Vector(df1, df2))
      }
    }
  }
}
