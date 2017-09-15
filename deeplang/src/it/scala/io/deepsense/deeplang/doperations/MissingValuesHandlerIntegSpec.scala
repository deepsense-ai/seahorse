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

import scala.collection.JavaConverters._

import org.apache.spark.SparkException
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.scalatest.Matchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoricalMapper
import io.deepsense.deeplang.doperations.exceptions.{WrongReplacementValueException, MultipleTypesReplacementException}
import io.deepsense.deeplang.parameters.ChoiceParameter.BinaryChoice
import io.deepsense.deeplang.parameters.{IndexRangeColumnSelection, MultipleColumnSelection}

class MissingValuesHandlerIntegSpec extends DeeplangIntegTestSupport
  with GeneratorDrivenPropertyChecks
  with Matchers {

  "MissingValuesHandler" should {
    "remove rows with empty values while using REMOVE_ROW strategy" in {
      val values = Seq(
        Row(1.0, null),
        Row(2.0, null),
        Row(null, 3.0),
        Row(4.0, 4.0),
        Row(5.0, 5.0),
        Row(null, null))

      val df = createDataFrame(values, StructType(List(
        StructField("value1", DoubleType, nullable = true),
        StructField("value2", DoubleType, nullable = true)
      )))

      val columnSelection = MultipleColumnSelection(
        Vector(IndexRangeColumnSelection(Some(0), Some(0))))

      val handler = MissingValuesHandler(
        columnSelection,
        MissingValuesHandler.Strategy.REMOVE_ROW,
        BinaryChoice.YES,
        Some("prefix_"))

      val resultDf = executeOperation(handler, df)

      val expectedRows = List(
        Row(1.0, null, false),
        Row(2.0, null, false),
        Row(4.0, 4.0, false),
        Row(5.0, 5.0, false)
      )

      resultDf.sparkDataFrame.columns shouldBe Array(
        "value1", "value2", "prefix_value1")
      resultDf.sparkDataFrame.collectAsList().asScala.toList shouldBe expectedRows
    }

    "remove columns with empty values while using REMOVE_COLUMN strategy" in {
      val values = Seq(
        Row(1.0, null, "ddd", null),
        Row(2.0, 2.0, "eee", null),
        Row(3.0, 3.0, "fff", null),
        Row(4.0, 4.0, null, null),
        Row(5.0, 5.0, "ggg", null)
      )

      val df = createDataFrame(values, StructType(List(
        StructField("value1", DoubleType, nullable = true),
        StructField("value2", DoubleType, nullable = true),
        StructField("value3", StringType, nullable = true),
        StructField("value4", StringType, nullable = true)
      )))

      val columnSelection = MultipleColumnSelection(
        Vector(IndexRangeColumnSelection(Some(0), Some(2))))
      val resultDf = executeOperation(
        MissingValuesHandler(
          columnSelection,
          MissingValuesHandler.Strategy.REMOVE_COLUMN,
          BinaryChoice.YES,
          Some("prefix_")),
        df)

      val expectedRows = List(
        Row(1.0, null, false, true, false),
        Row(2.0, null, false, false, false),
        Row(3.0, null, false, false, false),
        Row(4.0, null, false, false, true),
        Row(5.0, null, false, false, false)
      )
      resultDf.sparkDataFrame.columns shouldBe Array(
        "value1", "value4", "prefix_value1", "prefix_value2", "prefix_value3")
      resultDf.sparkDataFrame.collectAsList().asScala.toList shouldBe expectedRows
    }

    "replace numerics while using REPLACE_WITH_CUSTOM_VALUE strategy" in {
      val values = Seq(
        Row(1.0, null),
        Row(2.0, null),
        Row(null, null),
        Row(4.0, null)
      )

      val df = createDataFrame(values, StructType(List(
        StructField("value1", DoubleType, nullable = true),
        StructField("value2", StringType, nullable = true)
      )))

      val columnSelection = MultipleColumnSelection(
        Vector(IndexRangeColumnSelection(Some(0), Some(0))))

      val handler = MissingValuesHandler.replaceWithCustomValue(
        columnSelection,
        "3",
        BinaryChoice.YES,
        Some("prefix_"))

      val resultDf = executeOperation(handler, df)

      val expectedCols = Array("value1", "value2", "prefix_value1")

      val expectedRows = List(
        Row(1.0, null, false),
        Row(2.0, null, false),
        Row(3.0, null, true),
        Row(4.0, null, false)
      )
      resultDf.sparkDataFrame.columns shouldBe expectedCols
      resultDf.sparkDataFrame.collectAsList().asScala.toList shouldBe expectedRows
    }

    "replace strings while using REPLACE_WITH_CUSTOM_VALUE strategy" in {
      val values = Seq(
        Row("aaa", null),
        Row("bbb", null),
        Row(null, null),
        Row("ddd", null)
      )

      val df = createDataFrame(values, StructType(List(
        StructField("value1", StringType, nullable = true),
        StructField("value2", StringType, nullable = true)
      )))

      val columnSelection = MultipleColumnSelection(
        Vector(IndexRangeColumnSelection(Some(0), Some(0))))
      val resultDf = executeOperation(
        MissingValuesHandler.replaceWithCustomValue(columnSelection, "ccc"), df)

      val expectedCols = Array("value1", "value2")

      val expectedRows = List(
        Row("aaa", null),
        Row("bbb", null),
        Row("ccc", null),
        Row("ddd", null)
      )
      resultDf.sparkDataFrame.columns shouldBe expectedCols
      resultDf.sparkDataFrame.collectAsList().asScala.toList shouldBe expectedRows
    }

    "replace booleans while using REPLACE_WITH_CUSTOM_VALUE strategy" in {
      val values = Seq(
        Row(true, null),
        Row(false, null),
        Row(null, null),
        Row(false, null)
      )

      val df = createDataFrame(values, StructType(List(
        StructField("value1", BooleanType, nullable = true),
        StructField("value2", StringType, nullable = true)
      )))

      val columnSelection = MultipleColumnSelection(
        Vector(IndexRangeColumnSelection(Some(0), Some(0))))
      val resultDf = executeOperation(
        MissingValuesHandler.replaceWithCustomValue(columnSelection, "true"), df)

      val expectedCols = Array("value1", "value2")

      val expectedRows = List(
        Row(true, null),
        Row(false, null),
        Row(true, null),
        Row(false, null)
      )
      resultDf.sparkDataFrame.columns shouldBe expectedCols
      resultDf.sparkDataFrame.collectAsList().asScala.toList shouldBe expectedRows
    }

    "replace categoricals while using REPLACE_WITH_CUSTOM_VALUE strategy" in {
      val values = Seq(
        Row("red", "green", null),
        Row("red", null, null),
        Row("blue", "blue", null),
        Row(null, "green", null)
      )

      val rawDf = createDataFrame(values, StructType(List(
        StructField("value1", StringType, nullable = true),
        StructField("value2", StringType, nullable = true),
        StructField("value3", StringType, nullable = true)
      )))

      val df = CategoricalMapper(rawDf, executionContext.dataFrameBuilder)
        .categorized("value1", "value2")

      val columnSelection = MultipleColumnSelection(
        Vector(IndexRangeColumnSelection(Some(0), Some(1))))
      val resultDf = executeOperation(
        MissingValuesHandler.replaceWithCustomValue(columnSelection, "blue"), df)

      val expectedCols = Array("value1", "value2", "value3")
      val expectedRows = List(
        Row(1, 0, null),
        Row(1, 1, null),
        Row(0, 1, null),
        Row(0, 0, null)
      )
      resultDf.sparkDataFrame.columns shouldBe expectedCols
      resultDf.sparkDataFrame.collectAsList().asScala.toList shouldBe expectedRows
    }

    "replace timestamps while using REPLACE_WITH_CUSTOM_VALUE strategy" in {

      val base = new DateTime(2015, 3, 30, 15, 25)

      val t1 = new Timestamp(base.getMillis + 1e6.toLong)
      val t2 = new Timestamp(base.getMillis + 2e6.toLong)
      val t3 = new Timestamp(base.getMillis + 3e6.toLong)
      val t4 = new Timestamp(base.getMillis)

      val values = Seq(
        Row(t1, null),
        Row(t2, null),
        Row(t3, null),
        Row(null, null)
      )

      val df = createDataFrame(values, StructType(List(
        StructField("value1", TimestampType, nullable = true),
        StructField("value2", StringType, nullable = true)
      )))

      val columnSelection = MultipleColumnSelection(
        Vector(IndexRangeColumnSelection(Some(0), Some(0))))
      val resultDf = executeOperation(
        MissingValuesHandler.replaceWithCustomValue(columnSelection, "2015-03-30 15:25:00.0"), df)

      val expectedCols = Array("value1", "value2")
      val expectedRows = List(
        Row(t1, null),
        Row(t2, null),
        Row(t3, null),
        Row(t4, null)
      )
      resultDf.sparkDataFrame.columns shouldBe expectedCols
      resultDf.sparkDataFrame.collectAsList().asScala.toList shouldBe expectedRows
    }

    "throw an exception with different types using REPLACE_WITH_CUSTOM_VALUE strategy" in {
      val values = Seq(
        Row(1.0, "aaa", null),
        Row(2.0, "bbb", null),
        Row(null, "ccc", null),
        Row(4.0, null, null)
      )

      val df = createDataFrame(values, StructType(List(
        StructField("value1", DoubleType, nullable = true),
        StructField("value2", StringType, nullable = true),
        StructField("value3", StringType, nullable = true)
      )))

      val columnSelection = MultipleColumnSelection(
        Vector(IndexRangeColumnSelection(Some(0), Some(1))))

      an [MultipleTypesReplacementException] should be thrownBy executeOperation(
        MissingValuesHandler.replaceWithCustomValue(columnSelection, "3"), df)
    }

    "throw an exception with invalid value using REPLACE_WITH_CUSTOM_VALUE strategy" in {
      val values = Seq(
        Row(1.0, null),
        Row(2.0, null),
        Row(null, null),
        Row(4.0, null)
      )

      val df = createDataFrame(values, StructType(List(
        StructField("value1", DoubleType, nullable = true),
        StructField("value2", StringType, nullable = true)
      )))

      val columnSelection = MultipleColumnSelection(
        Vector(IndexRangeColumnSelection(Some(0), Some(0))))

      an [WrongReplacementValueException] should be thrownBy executeOperation(
        MissingValuesHandler.replaceWithCustomValue(columnSelection, "aaaa"), df)
    }

    "replace with mode using REPLACE_WITH_MODE strategy in RETAIN mode" in {
      val values = Seq(
        Row(1.0, null, null),
        Row(null, "aaa", null),
        Row(1.0, "aaa", null),
        Row(1.0, "aaa", null),
        Row(100.0, "bbb", null)
      )

      val df = createDataFrame(values, StructType(List(
        StructField("value1", DoubleType, nullable = true),
        StructField("value2", StringType, nullable = true),
        StructField("value3", StringType, nullable = true)
      )))

      val columnSelection = MultipleColumnSelection(
        Vector(IndexRangeColumnSelection(Some(0), Some(2))))

      val handler = MissingValuesHandler.replaceWithMode(
        columnSelection,
        MissingValuesHandler.EmptyColumnsMode.RETAIN,
        BinaryChoice.YES,
        Some("prefix_"))

      val resultDf = executeOperation(handler, df)

      val expectedRows = List(
        Row(1.0, "aaa", null, false, true, true),
        Row(1.0, "aaa", null, true, false, true),
        Row(1.0, "aaa", null, false, false, true),
        Row(1.0, "aaa", null, false, false, true),
        Row(100.0, "bbb", null, false, false, true)
      )
      resultDf.sparkDataFrame.columns shouldBe Array(
        "value1", "value2", "value3", "prefix_value1", "prefix_value2", "prefix_value3")
      resultDf.sparkDataFrame.collectAsList().asScala.toList shouldBe expectedRows
    }

    "replace with mode using REPLACE_WITH_MODE strategy in REMOVE mode" in {
      val values = Seq(
        Row(1.0, null, null),
        Row(null, 2.0, null),
        Row(1.0, 2.0, null),
        Row(1.0, 2.0, null),
        Row(100.0, 100.0, null)
      )

      val df = createDataFrame(values, StructType(List(
        StructField("value1", DoubleType, nullable = true),
        StructField("value2", DoubleType, nullable = true),
        StructField("value3", StringType, nullable = true)
      )))

      val columnSelection = MultipleColumnSelection(
        Vector(IndexRangeColumnSelection(Some(0), Some(2))))
      val resultDf = executeOperation(
        MissingValuesHandler.replaceWithMode(columnSelection,
          MissingValuesHandler.EmptyColumnsMode.REMOVE), df)

      val expectedRows = List(
        Row(1.0, 2.0),
        Row(1.0, 2.0),
        Row(1.0, 2.0),
        Row(1.0, 2.0),
        Row(100.0, 100.0)
      )
      resultDf.sparkDataFrame.columns shouldBe Array("value1", "value2")
      resultDf.sparkDataFrame.collectAsList().asScala.toList shouldBe expectedRows
    }

  }

}
