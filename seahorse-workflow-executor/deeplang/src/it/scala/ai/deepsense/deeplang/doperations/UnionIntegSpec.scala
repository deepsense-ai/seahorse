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

package ai.deepsense.deeplang.doperations

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.exceptions.SchemaMismatchException
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.{DKnowledge, DeeplangIntegTestSupport}

class UnionIntegSpec extends DeeplangIntegTestSupport {

  import DeeplangIntegTestSupport._
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
        .executeUntyped(Vector(df1, df2))(executionContext)
        .head.asInstanceOf[DataFrame]

      assertDataFramesEqual(
        merged, createDataFrame(rows1_1 ++ rows1_2, schema1))
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
        Union().executeUntyped(Vector(df1, df2))(executionContext)
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
        Union().executeUntyped(Vector(df1, df2))(executionContext)
      }
    }
  }

  it should {
    "propagate schema when both schemas match" in {
      val structType = StructType(Seq(
        StructField("x", DoubleType),
        StructField("y", DoubleType)))
      val knowledgeDF1 = DKnowledge(DataFrame.forInference(structType))
      val knowledgeDF2 = DKnowledge(DataFrame.forInference(structType))
      Union().inferKnowledgeUntyped(Vector(knowledgeDF1, knowledgeDF2))(mock[InferContext]) shouldBe
        (Vector(knowledgeDF1), InferenceWarnings())
    }
    "generate error when schemas don't match" in {
      val structType1 = StructType(Seq(
        StructField("x", DoubleType)))
      val structType2 = StructType(Seq(
        StructField("y", DoubleType)))
      val knowledgeDF1 = DKnowledge(DataFrame.forInference(structType1))
      val knowledgeDF2 = DKnowledge(DataFrame.forInference(structType2))
      an [SchemaMismatchException] shouldBe thrownBy(
        Union().inferKnowledgeUntyped(Vector(knowledgeDF1, knowledgeDF2))(mock[InferContext]))
    }
  }
}
