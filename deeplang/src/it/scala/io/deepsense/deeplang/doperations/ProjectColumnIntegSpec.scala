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

import java.sql.Timestamp

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.scalatest.Matchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException
import io.deepsense.deeplang.parameters.ColumnType.ColumnType
import io.deepsense.deeplang.parameters._

class ProjectColumnIntegSpec
  extends DeeplangIntegTestSupport
  with GeneratorDrivenPropertyChecks
  with Matchers {

  val columns = Seq(
    StructField("c", DoubleType),
    StructField("b", StringType),
    StructField("a", DoubleType),
    StructField("x", TimestampType),
    StructField("z", BooleanType))

  def schema = StructType(columns)

  // Projected:  0  "b"/1   "a"/2 3                                     "z"/4
  val row1 = Seq(1, "str1", 10.0, new Timestamp(DateTime.now.getMillis), true)
  val row2 = Seq(2, "str2", 20.0, new Timestamp(DateTime.now.getMillis), false)
  val row3 = Seq(3, "str3", 30.0, new Timestamp(DateTime.now.getMillis), false)
  val data = Seq(row1, row2, row3)

  "ProjectColumn" should {
    "select correct columns based on the column selection" in {
      val projected = projectColumns(Set("z", "b"), Set(1, 2), Set(ColumnType.numeric))
      val selectedIndices = Set(0, 1, 2, 4) // c b a z
      val expectedColumns = selectWithIndices[StructField](selectedIndices, columns)
      val expectedSchema = StructType(expectedColumns)
      val expectedData = data.map(r => selectWithIndices[Any](selectedIndices, r.toList))
      val expectedDataFrame = createDataFrame(expectedData.map(Row.fromSeq), expectedSchema)
      assertDataFramesEqual(projected, expectedDataFrame)
    }

    "throw an exception" when {
      "the columns selected by name does not exist" in {
        intercept[ColumnsDoNotExistException] {
          val nonExistingColumnName = "thisColumnDoesNotExist"
          projectColumns(
            Set(nonExistingColumnName),
            Set.empty,
            Set.empty)
        }
        ()
      }
      "the columns selected by index does not exist" in {
        intercept[ColumnsDoNotExistException] {
          val nonExistingColumnIndex = 1000
          projectColumns(
            Set.empty,
            Set(nonExistingColumnIndex),
            Set.empty)
        }
        ()
      }
    }

    "produce an empty set" when {
      "selecting a type that does not exist" in {
        val emptyDataFrame = projectColumns(
          Set.empty,
          Set.empty,
          Set(ColumnType.categorical)
        )
        emptyDataFrame.sparkDataFrame.collectAsList() shouldBe empty
      }
      "selection is empty" in {
        val emptyDataFrame = projectColumns(
          Set.empty,
          Set.empty,
          Set.empty
        )
        emptyDataFrame.sparkDataFrame.collectAsList() shouldBe empty
      }
    }
  }

  private def projectColumns(
      names: Set[String],
      ids: Set[Int],
      types: Set[ColumnType]): DataFrame = {
    val testDataFrame = createDataFrame(data.map(Row.fromSeq), schema)
    executeOperation(operation(names, ids, types), testDataFrame)
  }

  private def operation(
      names: Set[String],
      ids: Set[Int],
      types: Set[ColumnType]): ProjectColumns = {
    val operation = new ProjectColumns
    val valueParam = operation.parameters.getColumnSelectorParameter(operation.selectedColumns)
    valueParam.value = Some(MultipleColumnSelection(Vector(
      NameColumnSelection(names),
      IndexColumnSelection(ids),
      TypeColumnSelection(types)), false))
    operation
  }

  private def selectWithIndices[T](indices: Set[Int], sequence: Seq[T]): Seq[T] =
    sequence.zipWithIndex
      .filter { case (_, index) => indices.contains(index) }
      .map { case (v, _) => v }
}
