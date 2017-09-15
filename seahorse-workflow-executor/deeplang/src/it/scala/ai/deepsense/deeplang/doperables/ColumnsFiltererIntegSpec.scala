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

package ai.deepsense.deeplang.doperables

import java.sql.Timestamp

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.scalatest.Matchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import ai.deepsense.deeplang._
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.spark.wrappers.transformers.TransformerSerialization
import ai.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException
import ai.deepsense.deeplang.params.selections.{IndexColumnSelection, MultipleColumnSelection, NameColumnSelection, TypeColumnSelection}

class ColumnsFiltererIntegSpec
  extends DeeplangIntegTestSupport
  with GeneratorDrivenPropertyChecks
  with Matchers
  with TransformerSerialization {

  import DeeplangIntegTestSupport._
  import TransformerSerialization._

  val columns = Seq(
    StructField("c", IntegerType),
    StructField("b", StringType),
    StructField("a", DoubleType),
    StructField("x", TimestampType),
    StructField("z", BooleanType))

  def schema: StructType = StructType(columns)

  //         "c"/0  "b"/1   "a"/2 3                                     "z"/4
  val row1 = Seq(1, "str1", 10.0, new Timestamp(DateTime.now.getMillis), true)
  val row2 = Seq(2, "str2", 20.0, new Timestamp(DateTime.now.getMillis), false)
  val row3 = Seq(3, "str3", 30.0, new Timestamp(DateTime.now.getMillis), false)
  val data = Seq(row1, row2, row3)
  val dataFrame = createDataFrame(data.map(Row.fromSeq), schema)

  "ColumnsFilterer" should {
    val names: Set[String] = Set("z", "b")
    val indices: Set[Int] = Set(1, 2)
    val selectedIndices = Set(1, 2, 4) // c b a z
    val expectedColumns = selectWithIndices[StructField](selectedIndices, columns)
    val expectedSchema = StructType(expectedColumns)

    "select correct columns based on the column selection" in {
      val transformer = filterColumnTransformer(names, indices)
      val filtered = filterColumns(transformer)
      val expectedData = data.map(r => selectWithIndices[Any](selectedIndices, r.toList))
      val expectedDataFrame = createDataFrame(expectedData.map(Row.fromSeq), expectedSchema)
      assertDataFramesEqual(filtered, expectedDataFrame)
      val filteredBySerializedTransformer = filterColumnsUsingSerializedTransformer(transformer)
      assertDataFramesEqual(filtered, filteredBySerializedTransformer)
    }
    "infer correct schema" in {
      val filteredSchema = filterColumnsSchema(names, indices)
      filteredSchema shouldBe Some(expectedSchema)
    }
    "throw an exception" when {
      "the columns selected by name does not exist" when {
        "transforming a DataFrame" in {
          intercept[ColumnsDoNotExistException] {
            val nonExistingColumnName = "thisColumnDoesNotExist"
            filterColumns(
              Set(nonExistingColumnName),
              Set.empty)
          }
        }
        "transforming a schema" in {
          intercept[ColumnsDoNotExistException] {
            val nonExistingColumnName = "thisColumnDoesNotExist"
            filterColumnsSchema(
              Set(nonExistingColumnName),
              Set.empty)
          }
        }
      }
      "the columns selected by index does not exist" when {
        "transforming a DataFrame" in {
          intercept[ColumnsDoNotExistException] {
            val nonExistingColumnIndex = 1000
            filterColumns(
              Set.empty,
              Set(nonExistingColumnIndex))
          }
        }
        "transforming a schema" in {
          intercept[ColumnsDoNotExistException] {
            val nonExistingColumnIndex = 1000
            filterColumnsSchema(
              Set.empty,
              Set(nonExistingColumnIndex))
          }
        }
      }
    }
  }
  it when {
    "selection is empty" should {
      "produce an empty DataFrame" in {
        val emptyDataFrame = filterColumns(Set.empty, Set.empty)
        emptyDataFrame.sparkDataFrame.collectAsList() shouldBe empty
      }
      "produce an empty schema" in {
        val Some(inferredSchema) = filterColumnsSchema(Set.empty, Set.empty)
        inferredSchema.fields shouldBe empty
      }
    }
  }

  private def filterColumns(names: Set[String], ids: Set[Int]): DataFrame = {
    filterColumns(filterColumnTransformer(names, ids))
  }

  private def filterColumns(transformer: Transformer): DataFrame = {
    transformer._transform(executionContext, dataFrame)
  }

  private def filterColumnsUsingSerializedTransformer(transformer: Transformer): DataFrame = {
    transformer.loadSerializedTransformer(tempDir)._transform(executionContext, dataFrame)
  }

  private def filterColumnsSchema(names: Set[String], ids: Set[Int]): Option[StructType] =
    filterColumnTransformer(names, ids)._transformSchema(schema)

  private def filterColumnTransformer(names: Set[String], ids: Set[Int]): Transformer =
    new ColumnsFilterer().setSelectedColumns(
      MultipleColumnSelection(Vector(
        NameColumnSelection(names),
        IndexColumnSelection(ids),
        TypeColumnSelection(Set())), excluding = false))

  private def selectWithIndices[T](indices: Set[Int], sequence: Seq[T]): Seq[T] =
    sequence.zipWithIndex.collect { case (v, index) if indices.contains(index) => v }
}
