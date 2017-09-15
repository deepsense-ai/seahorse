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
import java.nio.file.Files

import scala.reflect.io.File
import scala.reflect.runtime.{universe => ru}

import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame => SparkDataFrame}

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

  override val name: String = "Read DataFrame"
  override val id: Id = "c48dd54c-6aef-42df-ad7a-42fc59a09f0e"
  override val description: String =
    "Reads a DataFrame from a file or database"

  val params = declareParams(storageType)
  setDefault(storageType, InputStorageTypeChoice.File())

  override protected def _execute(context: ExecutionContext)(): DataFrame = {
    implicit val ec = context

    val read: (ExecutionContext => DataFrame) = getStorageType match {
      case (jdbcChoice: InputStorageTypeChoice.Jdbc) =>
        readFromJdbc(jdbcChoice) _ andThen DataFrame.fromSparkDataFrame
      case (cassandraChoice: InputStorageTypeChoice.Cassandra) =>
        readFromCassandra(cassandraChoice) _ andThen DataFrame.fromSparkDataFrame
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

  private def readFromCassandra
    (cassandraChoice: InputStorageTypeChoice.Cassandra)
    (context: ExecutionContext): SparkDataFrame = {

    context.sqlContext
      .read.format("org.apache.spark.sql.cassandra")
      .option("keyspace", cassandraChoice.getCassandraKeyspace)
      .option("table", cassandraChoice.getCassandraTable)
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
      downloadFile(path, context.sparkContext)
    } else {
      FileSystemClient.replaceLeadingTildeWithHomeDirectory(path)
    }
  }

  private def isUrlSource(path: String): Boolean = {
    val isHttp = path.startsWith("http://") || path.startsWith("https://")
    val isFtp = path.startsWith("ftp://")
    isHttp || isFtp
  }

  private def downloadFile(url: String, sparkContext: SparkContext): String = {
    // TODO DS-2355 this saves the file in local filesystem, which will not work in non-local modes
    val content = scala.io.Source.fromURL(url).mkString
    val file = Files.createTempFile("download", ".csv")
    File(file.toString).writeAll(content)
    s"file:///$file"
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
}
