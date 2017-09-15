/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import java.sql.Timestamp

import org.apache.spark.SparkException
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.{DateTime, DateTimeZone}

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoriesMapping, MappingMetadataConverter}
import io.deepsense.deeplang.parameters.ColumnType._
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
      "are Categoricals" should {
        "return mapped categories' names" in {
          val converted =
            useConvertType(Set(numCatId, nonNumCatId), Set.empty, Set.empty, targetType)
          val expected = toString(Set(numCatId, nonNumCatId))
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
      "are Categorical" should {
        "return values as Double" when {
          "strings represent numbers" in {
            val converted = useConvertType(Set(numCatId), Set.empty, Set.empty, targetType)
            val expected = toDouble(Set(numCatId))
            assertDataFramesEqual(converted, expected)
          }
        }
        "fail" when {
          "strings DO NOT represent numbers" in {
            a[SparkException] should be thrownBy {
              useConvertType(Set(nonNumCatId), Set.empty, Set.empty, targetType)
                .sparkDataFrame.collect()
            }
          }
        }
      }
    }
    "convert columns to Categorical" which {
      val targetType = ColumnType.categorical
      "are Double" should {
        "use their string representation as category names" in {
          val converted = useConvertType(Set(doubleId), Set.empty, Set.empty, targetType)
          val expected = toCategorical(Set(doubleId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are String" should {
        "use their values as category names" in {
          val converted =
            useConvertType(Set(numStrId, nonNumStrId), Set.empty, Set.empty, targetType)
          val expected = toCategorical(Set(numStrId, nonNumStrId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Boolean" should {
        "use 'true', 'false' as category names" in {
          val converted = useConvertType(Set(booleanId), Set.empty, Set.empty, targetType)
          val expected = toCategorical(Set(booleanId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Timestamp" should {
        "use their ISO 8601 representation as category names" in {
          val converted = useConvertType(Set(timestampId), Set.empty, Set.empty, targetType)
          val expected = toCategorical(Set(timestampId))
          assertDataFramesEqual(converted, expected)
        }
      }
      "are Categorical" should {
        "return the same values" in {
          val converted =
            useConvertType(Set(numCatId, nonNumCatId), Set.empty, Set.empty, targetType)
          assertDataFramesEqual(converted, inputDataFrame)
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
    val mapping = CategoriesMapping(Seq(3.14.toString, 5.123456789.toString))
    val asCategorical = mapSeq(mapping, original)
    ColumnContainer(original, original, asString, asCategorical, mapping, emptyMetadata)
  }

  // String type
  val strColumn = {
    val original = Seq("string1", null, "string3")
    val mapping = CategoriesMapping(Seq("string1", "string3"))
    val asCategorical = mapSeq(mapping, original)
    ColumnContainer(original, null, original, asCategorical, mapping, emptyMetadata)
  }

  // Numerical string type
  val numStrColumn = {
    val original = Seq("5", null, "0.314e1")
    val asDouble = Seq(5.0, null, 3.14)
    val mapping = CategoriesMapping(Seq("5", "0.314e1"))
    val asCategorical = mapSeq(mapping, original)
    ColumnContainer(original, asDouble, original, asCategorical, mapping, emptyMetadata)
  }

  val boolColumn = {
    val original = Seq(true, null, false)
    val asString = Seq("true", null, "false")
    val asDouble = Seq(1.0, null, 0.0)
    val mapping = CategoriesMapping(Seq("true", "false"))
    val asCategorical = mapSeq(mapping, original)
    ColumnContainer(original, asDouble, asString, asCategorical, mapping, emptyMetadata)
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
    val mapping = CategoriesMapping(Seq(dateTime1String, dateTime2String))
    val asCategorical = mapSeq(mapping, asString)
    ColumnContainer(original, asDouble, asString, asCategorical, mapping, emptyMetadata)
  }

  // Non numerical categorical column
  val categoricalColumn = {
    val original = Seq(0, null, 2)
    val mapping = CategoriesMapping(Seq("Category1", "Category2", "7.0"))
    val metadata = MappingMetadataConverter.mappingToMetadata(mapping)
    val asString = unmapSeq(mapping, original)
    ColumnContainer(original, null, asString, original, mapping, metadata)
  }

  // Numerical categorical column
  val numCategoricalColumn = {
    val original = Seq(0, null, 2)
    val mapping = CategoriesMapping(Seq("5", "8", "13"))
    val metadata = MappingMetadataConverter.mappingToMetadata(mapping)
    val asString = unmapSeq(mapping, original)
    val asDouble = Seq(5.0, null, 13.0)
    ColumnContainer(original, asDouble, asString, original, mapping, metadata)
  }

  private def mapSeq(mapping: CategoriesMapping, values: Seq[Any]): Seq[Any] =
    values.map(v => if (v == null) null else mapping.valueToId(v.toString))

  private def unmapSeq(mapping: CategoriesMapping, ids: Seq[Any]): Seq[Any] =
    ids.map(id => if (id == null) null else mapping.idToValue(id.asInstanceOf[Int]))

  case class ColumnContainer(
    original: Seq[Any],
    asDouble: Seq[Any],
    asString: Seq[Any],
    asCategorical: Seq[Any],
    mapping: CategoriesMapping,
    originalMetadata: Metadata)

  val columns = Seq(
    doubleColumn,
    numStrColumn,
    strColumn,
    boolColumn,
    timestampColumn,
    numCategoricalColumn,
    categoricalColumn
  )

  val numCategoriesMeta = MappingMetadataConverter
    .mappingToMetadata(numCategoricalColumn.mapping)
  val nonNumCategoriesMeta = MappingMetadataConverter
    .mappingToMetadata(categoricalColumn.mapping)

  val schema = StructType(Seq(
    StructField("doubles", DoubleType), // 0
    StructField("num strings", StringType), // 1
    StructField("non num strings", StringType), // 2
    StructField("booleans", BooleanType), // 3
    StructField("timestamps", TimestampType), // 4
    StructField("num categorical", IntegerType, metadata = numCategoriesMeta), // 5
    StructField("non num categorical", IntegerType, metadata = nonNumCategoriesMeta) // 6
  ))

  val rowsNumber = 3

  val doubleId = 0
  val numStrId = 1
  val nonNumStrId = 2
  val booleanId = 3
  val timestampId = 4
  val numCatId = 5
  val nonNumCatId = 6

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

  def toCategorical(ids: Set[Int]): DataFrame = {
    val updatedSchema = ids.foldLeft(schema) { case (oldSchema, index) =>
      StructType(
        oldSchema.updated(
          index,
          oldSchema(index).copy(
            dataType = IntegerType,
            metadata = MappingMetadataConverter.mappingToMetadata(columns(index).mapping))))
    }

    to(IntegerType, ids, Some(updatedSchema)){ _.asCategorical }
  }

  private def useConvertType(
      ids: Set[Int],
      names: Set[String],
      types: Set[ColumnType],
      targetType: ColumnType): DataFrame = {
    val operation = new ConvertType
    operation
      .parameters
      .getColumnSelectorParameter(ConvertType.SelectedColumns)
      .value = Some(MultipleColumnSelection(
      Vector(NameColumnSelection(names), IndexColumnSelection(ids), TypeColumnSelection(types))))

    operation
      .parameters
      .getChoiceParameter(ConvertType.TargetType).value = Some(targetType.toString)

    executeOperation(operation, inputDataFrame)
  }
}
