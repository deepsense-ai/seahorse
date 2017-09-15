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

import java.io.{IOException, StringWriter}
import java.sql.Timestamp
import java.util.Properties

import scala.collection.immutable.ListMap
import scala.reflect.runtime.{universe => ru}

import au.com.bytecode.opencsv.CSVWriter
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.spark.SparkException
import org.apache.spark.sql.Row

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoricalMapper, CategoricalMetadata}
import io.deepsense.deeplang.doperations.CsvParameters.ColumnSeparator
import io.deepsense.deeplang.doperations.exceptions.{DeepSenseIOException, WriteFileException}
import io.deepsense.deeplang.parameters.FileFormat.FileFormat
import io.deepsense.deeplang.parameters.StorageType.StorageType
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperation1To0, ExecutionContext, FileSystemClient}

case class WriteDataFrame()
  extends DOperation1To0[DataFrame]
  with CsvParameters
  with JdbcParameters
  with CassandraParameters {

  override val id: Id = "9e460036-95cc-42c5-ba64-5bc767a40e4e"

  override val name: String = "Write DataFrame"

  val fileFormatParameter = ChoiceParameter(
    description = "Format of the output file",
    default = Some(FileFormat.CSV.toString),
    options = ListMap(
      FileFormat.CSV.toString -> ParametersSchema(
        "column separator" -> csvColumnSeparatorParameter,
        "write header" -> csvNamesIncludedParameter),
      FileFormat.PARQUET.toString -> ParametersSchema(),
      FileFormat.JSON.toString -> ParametersSchema()
  ))

  lazy val storageTypeParameter = ChoiceParameter(
    "Storage type",
    default = Some(StorageType.FILE.toString),
    options = ListMap(
      StorageType.FILE.toString -> ParametersSchema(
        "output file" -> outputFileParameter,
        "format" -> fileFormatParameter),
      StorageType.JDBC.toString -> ParametersSchema(
        "url" -> jdbcUrlParameter,
        "driver" -> jdbcDriverClassNameParameter,
        "table" -> jdbcTableNameParameter),
      StorageType.CASSANDRA.toString -> ParametersSchema(
        "keyspace" -> cassandraKeyspaceParameter,
        "table" -> cassandraTableParameter)
      )
    )

  val outputFileParameter = StringParameter(
    description = "Output file path",
    default = None,
    validator = new AcceptAllRegexValidator())

  override val parameters = ParametersSchema("data storage type" -> storageTypeParameter)

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): Unit = {
    try {
      StorageType.withName(storageTypeParameter.value) match {
        case StorageType.JDBC =>
          writeToJdbc(context, dataFrame)
        case StorageType.CASSANDRA =>
          writeToCassandra(context, dataFrame)
        case StorageType.FILE =>
          val path = FileSystemClient
            .replaceLeadingTildeWithHomeDirectory(outputFileParameter.value)
          try {
            writeToFile(context, dataFrame: DataFrame, path)
          } catch {
            case e: SparkException =>
              logger
                .error(s"WriteDataFrame error: Spark problem. Could not write file to: $path", e)
              throw WriteFileException(path, e)
          }
      }
    } catch {
      case e: IOException =>
        logger.error(s"WriteDataFrame error. Could not write file to designated storage", e)
        throw DeepSenseIOException(e)
    }
  }

  def writeToJdbc(context: ExecutionContext, dataFrame: DataFrame): Unit = {
    val properties = new Properties()
    properties.setProperty("driver", jdbcDriverClassNameParameter.value)
    val mapper = CategoricalMapper(dataFrame, context.dataFrameBuilder)
    val uncategorizedDataFrame = mapper.uncategorized(dataFrame)
    uncategorizedDataFrame.sparkDataFrame
      .write.jdbc(jdbcUrlParameter.value, jdbcTableNameParameter.value, properties)
  }

  def writeToCassandra(context: ExecutionContext, dataFrame: DataFrame): Unit = {
    val mapper = CategoricalMapper(dataFrame, context.dataFrameBuilder)
    val uncategorizedDataFrame = mapper.uncategorized(dataFrame)
    uncategorizedDataFrame.sparkDataFrame
      .write.format("org.apache.spark.sql.cassandra")
      .options(Map(
        "keyspace" -> cassandraKeyspaceParameter.value,
        "table" -> cassandraTableParameter.value
      )).save()
  }

  def writeToFile(context: ExecutionContext, dataFrame: DataFrame, path: String): Unit = {
    FileFormat.withName(fileFormatParameter.value) match {
      case FileFormat.CSV =>
        val categoricalMetadata = CategoricalMetadata(dataFrame.sparkDataFrame)
        val csv = dataFrame.sparkDataFrame.rdd
          .map(rowToStringArray(categoricalMetadata) andThen convertToCsv)
        val result = if (csvNamesIncludedParameter.value) {
          context.sparkContext.parallelize(Seq(
            convertToCsv(dataFrame.sparkDataFrame.schema.fieldNames)
          )).union(csv)
        } else {
          csv
        }
        result.saveAsTextFile(path)
      case FileFormat.PARQUET =>
        // TODO: DS-1480 Writing DF in parquet format when column names contain forbidden chars
        dataFrame.sparkDataFrame.write.parquet(path)
      case FileFormat.JSON =>
        val mapper = CategoricalMapper(dataFrame, context.dataFrameBuilder)
        val uncategorizedDataFrame = mapper.uncategorized(dataFrame)
        uncategorizedDataFrame.sparkDataFrame.write.json(path)
    }
  }

  private def contains(o: Option[String], value: String): Boolean = {
    o match {
      case None => false
      case Some(x) => x == value
    }
  }

  private def rowToStringArray(categoricalMetadata: CategoricalMetadata) = (row: Row) => {
    val zippedWithIndex = row.toSeq.zipWithIndex

    val withCategoricals = zippedWithIndex.map { case (value, index) =>
      if (value != null && categoricalMetadata.isCategorical(index)) {
        categoricalMetadata.mapping(index).idToValue(value.asInstanceOf[Int])
      } else {
        value
      }
    }

    withCategoricals.toArray.map {
      case null => ""
      case true => "1"
      case false => "0"
      case s: String => StringEscapeUtils.escapeCsv(s)
      case t: Timestamp => DateTimeConverter.toString(DateTimeConverter.fromMillis(t.getTime))
      case x => x.toString
    }
  }

  val convertToCsv = (data: Array[String]) => {
    import scala.collection.JavaConversions._
    val buf = new StringWriter
    val writer = new CSVWriter(buf, determineColumnSeparator(),
      CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER, "")
    writer.writeAll(List(data))
    buf.toString
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

object WriteDataFrame {
  def apply(
      storageType: StorageType,
      fileFormat: FileFormat,
      path: String): WriteDataFrame = {
    val writeDataFrame = WriteDataFrame()
    writeDataFrame.storageTypeParameter.value = Some(storageType.toString)
    writeDataFrame.fileFormatParameter.value = Some(fileFormat.toString)
    writeDataFrame.outputFileParameter.value = Some(path)
    writeDataFrame
  }

  /**
   * WriteDataFrame: CSV file type.
   */
  def apply(
      columnSeparator: (ColumnSeparator.ColumnSeparator, Option[String]),
      writeHeader: Boolean,
      path: String): WriteDataFrame = {
    val (separator, customSeparator) = columnSeparator
    val writeDataFrame = WriteDataFrame()
    writeDataFrame.storageTypeParameter.value = Some(StorageType.FILE.toString)
    writeDataFrame.fileFormatParameter.value = FileFormat.CSV.toString
    writeDataFrame.csvColumnSeparatorParameter.value = separator.toString
    writeDataFrame.csvCustomColumnSeparatorParameter.value = customSeparator
    writeDataFrame.csvNamesIncludedParameter.value = writeHeader
    writeDataFrame.outputFileParameter.value = path
    writeDataFrame
  }
}
