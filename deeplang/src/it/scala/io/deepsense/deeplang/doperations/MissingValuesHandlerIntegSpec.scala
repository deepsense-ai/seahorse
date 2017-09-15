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

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.scalatest.Matchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoriesMapping, MappingMetadataConverter, CategoricalMetadata, CategoricalMapper}
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

      val handler =
        new MissingValuesHandler()
          .setSelectedColumns(columnSelection)
          .setStrategy(MissingValuesHandler.Strategy.RemoveRow())
          .setMissingValueIndicator(
            MissingValuesHandler.MissingValueIndicatorChoice.Yes()
              .setIndicatorPrefix("prefix_"))

      val resultDf = executeOperation(handler, df)

      val expectedDf = createDataFrame(
        Seq(
          Row(1.0, null, false),
          Row(2.0, null, false),
          Row(4.0, 4.0, false),
          Row(5.0, 5.0, false)),
        StructType(List(
          StructField("value1", DoubleType, nullable = true),
          StructField("value2", DoubleType, nullable = true),
          StructField("prefix_value1", BooleanType, nullable = false)
        ))
      )

      assertDataFramesEqual(resultDf, expectedDf)
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
        new MissingValuesHandler()
          .setSelectedColumns(columnSelection)
          .setStrategy(MissingValuesHandler.Strategy.RemoveColumn())
          .setMissingValueIndicator(
            MissingValuesHandler.MissingValueIndicatorChoice.Yes()
              .setIndicatorPrefix("prefix_")),
        df)

      val expectedDf = createDataFrame(
        Seq(
          Row(1.0, null, false, true, false),
          Row(2.0, null, false, false, false),
          Row(3.0, null, false, false, false),
          Row(4.0, null, false, false, true),
          Row(5.0, null, false, false, false)),
        StructType(List(
          StructField("value1", DoubleType, nullable = true),
          StructField("value4", StringType, nullable = true),
          StructField("prefix_value1", BooleanType, nullable = false),
          StructField("prefix_value2", BooleanType, nullable = false),
          StructField("prefix_value3", BooleanType, nullable = false)))
      )

      assertDataFramesEqual(resultDf, expectedDf)
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

      val handler =
        new MissingValuesHandler()
          .setSelectedColumns(columnSelection)
          .setStrategy(
            MissingValuesHandler.Strategy.ReplaceWithCustomValue()
              .setCustomValue("3"))
          .setMissingValueIndicator(
            MissingValuesHandler.MissingValueIndicatorChoice.Yes()
              .setIndicatorPrefix("prefix_"))

      val resultDf = executeOperation(handler, df)

      val expectedDf = createDataFrame(
        Seq(
          Row(1.0, null, false),
          Row(2.0, null, false),
          Row(3.0, null, true),
          Row(4.0, null, false)),
        StructType(List(
          StructField("value1", DoubleType, nullable = true),
          StructField("value2", StringType, nullable = true),
          StructField("prefix_value1", BooleanType, nullable = false)))
      )

      assertDataFramesEqual(resultDf, expectedDf)
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
        new MissingValuesHandler()
          .setSelectedColumns(columnSelection)
          .setStrategy(
            MissingValuesHandler.Strategy.ReplaceWithCustomValue()
              .setCustomValue("ccc")),
        df)

      val expectedDf = createDataFrame(
        Seq(
          Row("aaa", null),
          Row("bbb", null),
          Row("ccc", null),
          Row("ddd", null)
        ),
        StructType(List(
          StructField("value1", StringType, nullable = true),
          StructField("value2", StringType, nullable = true)
        ))
      )

      assertDataFramesEqual(resultDf, expectedDf)
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
        new MissingValuesHandler()
          .setSelectedColumns(columnSelection)
          .setStrategy(
            MissingValuesHandler.Strategy.ReplaceWithCustomValue()
              .setCustomValue("true")),
        df)

      val expectedDf = createDataFrame(
        Seq(
          Row(true, null),
          Row(false, null),
          Row(true, null),
          Row(false, null)
        ),
        StructType(List(
          StructField("value1", BooleanType, nullable = true),
          StructField("value2", StringType, nullable = true))
        )
      )

      assertDataFramesEqual(resultDf, expectedDf)
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
        new MissingValuesHandler()
          .setSelectedColumns(columnSelection)
          .setStrategy(
            MissingValuesHandler.Strategy.ReplaceWithCustomValue()
              .setCustomValue("blue"))
        , df)

      val expectedDf = createDataFrame(
        Seq(
          Row(1, 0, null),
          Row(1, 1, null),
          Row(0, 1, null),
          Row(0, 0, null)
        ),
        StructType(List(
          StructField(
            "value1",
            IntegerType,
            nullable = true,
            metadata = createMetadata(Seq("red", "blue"))),
          StructField(
            "value2",
            IntegerType,
            nullable = true,
            metadata = createMetadata(Seq("green", "blue"))),
          StructField(
            "value3",
            StringType,
            nullable = true)))
      )

      assertDataFramesEqual(resultDf, expectedDf)
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
        new MissingValuesHandler()
          .setSelectedColumns(columnSelection)
          .setStrategy(
            MissingValuesHandler.Strategy.ReplaceWithCustomValue()
              .setCustomValue("2015-03-30 15:25:00.0"))
        , df)

      val expectedDf = createDataFrame(
        Seq(
          Row(t1, null),
          Row(t2, null),
          Row(t3, null),
          Row(t4, null)
        ),
        StructType(List(
          StructField("value1", TimestampType, nullable = true),
          StructField("value2", StringType, nullable = true)
        ))
      )

      assertDataFramesEqual(resultDf, expectedDf)
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
        new MissingValuesHandler()
          .setSelectedColumns(columnSelection)
          .setStrategy(
            MissingValuesHandler.Strategy.ReplaceWithCustomValue()
              .setCustomValue("3"))
        , df)
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
        new MissingValuesHandler()
          .setSelectedColumns(columnSelection)
          .setStrategy(
            MissingValuesHandler.Strategy.ReplaceWithCustomValue()
              .setCustomValue("aaaa"))
        , df)
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

      val handler =
        new MissingValuesHandler()
          .setSelectedColumns(columnSelection)
          .setStrategy(
            MissingValuesHandler.Strategy.ReplaceWithMode()
              .setEmptyColumnStrategy(
                MissingValuesHandler.EmptyColumnsStrategy.RetainEmptyColumns()))
          .setMissingValueIndicator(
            MissingValuesHandler.MissingValueIndicatorChoice.Yes()
              .setIndicatorPrefix("prefix_"))

      val resultDf = executeOperation(handler, df)

      val expectedDf = createDataFrame(
        Seq(
          Row(1.0, "aaa", null, false, true, true),
          Row(1.0, "aaa", null, true, false, true),
          Row(1.0, "aaa", null, false, false, true),
          Row(1.0, "aaa", null, false, false, true),
          Row(100.0, "bbb", null, false, false, true)
        ),
        StructType(List(
          StructField("value1", DoubleType, nullable = true),
          StructField("value2", StringType, nullable = true),
          StructField("value3", StringType, nullable = true),
          StructField("prefix_value1", BooleanType, nullable = false),
          StructField("prefix_value2", BooleanType, nullable = false),
          StructField("prefix_value3", BooleanType, nullable = false)
        ))
      )

      assertDataFramesEqual(resultDf, expectedDf)
    }

    "replace with mode using REPLACE_WITH_MODE strategy in REMOVE mode" in {
      val values = Seq(
        Row(1.0, null, "red", null),
        Row(null, 2.0, "blue", null),
        Row(1.0, 2.0, "blue", null),
        Row(1.0, 2.0, "blue", null),
        Row(100.0, 100.0, null, null)
      )

      val rawDf = createDataFrame(values, StructType(List(
        StructField("value1", DoubleType, nullable = true),
        StructField("value2", DoubleType, nullable = true),
        StructField("value3", StringType, nullable = true),
        StructField("value4", StringType, nullable = true)
      )))

      val df = CategoricalMapper(rawDf, executionContext.dataFrameBuilder).categorized("value3")

      val columnSelection = MultipleColumnSelection(
        Vector(IndexRangeColumnSelection(Some(0), Some(3))))
      val resultDf = executeOperation(
        new MissingValuesHandler()
          .setSelectedColumns(columnSelection)
          .setStrategy(
            MissingValuesHandler.Strategy.ReplaceWithMode()
              .setEmptyColumnStrategy(
                MissingValuesHandler.EmptyColumnsStrategy.RemoveEmptyColumns()))
        , df)

      val expectedDf = createDataFrame(
        Seq(
          Row(1.0, 2.0, 1),
          Row(1.0, 2.0, 0),
          Row(1.0, 2.0, 0),
          Row(1.0, 2.0, 0),
          Row(100.0, 100.0, 0)
        ),
        StructType(List(
          StructField(
            "value1", DoubleType, nullable = true),
          StructField(
            "value2", DoubleType, nullable = true),
          StructField(
            "value3", IntegerType, nullable = true, metadata = createMetadata(Seq("red", "blue")))
        ))
      )

      assertDataFramesEqual(resultDf, expectedDf)
    }

  }

  private def createMetadata(categoricalValues: Seq[String]) =
    MappingMetadataConverter.mappingToMetadata(CategoriesMapping(categoricalValues))

}
