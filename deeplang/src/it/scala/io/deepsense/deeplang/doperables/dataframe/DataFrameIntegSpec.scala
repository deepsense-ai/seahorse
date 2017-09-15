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

package io.deepsense.deeplang.doperables.dataframe

import org.apache.spark.sql.types._

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException
import io.deepsense.deeplang.parameters._

class DataFrameIntegSpec extends DeeplangIntegTestSupport {

  "DataFrame" should {
    def schema: StructType = StructType(List(
      StructField("c", DoubleType),
      StructField("b", StringType),
      StructField("a", DoubleType),
      StructField("x", TimestampType),
      StructField("z", BooleanType),
      StructField("m", IntegerType)
    ))

    def dataFrame: DataFrame = createDataFrame(Seq.empty, schema)

    "return correct sequence of columns' names based on column selection" when {

      "many selectors are used" in {
        val selection = MultipleColumnSelection(Vector(
          NameColumnSelection(Set("a")),
          IndexColumnSelection(Set(1, 3)),
          TypeColumnSelection(Set(ColumnType.string, ColumnType.timestamp))
        ), false)
        dataFrame.getColumnNames(selection) shouldBe Seq("b", "a", "x")
      }

      "columns are selected in different order" in {
        val selection = MultipleColumnSelection(Vector(
          NameColumnSelection(Set("a", "b", "c"))
        ), false)
        dataFrame.getColumnNames(selection) shouldBe Seq("c", "b", "a")
      }

      def selectSingleType(columnType: ColumnType.ColumnType): Seq[String] = {
        val selection = MultipleColumnSelection(Vector(TypeColumnSelection(Set(columnType))), false)
        dataFrame.getColumnNames(selection)
      }

      "boolean type is selected" in {
        selectSingleType(ColumnType.boolean) shouldBe Seq("z")
      }

      "string type is selected" in {
        selectSingleType(ColumnType.string) shouldBe Seq("b")
      }

      "numeric type is selected" in {
        selectSingleType(ColumnType.numeric) shouldBe Seq("c", "a")
      }

      "timestamp type is selected" in {
        selectSingleType(ColumnType.timestamp) shouldBe Seq("x")
      }

      "categorical type is selected" in {
        selectSingleType(ColumnType.categorical) shouldBe Seq("m")
      }

      "excluding selector is used" in {
        val selection = MultipleColumnSelection(Vector(
          NameColumnSelection(Set("a")),
          IndexColumnSelection(Set(1, 3)),
          TypeColumnSelection(Set(ColumnType.string, ColumnType.timestamp))
        ), true)
        dataFrame.getColumnNames(selection) shouldBe Seq("c", "z", "m")
      }
    }

    "throw an exception" when {
      "non-existing column name was selected" in {
        intercept[ColumnsDoNotExistException] {
          val selection = MultipleColumnSelection(Vector(
            NameColumnSelection(Set("no such column"))), false)
          dataFrame.getColumnNames(selection)
        }
        ()
      }
      "index out of bounds was selected" in {
        intercept[ColumnsDoNotExistException] {
          val selection = MultipleColumnSelection(Vector(
            IndexColumnSelection(Set(10))), false)
          dataFrame.getColumnNames(selection)
        }
        ()
      }
    }
  }
}
