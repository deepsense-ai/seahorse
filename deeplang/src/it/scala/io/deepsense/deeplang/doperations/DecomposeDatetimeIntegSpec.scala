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

package io.deepsense.deeplang.doperations

import java.sql.Timestamp

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.joda.time.DateTime

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.{WrongColumnTypeException, ColumnDoesNotExistException}
import io.deepsense.deeplang.parameters.{IndexSingleColumnSelection, NameSingleColumnSelection, SingleColumnSelection}
import io.deepsense.deeplang.{DOperable, DOperation, DeeplangIntegTestSupport, ExecutionContext}

class DecomposeDatetimeIntegSpec extends DeeplangIntegTestSupport {

  private[this] val timestampColumnName = "timestampColumn"
  private[this] val t1 = new DateTime(2015, 3, 30, 15, 25)

  "TimestampDecomposer" should {
    "decompose timestamp column without prefix" in {
      val schema = createSchema
      val t2 = t1.plusDays(1)
      val data = createData(
        List(Some(new Timestamp(t1.getMillis)), Some(new Timestamp(t2.getMillis)))
      )
      val expectedData: Seq[Row] = Seq(
        createDecomposedTimestampRow(schema, 0, t1), createDecomposedTimestampRow(schema, 1, t2)
      )
      shouldDecomposeTimestamp(schema, data, expectedData, prefix = None)
    }

    "decompose timestamp column with prefix" in {
      val schema = createSchema
      val prefix = Some(timestampColumnName + "_")
      val t2 = t1.plusDays(1)
      val data = createData(
        List(Some(new Timestamp(t1.getMillis)), Some(new Timestamp(t2.getMillis)))
      )
      val expectedData: Seq[Row] = Seq(
        createDecomposedTimestampRow(schema, 0, t1), createDecomposedTimestampRow(schema, 1, t2)
      )
      shouldDecomposeTimestamp(schema, data, expectedData, prefix)
    }
  }

  it should {
    "decompose null timestamp column" in {
      val schema = createSchema
      val prefix = None
      val data = createData(List(Some(new Timestamp(t1.getMillis)), prefix))
      val expectedData: Seq[Row] = Seq(
        createDecomposedTimestampRow(schema, 0, t1),
        new GenericRowWithSchema(Array(1, null, null, null, null, null, null, null),
          resultSchema(schema, prefix))
      )

      shouldDecomposeTimestamp(schema, data, expectedData, prefix = None)
    }
  }

  it should {
    "throw an exception" when {
      "column selected by name does not exist" in {
        intercept[ColumnDoesNotExistException] {
          val operation = DecomposeDatetime(
            NameSingleColumnSelection("nonExistsingColumnName"), Seq("year"), prefix = None)
          val dataFrame = createDataFrame(
            Seq.empty, StructType(List(StructField("id", DoubleType))))
          executeOperation(executionContext, operation)(dataFrame)
        }
      }
      "column selected by index does not exist" in {
        intercept[ColumnDoesNotExistException] {
          val operation = DecomposeDatetime(
            IndexSingleColumnSelection(1), Seq("year"), prefix = None)
          val dataFrame = createDataFrame(
            Seq.empty, StructType(List(StructField("id", DoubleType))))
          executeOperation(executionContext, operation)(dataFrame)
        }
      }
      "selected column is not timestamp" in {
        intercept[WrongColumnTypeException] {
          val operation = DecomposeDatetime(
            IndexSingleColumnSelection(0), Seq("year"), prefix = None)
          val dataFrame = createDataFrame(
            Seq.empty, StructType(List(StructField("id", DoubleType))))
          executeOperation(executionContext, operation)(dataFrame)
        }
      }
    }
  }
  private def shouldDecomposeTimestamp(
      schema: StructType, data: RDD[Row],
      expectedData: Seq[Row],
      prefix: Option[String]): Unit = {
    val context = executionContext
    val operation: DecomposeDatetime = operationWithParamsSet(prefix)
    val dataFrame = context.dataFrameBuilder.buildDataFrame(schema, data)

    val resultDataFrame: DataFrame = executeOperation(context, operation)(dataFrame)

    val expectedSchema: StructType =
      resultSchema(schema, prefix)
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
    originalSchema: StructType, prefixParamValue: Option[String]): StructType = {

    val prefix = prefixParamValue.getOrElse("")

    StructType(originalSchema.fields ++ Array(
      StructField(prefix + "year", DoubleType),
      StructField(prefix + "month", DoubleType),
      StructField(prefix + "day", DoubleType),
      StructField(prefix + "hour", DoubleType),
      StructField(prefix + "minutes", DoubleType),
      StructField(prefix + "seconds", DoubleType)
    ))
  }

  private def createData(timestamps: Seq[Option[Timestamp]]): RDD[Row] = {
    sparkContext.parallelize(timestamps.zipWithIndex.map(p => Row(p._2, p._1.orNull)))
  }

  private def createSchema: StructType = {
    StructType(List(
      StructField("id", LongType),
      StructField(timestampColumnName, TimestampType)
    ))
  }

  private def operationWithParamsSet(prefixParam: Option[String]): DecomposeDatetime = {
    DecomposeDatetime(
      NameSingleColumnSelection(timestampColumnName),
      Seq("year", "month", "day", "hour", "minutes", "seconds"),
      prefixParam)
  }
}
