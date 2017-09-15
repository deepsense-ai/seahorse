/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.doperations

import java.sql.Timestamp

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.joda.time.DateTime

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.NameSingleColumnSelection
import io.deepsense.deeplang.{DOperable, DOperation, ExecutionContext, SparkIntegTestSupport}

class TimestampDecomposerIntegSpec extends SparkIntegTestSupport {

  private[this] val timestampColumnName = "timestampColumn"
  private[this] val t1 = new DateTime(2015, 3, 30, 15, 25)

  "TimestampDecomposer" should "decompose timestamp column" in {
    val schema = createSchema
    val t2 = t1.plusDays(1)
    val data = createData(
      List(Some(new Timestamp(t1.getMillis)), Some(new Timestamp(t2.getMillis)))
    )
    val expectedData: Seq[Row] = Seq(
      createDecomposedTimestampRow(schema, 0, t1), createDecomposedTimestampRow(schema, 1, t2)
    )

    shouldDecomposeTimestamp(schema, data, expectedData, 0)
  }

  it should "decompose null timestamp column" in {
    val schema = createSchema
    val data = createData(List(Some(new Timestamp(t1.getMillis)), None))
    val expectedData: Seq[Row] = Seq(
      createDecomposedTimestampRow(schema, 0, t1),
      new GenericRowWithSchema(Array(1, null, null, null, null, null, null, null),
        resultSchema(schema, timestampColumnName, 0))
    )

    shouldDecomposeTimestamp(schema, data, expectedData, 0)
  }

  it should "correctly generate column names" in {
    val schema = StructType(List(
      StructField(timestampColumnName, TimestampType),
      StructField(timestampColumnName + "_seconds", IntegerType)
    ))
    val data = sparkContext.parallelize(List(
      Row(new Timestamp(t1.getMillis), 123)
    ))
    val modifiedSchema = resultSchema(schema, timestampColumnName, 1)
    val expectedData: Seq[Row] = Seq(
      new GenericRowWithSchema(
        Array(new Timestamp(t1.getMillis), 123, t1.getYear, t1.getMonthOfYear, t1.getDayOfMonth,
          t1.getHourOfDay, t1.getMinuteOfHour, t1.getSecondOfMinute), modifiedSchema)
    )

    shouldDecomposeTimestamp(schema, data, expectedData, 1)
  }

  private def shouldDecomposeTimestamp(
      schema: StructType, data: RDD[Row],
      expectedData: Seq[Row],
      generatedColumnsLevel: Int): Unit = {
    val context = executionContext
    val operation: TimestampDecomposer = operationWithParamsSet
    val dataFrame = context.dataFrameBuilder.buildDataFrame(schema, data)

    val resultDataFrame: DataFrame = executeOperation(context, operation)(dataFrame)

    val expectedSchema: StructType =
      resultSchema(schema, timestampColumnName, generatedColumnsLevel)
    assert(expectedSchema == resultDataFrame.sparkDataFrame.schema)
    assert(expectedData.size == resultDataFrame.sparkDataFrame.count())
    val zipped = expectedData zip resultDataFrame.sparkDataFrame.rdd.collect()
    assert(zipped.forall(p => p._1 ==  p._2))
  }

  private def executeOperation(context: ExecutionContext, operation: DOperation)
    (dataFrame: DataFrame): DataFrame = {
    val operationResult = operation.execute(context)(Vector[DOperable](dataFrame))
    val resultDataFrame = operationResult.head.asInstanceOf[DataFrame]
    resultDataFrame
  }

  private def createDecomposedTimestampRow(schema: StructType, id: Int, t: DateTime): Row = {
    new GenericRowWithSchema(Array(id, new Timestamp(t.getMillis), t.getYear, t.getMonthOfYear,
      t.getDayOfMonth, t.getHourOfDay, t.getMinuteOfHour, t.getSecondOfMinute), schema)
  }

  private def resultSchema(
    originalSchema: StructType, timestampColumn: String, level: Int): StructType = {
    val levelSuffix = if (level > 0) "_" + level else ""

    StructType(originalSchema.fields ++ Array(
      StructField(timestampColumn + "_year" + levelSuffix, IntegerType, nullable = true),
      StructField(timestampColumn + "_month" + levelSuffix, IntegerType, nullable = true),
      StructField(timestampColumn + "_day" + levelSuffix, IntegerType, nullable = true),
      StructField(timestampColumn + "_hour" + levelSuffix, IntegerType, nullable = true),
      StructField(timestampColumn + "_minutes" + levelSuffix, IntegerType, nullable = true),
      StructField(timestampColumn + "_seconds" + levelSuffix, IntegerType, nullable = true)
    ))
  }

  private def createData(timestamps: Seq[Option[Timestamp]]): RDD[Row] = {
    sparkContext.parallelize(timestamps.zipWithIndex.map(p => Row(p._2, p._1.orNull, null)))
  }

  private def createSchema: StructType = {
    StructType(List(
      StructField("id", IntegerType, nullable = false),
      StructField(timestampColumnName, TimestampType, nullable = true)
    ))
  }

  private def operationWithParamsSet: TimestampDecomposer = {
    val operation = new TimestampDecomposer
    val columnParam = operation.parameters.getSingleColumnSelectorParameter("timestampColumn")
    columnParam.value = Some(NameSingleColumnSelection("timestampColumn"))
    val timeUnitsParam = operation.parameters.getMultipleChoiceParameter("parts")
    timeUnitsParam.value = Some(Seq("year", "month", "day", "hour", "minutes", "seconds"))
    operation
  }
}
