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

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.scalatest.BeforeAndAfter

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.dataframe.types.SparkConversions
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoriesMapping, MappingMetadataConverter}
import io.deepsense.deeplang.doperations.inout.CsvParameters.ColumnSeparator
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{CassandraTestSupport, DeeplangIntegTestSupport}

class WriteReadDataFrameIntegSpec
  extends DeeplangIntegTestSupport
  with BeforeAndAfter
  with CassandraTestSupport {

  val absoluteWriteReadDataFrameTestPath = absoluteTestsDirPath + "/WriteReadDataFrameTest"

  override def cassandraKeySpaceName: String = "dataframes"
  override def cassandraTableName: String = "dataframe"

  val timestamp = new Timestamp(System.currentTimeMillis())

  val schema: StructType =
    StructType(Seq(
      StructField("boolean", SparkConversions.columnTypeToSparkColumnType(ColumnType.boolean)),
      StructField(
        "categorical",
        SparkConversions.columnTypeToSparkColumnType(ColumnType.categorical),
        metadata =
          MappingMetadataConverter.mappingToMetadata(CategoriesMapping(Seq("A", "B", "C")))),
      StructField("numeric", SparkConversions.columnTypeToSparkColumnType(ColumnType.numeric)),
      StructField("string", SparkConversions.columnTypeToSparkColumnType(ColumnType.string)),
      StructField("timestamp", SparkConversions.columnTypeToSparkColumnType(ColumnType.timestamp))
    ))

  val rows = Seq(
    Row(true, 0, 0.45, "3.14", timestamp),
    Row(false, 1, null, "\"testing...\"", null),
    Row(false, 2, 3.14159, "Hello, world!", timestamp),
    Row(null, null, null, "", null) // in case of CSV, an empty string is the same as null -
                                    // no way around it
  )

  val dataFrame = createDataFrame(rows, schema)

  before {
    fileSystemClient.delete(testsDir)
  }

  "WriteDataFrame and ReadDataFrame" should {
    "write and read CSV file" in {
      val wdf = WriteDataFrame(
        (ColumnSeparator.TAB, None),
        writeHeader = true,
        absoluteWriteReadDataFrameTestPath + "/csv")
      wdf.execute(executionContext)(Vector(dataFrame))

      val rdf = ReadDataFrame(
        absoluteWriteReadDataFrameTestPath + "/csv",
        (ColumnSeparator.TAB, None),
        csvNamesIncluded = true,
        csvShouldConvertToBoolean = true,
        categoricalColumns = Some(MultipleColumnSelection(
          Vector(NameColumnSelection(Set("categorical")))
        ))
      )
      val loadedDataFrame = rdf.execute(executionContext)(Vector()).head.asInstanceOf[DataFrame]

      assertDataFramesEqual(loadedDataFrame, dataFrame, checkRowOrder = false)
    }

    "write and read PARQUET file" in {
      val wdf = WriteDataFrame(
        StorageType.FILE,
        FileFormat.PARQUET,
        absoluteWriteReadDataFrameTestPath + "/parquet")
      wdf.execute(executionContext)(Vector(dataFrame))

      val rdf = ReadDataFrame(
        FileFormat.PARQUET,
        absoluteWriteReadDataFrameTestPath + "/parquet")
      val loadedDataFrame = rdf.execute(executionContext)(Vector()).head.asInstanceOf[DataFrame]

      assertDataFramesEqual(loadedDataFrame, dataFrame, checkRowOrder = false)
    }

    "write and read JSON file" in {
      val convertedSchema = StructType(schema.map {
        case field@StructField("timestamp", _, _, _) =>
          field.copy(dataType = SparkConversions.columnTypeToSparkColumnType(ColumnType.string))
        case field => field
      })

      val timestampFieldIndex = convertedSchema.fieldIndex("timestamp")

      // JSON format does not completely preserve schema.
      // Timestamp columns are converted to string columns by WriteDataFrame operation.
      val convertedRows = rows.map {
        row =>
          val timestampValue = row.get(timestampFieldIndex)
          if (timestampValue != null) {
            Row.fromSeq(row.toSeq.updated(timestampFieldIndex, timestampValue.toString))
          } else {
            row
          }
      }
      val convertedDataFrame = createDataFrame(convertedRows, convertedSchema)

      val wdf = WriteDataFrame(
        StorageType.FILE,
        FileFormat.JSON,
        absoluteWriteReadDataFrameTestPath + "/json")
      wdf.execute(executionContext)(Vector(dataFrame))

      val rdf = ReadDataFrame(
        FileFormat.JSON,
        absoluteWriteReadDataFrameTestPath + "/json",
        categoricalColumns = Some(MultipleColumnSelection(
          Vector(NameColumnSelection(Set("categorical")))
        ))
      )
      val loadedDataFrame = rdf.execute(executionContext)(Vector()).head.asInstanceOf[DataFrame]

      assertDataFramesEqual(loadedDataFrame, convertedDataFrame, checkRowOrder = false)
    }

    "write and read from cassandra" in {
      // Cassandra requires non-null primary key column
      val idField =
        StructField("id", SparkConversions.columnTypeToSparkColumnType(ColumnType.string))
      val convertedSchema = schema.copy(fields = idField +: schema.fields)
      val convertedRows = rows.zipWithIndex.map { case (r, i) => Row(i.toString +: r.toSeq: _*) }
      val dataFrame = createDataFrame(convertedRows, convertedSchema)

      createTable()

      val wdf = WriteDataFrame()
      wdf.storageTypeParameter.value = StorageType.CASSANDRA.toString
      wdf.cassandraTableParameter.value = cassandraTableName
      wdf.cassandraKeyspaceParameter.value = cassandraKeySpaceName
      wdf.execute(executionContext)(Vector(dataFrame))

      val rdf = ReadDataFrame()
      rdf.storageTypeParameter.value = StorageType.CASSANDRA.toString
      rdf.cassandraTableParameter.value = cassandraTableName
      rdf.cassandraKeyspaceParameter.value = cassandraKeySpaceName
      rdf.categoricalColumnsParameter.value =
        MultipleColumnSelection(Vector(NameColumnSelection(Set("categorical"))))
      val loadedDataFrame = rdf.execute(executionContext)(Vector()).head.asInstanceOf[DataFrame]

      assertDataFramesEqual(loadedDataFrame, dataFrame, checkRowOrder = false)
    }
  }

  def createTable(): Unit = {
    session.execute(s"""
      |CREATE TABLE IF NOT EXISTS $cassandraTableName (
      |  boolean BOOLEAN,
      |  categorical TEXT,
      |  numeric DOUBLE,
      |  string TEXT,
      |  timestamp TIMESTAMP,
      |  id TEXT,
      |  PRIMARY KEY(id)
      |)""".stripMargin)
  }
}
