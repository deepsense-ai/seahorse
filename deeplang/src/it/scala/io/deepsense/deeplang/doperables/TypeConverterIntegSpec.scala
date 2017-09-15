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

package io.deepsense.deeplang.doperables

import java.sql.Timestamp

import org.apache.spark.SparkException
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.{DateTime, DateTimeZone}

import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.types.ColumnType._
import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.TypeConverter.TargetTypeChoice
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException
import io.deepsense.deeplang.params.selections.{IndexColumnSelection, MultipleColumnSelection, NameColumnSelection, TypeColumnSelection}

class TypeConverterIntegSpec extends DeeplangIntegTestSupport {

  var inputDataFrame: DataFrame = _

  "TypeConverter" when {
    "converting columns to String" which {
      val targetType = StringType
      "are Doubles" should {
        "cast them to String" in {
          val converted = useTypeConverter(Set(doubleId), Set.empty, Set.empty, targetType)
          val expected = toString(Set(doubleId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Strings" should {
        "return the same values" in {
          val converted = useTypeConverter(Set.empty, Set.empty, Set(ColumnType.string), targetType)
          assertDataFramesEqual(converted, inputDataFrame)
        }
      }
      "are Booleans" should {
        "return 'true', 'false' strings" in {
          val converted = useTypeConverter(Set(booleanId), Set.empty, Set.empty, targetType)
          val expected = toString(Set(booleanId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Timestamps" should {
        "use ISO 8601 format" in {
          val converted = useTypeConverter(Set(timestampId), Set.empty, Set.empty, targetType)
          val expected = toString(Set(timestampId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Longs" should {
        "cast them to String" in {
          val converted = useTypeConverter(Set(longId), Set.empty, Set.empty, targetType)
          val expected = toString(Set(longId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Integers" should {
        "cast them to String" in {
          val converted = useTypeConverter(Set(integerId), Set.empty, Set.empty, targetType)
          val expected = toString(Set(integerId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Floats" should {
        "cast them to String" in {
          val converted = useTypeConverter(Set(floatId), Set.empty, Set.empty, targetType)
          val expected = toString(Set(floatId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Bytes" should {
        "cast them to String" in {
          val converted = useTypeConverter(Set(byteId), Set.empty, Set.empty, targetType)
          val expected = toString(Set(byteId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Shorts" should {
        "cast them to String" in {
          val converted = useTypeConverter(Set(shortId), Set.empty, Set.empty, targetType)
          val expected = toString(Set(shortId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Decimals" should {
        "cast them to String" in {
          val converted = useTypeConverter(Set(decimalId), Set.empty, Set.empty, targetType)
          val expected = toString(Set(decimalId))
          assertDataFramesEqual(converted, expected)
        }
      }
    }
    "converting columns to Double" which {
      val targetType = DoubleType
      "are strings" should {
        "return values as Double" when {
          "strings represent numbers" in {
            val converted = useTypeConverter(Set(numStrId), Set.empty, Set.empty, targetType)
            val expected = toDouble(Set(numStrId))
            assertDataFramesEqual(converted, expected)
          }
        }
        "fail" when {
          "strings DO NOT represent numbers" in {
            a[SparkException] should be thrownBy {
              useTypeConverter(Set(nonNumStrId), Set.empty, Set.empty, targetType)
                .sparkDataFrame.collect()
            }
          }
        }
      }
      "are Boolean" should {
        "return 1.0s and 0.0s" in {
          val converted = useTypeConverter(Set(booleanId), Set.empty, Set.empty, targetType)
          val expected = toDouble(Set(booleanId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Timestamp" should {
        "convert the value to millis and then to double" in {
          val converted = useTypeConverter(Set(timestampId), Set.empty, Set.empty, targetType)
          val expected = toDouble(Set(timestampId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Longs" should {
        "cast them to Double" in {
          val converted = useTypeConverter(Set(longId), Set.empty, Set.empty, targetType)
          val expected = toDouble(Set(longId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Integers" should {
        "cast them to Double" in {
          val converted = useTypeConverter(Set(integerId), Set.empty, Set.empty, targetType)
          val expected = toDouble(Set(integerId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Floats" should {
        "cast them to Double" in {
          val converted = useTypeConverter(Set(floatId), Set.empty, Set.empty, targetType)
          val expected = toDouble(Set(floatId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Bytes" should {
        "cast them to Double" in {
          val converted = useTypeConverter(Set(byteId), Set.empty, Set.empty, targetType)
          val expected = toDouble(Set(byteId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Shorts" should {
        "cast them to Double" in {
          val converted = useTypeConverter(Set(shortId), Set.empty, Set.empty, targetType)
          val expected = toDouble(Set(shortId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Decimals" should {
        "cast them to Double" in {
          val converted = useTypeConverter(Set(decimalId), Set.empty, Set.empty, targetType)
          val expected = toDouble(Set(decimalId))
          assertDataFramesEqual(converted, expected)
        }
      }
    }
  }

  it should {
    "transform schema" in {
      val originalSchema = StructType(Seq(
        StructField("col1", DoubleType),
        StructField("col2", StringType),
        StructField("col3", BooleanType),
        StructField("col4", DoubleType),
        StructField("col5", TimestampType)
      ))

      val transformedSchema = new TypeConverter()
        .setSelectedColumns(
          MultipleColumnSelection(Vector(
            TypeColumnSelection(Set(ColumnType.numeric)),
            NameColumnSelection(Set("col5"))
          )))
        .setTargetType(TargetTypeChoice.StringTargetTypeChoice())
        ._transformSchema(originalSchema)

      transformedSchema shouldBe Some(StructType(Seq(
        StructField("col1", StringType),
        StructField("col2", StringType),
        StructField("col3", BooleanType),
        StructField("col4", StringType),
        StructField("col5", StringType)
      )))
    }

    "throw an exception" when {
      "selected columns do not exist" in {
        val originalSchema = StructType(Seq(
          StructField("col", DoubleType)
        ))

        val operation = new TypeConverter()
          .setSelectedColumns(
            MultipleColumnSelection(Vector(
              NameColumnSelection(Set("non-existent"))
            )))
          .setTargetType(TargetTypeChoice.StringTargetTypeChoice())

        a[ColumnsDoNotExistException] should be thrownBy {
          operation._transformSchema(originalSchema)
        }
      }
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    inputDataFrame = createDataFrame(inputRows, schema)
  }

  val emptyMetadata = new MetadataBuilder().build()

  // Fixtures Seq(normal, string, double)
  val doubleColumn = {
    val original = Seq(5.123456789, null, 3.14)
    val asString = Seq("5.123457", null, "3.14")
    ColumnContainer(original, original, asString, emptyMetadata)
  }

  // String type
  val strColumn = {
    val original = Seq("string1", null, "string3")
    ColumnContainer(original, null, original, emptyMetadata)
  }

  // Numerical string type
  val numStrColumn = {
    val original = Seq("5", null, "0.314e1")
    val asDouble = Seq(5.0, null, 3.14)
    ColumnContainer(original, asDouble, original, emptyMetadata)
  }

  val boolColumn = {
    val original = Seq(true, null, false)
    val asString = Seq("true", null, "false")
    val asDouble = Seq(1.0, null, 0.0)
    ColumnContainer(original, asDouble, asString, emptyMetadata)
  }

  val timestampColumn = {
    val dateTime1 = new DateTime(1989, 4, 23, 3, 14, 15, 926, DateTimeZone.UTC)
    val dateTime2 = dateTime1.plusDays(1).plusMinutes(1)
    val timestamp1 = new Timestamp(dateTime1.getMillis)
    val timestamp2 = new Timestamp(dateTime2.getMillis)
    val dateTime1String = "1989-04-23T03:14:15.926Z"
    val dateTime2String = "1989-04-24T03:15:15.926Z"
    val original = Seq(timestamp1, null, timestamp2)
    val asString = Seq(dateTime1String, null, dateTime2String)
    val asDouble = Seq(dateTime1.getMillis.toDouble, null, dateTime2.getMillis.toDouble)
    ColumnContainer(original, asDouble, asString, emptyMetadata)
  }

  val longColumn = {
    val original = Seq(1L, null, 42L)
    val asString = Seq("1", null, "42")
    val asDouble = Seq(1.0, null, 42.0)
    ColumnContainer(original, asDouble, asString, emptyMetadata)
  }

  val integerColumn = {
    val original = Seq(1, null, 42)
    val asString = Seq("1", null, "42")
    val asDouble = Seq(1.0, null, 42.0)
    ColumnContainer(original, asDouble, asString, emptyMetadata)
  }

  val floatColumn = {
    val original = Seq(1.5f, null, 42.0f)
    val asString = Seq("1.5", null, "42")
    val asDouble = Seq(1.5, null, 42.0)
    ColumnContainer(original, asDouble, asString, emptyMetadata)
  }

  val byteColumn = {
    val original = Seq(1.toByte, null, 42.toByte)
    val asString = Seq("1", null, "42")
    val asDouble = Seq(1.0, null, 42.0)
    ColumnContainer(original, asDouble, asString, emptyMetadata)
  }

  val shortColumn = {
    val original = Seq(1.toShort, null, 42.toShort)
    val asString = Seq("1", null, "42")
    val asDouble = Seq(1.0, null, 42.0)
    ColumnContainer(original, asDouble, asString, emptyMetadata)
  }

  val decimalColumn = {
    val original = Seq(Decimal(3.14), null, Decimal(42.0))
    val asString = Seq("3.14", null, "42.0")
    val asDouble = Seq(3.14, null, 42.0)
    ColumnContainer(original, asDouble, asString, emptyMetadata)
  }

  case class ColumnContainer(
    original: Seq[Any],
    asDouble: Seq[Any],
    asString: Seq[Any],
    originalMetadata: Metadata)

  val columns = Seq(
    doubleColumn,
    numStrColumn,
    strColumn,
    boolColumn,
    timestampColumn,
    longColumn,
    integerColumn,
    floatColumn,
    byteColumn,
    shortColumn,
    decimalColumn)

  val schema = StructType(Seq(
    StructField("doubles", DoubleType), // 0
    StructField("num strings", StringType), // 1
    StructField("non num strings", StringType), // 2
    StructField("booleans", BooleanType), // 3
    StructField("timestamps", TimestampType), // 4
    StructField("longs", LongType), // 5
    StructField("integers", IntegerType), // 6
    StructField("floats", FloatType), // 7
    StructField("bytes", ByteType), // 8
    StructField("shorts", ShortType), // 9
    StructField("decimals", DecimalType()) // 10
  ))

  val rowsNumber = 3

  val doubleId = 0
  val numStrId = 1
  val nonNumStrId = 2
  val booleanId = 3
  val timestampId = 4
  val longId = 5
  val integerId = 6
  val floatId = 7
  val byteId = 8
  val shortId = 9
  val decimalId = 10

  val originalColumns = columns.map(_.original)
  val inputRows = (0 until rowsNumber).map(i => Row.fromSeq(originalColumns.map(_.apply(i))))

  def to(dataType: DataType, ids: Set[Int], newSchema: Option[StructType])
      (f: ColumnContainer => Seq[Any]): DataFrame = {
    val values = columns.zipWithIndex.map { case (cc, idx) =>
      if (ids.contains(idx)) {
        f(cc)
      } else {
        cc.original
      }
    }
    val rows = (0 until rowsNumber).map(i => Row.fromSeq(values.map(_.apply(i))))
    val updatedSchema = newSchema.getOrElse(ids.foldLeft(schema) { case (oldSchema, index) =>
      StructType(oldSchema.updated(index, oldSchema(index).copy(
        dataType = dataType, metadata = new MetadataBuilder().build())))
    })
    createDataFrame(rows, updatedSchema)
  }

  def toDouble(ids: Set[Int]): DataFrame = to(DoubleType, ids, None){ _.asDouble }

  def toString(ids: Set[Int]): DataFrame = to(StringType, ids, None){ _.asString }

  private def useTypeConverter(
      ids: Set[Int] = Set(),
      names: Set[String] = Set(),
      types: Set[ColumnType] = Set(),
      targetType: DataType,
      dataFrame: DataFrame = inputDataFrame): DataFrame = {
    val operation = new TypeConverter()
      .setSelectedColumns(
        MultipleColumnSelection(
          Vector(
            NameColumnSelection(names),
            IndexColumnSelection(ids),
            TypeColumnSelection(types))))
      .setTargetType(TargetTypeChoice.fromColumnType(targetType))

    operation.transform.apply(executionContext)(())(dataFrame)
  }
}
