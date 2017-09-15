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

import java.io._
import java.nio.file.{Files, Paths}
import java.util.UUID

import scala.reflect.runtime.{universe => ru}

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame => SparkDataFrame}

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.ReadDataFrame._
import io.deepsense.deeplang.doperations.exceptions.DeepSenseIOException
import io.deepsense.deeplang.doperations.inout._
import io.deepsense.deeplang.params.Params
import io.deepsense.deeplang.params.choice.ChoiceParam
import io.deepsense.deeplang.{DOperation0To1, ExecutionContext, FileSystemClient}

case class ReadDataFrame()
    extends DOperation0To1[DataFrame]
    with ReadDataFrameParameters
    with CsvReader
    with Params {

  override val id: Id = "c48dd54c-6aef-42df-ad7a-42fc59a09f0e"
  override val name: String = "Read DataFrame"
  override val description: String =
    "Reads a DataFrame from a file or database"

  override val since: Version = Version(0, 4, 0)

  val params = declareParams(storageType)
  setDefault(storageType, InputStorageTypeChoice.File())

  override protected def _execute(context: ExecutionContext)(): DataFrame = {
    implicit val ec = context

    val read: (ExecutionContext => DataFrame) = getStorageType match {
      case (jdbcChoice: InputStorageTypeChoice.Jdbc) =>
        readFromJdbc(jdbcChoice) _ andThen DataFrame.fromSparkDataFrame
      case (fileChoice: InputStorageTypeChoice.File) =>
        prepareFilePath(fileChoice) _ andThen readFromFile(fileChoice.getFileFormat)
    }

    try {
      read(context)
    } catch {
      case e: IOException => throw DeepSenseIOException(e)
    }
  }

  private def readFromJdbc
    (jdbcChoice: InputStorageTypeChoice.Jdbc)
    (context: ExecutionContext): SparkDataFrame = {

    context.sqlContext
      .read.format("jdbc")
      .option("driver", jdbcChoice.getJdbcDriverClassName)
      .option("url", jdbcChoice.getJdbcUrl)
      .option("dbtable", jdbcChoice.getJdbcTableName)
      .load()
  }

  private def readFromFile
      (fileFormat: InputFileFormatChoice)
      (path: String)
      (implicit context: ExecutionContext): DataFrame = {
    val readPipeline: (ExecutionContext => DataFrame) = fileFormat match {
      case (parquetSource: InputFileFormatChoice.Parquet) =>
        readFromParquetFile(path) _ andThen DataFrame.fromSparkDataFrame
      case (jsonChoice: InputFileFormatChoice.Json) =>
        readFromJsonFile(path) _ andThen DataFrame.fromSparkDataFrame
      case (csvChoice: InputFileFormatChoice.Csv) =>
        readFromCsvFile(path)(csvChoice) _ andThen
          inferAndConvert(csvChoice) andThen
          DataFrame.fromSparkDataFrame
    }
    readPipeline(context)
  }

  private def readFromParquetFile(path: String)(context: ExecutionContext): SparkDataFrame =
    context.sqlContext.read.parquet(path)

  private def readFromJsonFile(path: String)(context: ExecutionContext): SparkDataFrame =
    context.sqlContext.read.json(path)

  private def readFromCsvFile
    (path: String)
    (csvChoice: InputFileFormatChoice.Csv)
    (context: ExecutionContext): SparkDataFrame = {
    context.sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", if (csvChoice.getCsvNamesIncluded) "true" else "false")
      .option("delimiter", csvChoice.determineColumnSeparator().toString)
      .load(path)
  }

  private def prepareFilePath
    (fileChoice: InputStorageTypeChoice.File)
    (context: ExecutionContext): String = {

    val path = fileChoice.getSourceFile
    if (isUrlSource(path)) {
      downloadFile(path, context.tempPath, context.sparkContext)
    } else {
      FileSystemClient.replaceLeadingTildeWithHomeDirectory(path)
    }
  }

  private def isUrlSource(path: String): Boolean = {
    val isHttp = path.startsWith("http://") || path.startsWith("https://")
    val isFtp = path.startsWith("ftp://")
    isHttp || isFtp
  }

  private def downloadFile(
      url: String, tempPath: String, sparkContext: SparkContext): String = {
    if (tempPath.startsWith("hdfs://")) {
      downloadFileToHdfs(url, tempPath, sparkContext)
    } else {
      downloadFileToLocal(url, tempPath, sparkContext)
    }
  }

  private def downloadFileToHdfs(
      url: String, tempPath: String, sparkContext: SparkContext): String = {
    val content = scala.io.Source.fromURL(url).getLines()
    val hdfsPath = s"$tempPath/${UUID.randomUUID()}"

    val configuration = new Configuration()
    val hdfs = FileSystem.get(configuration)
    val file = new Path(hdfsPath)
    val hdfsStream = hdfs.create(file)
    val writer = new BufferedWriter(new OutputStreamWriter(hdfsStream))
    try {
      content.foreach {s =>
        writer.write(s)
        writer.newLine()
      }
    } finally {
      safeClose(writer)
      hdfs.close()
    }

    hdfsPath
  }

  private def downloadFileToLocal(
      url: String, tempPath: String, sparkContext: SparkContext): String = {
    val outFilePath = Files.createTempFile(
      Files.createDirectories(Paths.get(tempPath)), "download", ".csv")
    // content is a stream. Do not invoke stuff like .toList() on it.
    val content = scala.io.Source.fromURL(url).getLines()
    val writer: BufferedWriter =
      new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFilePath.toFile)))
    try {
      content.foreach {s =>
        writer.write(s)
        writer.newLine()
      }
    } finally {
      safeClose(writer)
    }
    s"file:///$outFilePath"
  }

  private def safeClose(bufferedWriter: BufferedWriter): Unit = {
    try {
      bufferedWriter.flush()
      bufferedWriter.close()
    } catch {
      case e: IOException => throw new DeepSenseIOException(e)
    }
  }

  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

object ReadDataFrame {
  val recordDelimiterSettingName = "textinputformat.record.delimiter"

  trait ReadDataFrameParameters {
    this: Params =>

    val storageType = ChoiceParam[InputStorageTypeChoice](
      name = "data storage type",
      description = "Storage type.")

    def getStorageType: InputStorageTypeChoice = $(storageType)
    def setStorageType(value: InputStorageTypeChoice): this.type = set(storageType, value)
  }

  def apply(
      fileName: String,
      csvColumnSeparator: CsvParameters.ColumnSeparatorChoice,
      csvNamesIncluded: Boolean,
      csvConvertToBoolean: Boolean) : ReadDataFrame = {
    new ReadDataFrame()
      .setStorageType(
        InputStorageTypeChoice.File()
          .setSourceFile(fileName)
          .setFileFormat(
            InputFileFormatChoice.Csv()
              .setCsvColumnSeparator(csvColumnSeparator)
              .setCsvNamesIncluded(csvNamesIncluded)
              .setShouldConvertToBoolean(csvConvertToBoolean)))
  }
}
