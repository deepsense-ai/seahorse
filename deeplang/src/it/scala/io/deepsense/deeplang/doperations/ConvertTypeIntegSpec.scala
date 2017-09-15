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

import org.apache.spark.SparkException
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.{DateTime, DateTimeZone}

import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.types.ColumnType._
import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.ConvertType.TargetTypeChoice
import io.deepsense.deeplang.parameters._

class ConvertTypeIntegSpec extends DeeplangIntegTestSupport {

  var inputDataFrame: DataFrame = _

  "Convert Type" when {
    "converting columns to String" which {
      val targetType = ColumnType.string
      "are Doubles" should {
        "cast them to String" in {
          val converted = useConvertType(Set(doubleId), Set.empty, Set.empty, targetType)
          val expected = toString(Set(doubleId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Strings" should {
        "return the same values" in {
          val converted = useConvertType(Set.empty, Set.empty, Set(ColumnType.string), targetType)
          assertDataFramesEqual(converted, inputDataFrame)
        }
      }
      "are Booleans" should {
        "return 'true', 'false' strings" in {
          val converted = useConvertType(Set(booleanId), Set.empty, Set.empty, targetType)
          val expected = toString(Set(booleanId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Timestamps" should {
        "use ISO 8601 format" in {
          val converted = useConvertType(Set(timestampId), Set.empty, Set.empty, targetType)
          val expected = toString(Set(timestampId))
          assertDataFramesEqual(converted, expected)
        }
      }
    }
    "converting columns to Double" which {
      val targetType = ColumnType.numeric
      "are strings" should {
        "return values as Double" when {
          "strings represent numbers" in {
            val converted = useConvertType(Set(numStrId), Set.empty, Set.empty, targetType)
            val expected = toDouble(Set(numStrId))
            assertDataFramesEqual(converted, expected)
          }
        }
        "fail" when {
          "strings DO NOT represent numbers" in {
            a[SparkException] should be thrownBy {
              useConvertType(Set(nonNumStrId), Set.empty, Set.empty, targetType)
                .sparkDataFrame.collect()
            }
          }
        }
      }
      "are Boolean" should {
        "return 1.0s and 0.0s" in {
          val converted = useConvertType(Set(booleanId), Set.empty, Set.empty, targetType)
          val expected = toDouble(Set(booleanId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Timestamp" should {
        "convert the value to millis and then to double" in {
          val converted = useConvertType(Set(timestampId), Set.empty, Set.empty, targetType)
          val expected = toDouble(Set(timestampId))
          assertDataFramesEqual(converted, expected)
        }
      }
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    inputDataFrame = createDataFrame(inputRows, schema)
  }

  val emptyMetadata = new MetadataBuilder().build()

  // Fixtures Seq(normal, string, double, categorical)
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
    timestampColumn)

  val schema = StructType(Seq(
    StructField("doubles", DoubleType), // 0
    StructField("num strings", StringType), // 1
    StructField("non num strings", StringType), // 2
    StructField("booleans", BooleanType), // 3
    StructField("timestamps", TimestampType) // 4
  ))

  val rowsNumber = 3

  val doubleId = 0
  val numStrId = 1
  val nonNumStrId = 2
  val booleanId = 3
  val timestampId = 4

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

  private def useConvertType(
      ids: Set[Int] = Set(),
      names: Set[String] = Set(),
      types: Set[ColumnType] = Set(),
      targetType: ColumnType,
      dataFrame: DataFrame = inputDataFrame): DataFrame = {
    val operation = new ConvertType()
      .setSelectedColumns(
        MultipleColumnSelection(
          Vector(
            NameColumnSelection(names),
            IndexColumnSelection(ids),
            TypeColumnSelection(types))))
      .setTargetType(TargetTypeChoice.fromColumnType(targetType))

    executeOperation(operation, dataFrame)
  }

}
