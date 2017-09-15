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

import java.io.IOException
import java.sql.Timestamp
import java.util.Properties

import scala.collection.immutable.ListMap
import scala.reflect.runtime.{universe => ru}

import org.apache.spark.SparkException
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoricalMapper
import io.deepsense.deeplang.doperations.inout.{CassandraParameters, JdbcParameters, CsvParameters}
import CsvParameters.ColumnSeparator
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
          val path =
            FileSystemClient.replaceLeadingTildeWithHomeDirectory(outputFileParameter.value)

          try {
            writeToFile(context, dataFrame: DataFrame, path)
          } catch {
            case e: SparkException =>
              logger.error(s"WriteDataFrame error: Spark problem. Unable to write file to $path", e)
              throw WriteFileException(path, e)
          }
      }
    } catch {
      case e: IOException =>
        logger.error(s"WriteDataFrame error. Could not write file to designated storage", e)
        throw DeepSenseIOException(e)
    }
  }

  private def writeToJdbc(context: ExecutionContext, dataFrame: DataFrame): Unit = {
    val properties = new Properties()
    properties.setProperty("driver", jdbcDriverClassNameParameter.value)

    CategoricalMapper(dataFrame, context.dataFrameBuilder)
      .uncategorized(dataFrame)
      .sparkDataFrame
      .write.jdbc(jdbcUrlParameter.value, jdbcTableNameParameter.value, properties)
  }

  private def writeToCassandra(context: ExecutionContext, dataFrame: DataFrame): Unit = {
    CategoricalMapper(dataFrame, context.dataFrameBuilder)
      .uncategorized(dataFrame)
      .sparkDataFrame
      .write.format("org.apache.spark.sql.cassandra")
      .option("keyspace", cassandraKeyspaceParameter.value)
      .option("table", cassandraTableParameter.value)
      .save()
  }

  private def writeToFile(context: ExecutionContext, dataFrame: DataFrame, path: String): Unit = {
    FileFormat.withName(fileFormatParameter.value) match {
      case FileFormat.CSV =>
        prepareDataFrameForCsv(context, dataFrame)
          .sparkDataFrame
          .write.format("com.databricks.spark.csv")
          .option("header", if (csvNamesIncludedParameter.value) "true" else "false")
          .option("delimiter", determineColumnSeparator().toString)
          .save(path)

      case FileFormat.PARQUET =>
        // TODO: DS-1480 Writing DF in parquet format when column names contain forbidden chars
        dataFrame.sparkDataFrame.write.parquet(path)

      case FileFormat.JSON =>
        CategoricalMapper(dataFrame, context.dataFrameBuilder)
          .uncategorized(dataFrame)
          .sparkDataFrame
          .write.json(path)
    }
  }

  private def prepareDataFrameForCsv(context: ExecutionContext, dataFrame: DataFrame): DataFrame = {
    val originalSchema = dataFrame.sparkDataFrame.schema

    def stringifySelectedCells(row: Row): Row =
      Row.fromSeq(
        row.toSeq.zipWithIndex map { case (value, index) =>
          (value, originalSchema(index).dataType) match {
            case (null, _) => ""
            case (_, BooleanType) =>
              if (value.asInstanceOf[Boolean]) "1" else "0"
            case (_, TimestampType) =>
              DateTimeConverter.toString(
                DateTimeConverter.fromMillis(value.asInstanceOf[Timestamp].getTime))
            case _ => value
          }
        })

    def stringifySelectedTypes(schema: StructType): StructType = {
      StructType(
        schema.map {
          case field@StructField(_, BooleanType, _, _) => field.copy(dataType = StringType)
          case field@StructField(_, TimestampType, _, _) => field.copy(dataType = StringType)
          case field => field
        }
      )
    }

    val uncategorized =
      CategoricalMapper(dataFrame, context.dataFrameBuilder)
        .uncategorized(dataFrame)

    context.dataFrameBuilder.buildDataFrame(
      stringifySelectedTypes(uncategorized.sparkDataFrame.schema),
      uncategorized.sparkDataFrame.map(stringifySelectedCells))
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
