/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import org.apache.spark.sql.types._

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException
import io.deepsense.deeplang.parameters._

class DataFrameIntegSpec extends DeeplangIntegTestSupport {

  "DataFrame" should {
    def schema = StructType(List(
      StructField("c", DoubleType),
      StructField("b", StringType),
      StructField("a", DoubleType),
      StructField("y", LongType),
      StructField("x", TimestampType),
      StructField("z", BooleanType),
      StructField("m", IntegerType)
    ))

    def dataFrame = createDataFrame(Seq.empty, schema)

    "return correct sequence of columns' names based on column selection" when {

      "many selectors are used" in {
        val selection = MultipleColumnSelection(Vector(
          NameColumnSelection(Set("a")),
          IndexColumnSelection(Set(1, 3)),
          TypeColumnSelection(Set(ColumnType.string, ColumnType.timestamp))
        ))
        dataFrame.getColumnNames(selection) shouldBe Seq("b", "a", "y", "x")
      }

      "columns are selected in different order" in {
        val selection = MultipleColumnSelection(Vector(
          NameColumnSelection(Set("a", "b", "c"))
        ))
        dataFrame.getColumnNames(selection) shouldBe Seq("c", "b", "a")
      }

      def selectSingleType(columnType: ColumnType.ColumnType): Seq[String] = {
        val selection = MultipleColumnSelection(Vector(TypeColumnSelection(Set(columnType))))
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

      "ordinal type is selected" in {
        selectSingleType(ColumnType.ordinal) shouldBe Seq("y")
      }

      "timestamp type is selected" in {
        selectSingleType(ColumnType.timestamp) shouldBe Seq("x")
      }

      "categorical type is selected" in {
        selectSingleType(ColumnType.categorical) shouldBe Seq("m")
      }
    }

    "throw an exception" when {
      "non-existing column name was selected" in {
        intercept[ColumnsDoNotExistException] {
          val selection = MultipleColumnSelection(Vector(
            NameColumnSelection(Set("no such column"))))
          dataFrame.getColumnNames(selection)
        }
      }
      "index out of bounds was selected" in {
        intercept[ColumnsDoNotExistException] {
          val selection = MultipleColumnSelection(Vector(
            IndexColumnSelection(Set(10))))
          dataFrame.getColumnNames(selection)
        }
      }
    }
  }
}
