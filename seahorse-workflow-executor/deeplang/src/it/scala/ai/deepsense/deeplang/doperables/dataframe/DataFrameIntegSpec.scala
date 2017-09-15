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

package ai.deepsense.deeplang.doperables.dataframe

import org.apache.spark.sql.types._

import ai.deepsense.commons.types.ColumnType
import ai.deepsense.deeplang.DeeplangIntegTestSupport
import ai.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException
import ai.deepsense.deeplang.params.selections.{IndexColumnSelection, MultipleColumnSelection, NameColumnSelection, TypeColumnSelection}

class DataFrameIntegSpec extends DeeplangIntegTestSupport {

  "DataFrame" should {
    def schema: StructType = StructType(List(
      StructField("a", ArrayType(BooleanType)),
      StructField("b", BinaryType),
      StructField("c", BooleanType),
      StructField("d", ByteType),
      StructField("e", DateType),
      StructField("f", DecimalType(5, 5)),
      StructField("g", DoubleType),
      StructField("h", FloatType),
      StructField("i", IntegerType),
      StructField("j", LongType),
      StructField("k", MapType(StringType, StringType)),
      StructField("l", NullType),
      StructField("m", ShortType),
      StructField("n", StringType),
      StructField("o", StructType(Seq(StructField("n", StringType)))),
      StructField("p", TimestampType)
    ))

    def dataFrame: DataFrame = createDataFrame(Seq.empty, schema)

    "return correct sequence of columns' names based on column selection" when {

      "many selectors are used" in {
        val selection = MultipleColumnSelection(Vector(
          NameColumnSelection(Set("a")),
          IndexColumnSelection(Set(1, 3)),
          TypeColumnSelection(Set(ColumnType.string, ColumnType.timestamp))
        ), false)
        dataFrame.getColumnNames(selection) shouldBe Seq("a", "b", "d", "n", "p")
      }

      "columns are selected in different order" in {
        val selection = MultipleColumnSelection(Vector(
          NameColumnSelection(Set("c")),
          NameColumnSelection(Set("a")),
          NameColumnSelection(Set("b"))
        ), false)
        dataFrame.getColumnNames(selection) shouldBe Seq("a", "b", "c")
      }

      def selectSingleType(columnType: ColumnType.ColumnType): Seq[String] = {
        val selection = MultipleColumnSelection(Vector(TypeColumnSelection(Set(columnType))), false)
        dataFrame.getColumnNames(selection)
      }

      "boolean type is selected" in {
        selectSingleType(ColumnType.boolean) shouldBe Seq("c")
      }

      "string type is selected" in {
        selectSingleType(ColumnType.string) shouldBe Seq("n")
      }

      "numeric type is selected" in {
        selectSingleType(ColumnType.numeric) shouldBe Seq("d", "f", "g", "h", "i", "j", "m")
      }

      "timestamp type is selected" in {
        selectSingleType(ColumnType.timestamp) shouldBe Seq("p")
      }

      "excluding selector is used" in {
        val selection = MultipleColumnSelection(Vector(
          NameColumnSelection(Set("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m"))
        ), true)
        dataFrame.getColumnNames(selection) shouldBe Seq("n", "o", "p")
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
            IndexColumnSelection(Set(20))), false)
          dataFrame.getColumnNames(selection)
        }
        ()
      }
    }
  }
}
