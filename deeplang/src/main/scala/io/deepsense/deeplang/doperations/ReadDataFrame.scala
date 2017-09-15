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
import java.util.UUID

import scala.collection.immutable.ListMap
import scala.reflect.runtime.{universe => ru}

import org.apache.spark.SparkContext
import org.apache.spark.sql.types.{StringType, StructType}
import org.apache.spark.sql.{DataFrame => SparkDataFrame}

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import io.deepsense.deeplang.doperations.inout.{CassandraParameters, JdbcParameters, CsvParameters, CsvReader}
import CsvParameters.ColumnSeparator
import io.deepsense.deeplang.doperations.exceptions.DeepSenseIOException
import io.deepsense.deeplang.parameters.FileFormat.FileFormat
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperation0To1, ExecutionContext, FileSystemClient}

case class ReadDataFrame()
    extends DOperation0To1[DataFrame]
    with ReadDataFrameParameters
    with CategoricalExtraction
    with CsvReader {

  override val id: Id = "c48dd54c-6aef-42df-ad7a-42fc59a09f0e"
  override val name = "Read DataFrame"
  override val parameters = ParametersSchema("data storage type" -> storageTypeParameter)

  override protected def _execute(context: ExecutionContext)(): DataFrame = {
    implicit val ec = context

    val read = StorageType.withName(storageTypeParameter.value) match {
      case StorageType.JDBC =>
        readFromJdbc _ andThen addCategoricalsToSchema
      case StorageType.CASSANDRA =>
        readFromCassandra _ andThen addCategoricalsToSchema
      case StorageType.FILE =>
        prepareFilePath _ andThen readFromFile
    }

    try {
      read(context)
    } catch {
      case e: IOException => throw DeepSenseIOException(e)
    }
  }

  private def readFromJdbc(context: ExecutionContext): SparkDataFrame =
    context.sqlContext
      .read.format("jdbc")
      .option("driver", jdbcDriverClassNameParameter.value)
      .option("url", jdbcUrlParameter.value)
      .option("dbtable", jdbcTableNameParameter.value)
      .load()

  private def readFromCassandra(context: ExecutionContext): SparkDataFrame =
    context.sqlContext
      .read.format("org.apache.spark.sql.cassandra")
      .option("keyspace", cassandraKeyspaceParameter.value)
      .option("table", cassandraTableParameter.value)
      .load()

  private def readFromFile(path: String)(implicit context: ExecutionContext) = {
    val readPipeline = FileFormat.withName(fileFormatParameter.value) match {
      case FileFormat.PARQUET =>
        readFromParquetFile(path) _ andThen context.dataFrameBuilder.buildDataFrame
      case FileFormat.JSON =>
        readFromJsonFile(path) _ andThen addCategoricalsToSchema
      case FileFormat.CSV =>
        readFromCsvFile(path) _ andThen inferAndConvert andThen addCategoricalsToSchema
    }
    readPipeline(context)
  }

  private def readFromParquetFile(path: String)(context: ExecutionContext): SparkDataFrame =
    context.sqlContext.read.parquet(path)

  private def readFromJsonFile(path: String)(context: ExecutionContext): SparkDataFrame =
    context.sqlContext.read.json(path)

  private def readFromCsvFile(path: String)(context: ExecutionContext): SparkDataFrame =
    context.sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", if (csvNamesIncludedParameter.value) "true" else "false")
      .option("delimiter", determineColumnSeparator().toString)
      .load(path)

  private def prepareFilePath(context: ExecutionContext) = {
    val path = sourceFileParameter.value
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
    val prefix = if (sparkContext.isLocal) {
      "file://"
    } else {
      "hdfs://"
    }

    val fileName = s"$prefix/tmp/deepsense/download/${UUID.randomUUID().toString}"
    val content = scala.io.Source.fromURL(url).mkString
    val lines = content.split("\n")
    sparkContext.parallelize(lines).saveAsTextFile(fileName)
    fileName
  }

  private def addCategoricalsToSchema(
      sparkDataFrame: SparkDataFrame)(
      implicit context: ExecutionContext): DataFrame = {

    val categoricals =
      getCategoricalColumns(sparkDataFrame.schema, categoricalColumnsParameter.value)

    val schemaWithCategoricals = StructType(
      sparkDataFrame.schema.zipWithIndex.map { case (column, index) =>
        if (categoricals.contain(index)) {
          column.copy(dataType = StringType)
        } else {
          column
        }
      })

    context.dataFrameBuilder
      .buildDataFrame(schemaWithCategoricals, sparkDataFrame.rdd, categoricals.names)
  }

  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

trait ReadDataFrameParameters
    extends CsvParameters
    with JdbcParameters
    with CassandraParameters {

  val csvShouldConvertToBooleanParameter = BooleanParameter(
    "Should columns containing only 0 and 1 be converted to Boolean?",
    default = Some(false))

  val categoricalColumnsParameter = ColumnSelectorParameter(
    "Categorical columns in the input file",
    portIndex = 0,
    default = Some(MultipleColumnSelection.emptySelection))

  lazy val fileFormatParameter = ChoiceParameter(
    "Format of the input file",
    default = Some(FileFormat.CSV.toString),
    options = ListMap(
      FileFormat.CSV.toString -> ParametersSchema(
        "separator" -> csvColumnSeparatorParameter,
        "names included" -> csvNamesIncludedParameter,
        "convert to boolean" -> csvShouldConvertToBooleanParameter,
        "categorical columns" -> categoricalColumnsParameter),
      FileFormat.PARQUET.toString -> ParametersSchema(),
      FileFormat.JSON.toString -> ParametersSchema(
        "categorical columns" -> categoricalColumnsParameter)
    ))

  lazy val storageTypeParameter = ChoiceParameter(
    "Storage type",
    default = Some(StorageType.FILE.toString),
    options = ListMap(
      StorageType.FILE.toString -> ParametersSchema(
        "source" -> sourceFileParameter,
        "format" -> fileFormatParameter),
      StorageType.JDBC.toString -> ParametersSchema(
        "url" -> jdbcUrlParameter,
        "driver" -> jdbcDriverClassNameParameter,
        "table" -> jdbcTableNameParameter,
        "categorical columns" -> categoricalColumnsParameter),
      StorageType.CASSANDRA.toString -> ParametersSchema(
        "keyspace" -> cassandraKeyspaceParameter,
        "table" -> cassandraTableParameter,
        "categorical columns" -> categoricalColumnsParameter)
    ))

  val sourceFileParameter = StringParameter(
    "Path to the DataFrame file",
    default = None,
    validator = new AcceptAllRegexValidator())
}

trait CategoricalExtraction {

  case class Categoricals(indices: Set[Int], names: Seq[String]) {
    def contain(idx: Int): Boolean = indices.contains(idx)
  }

  /**
    * Returns indices and names of columns selected to be categorized
    * wrapped in a convenient case class.
    */
  protected def getCategoricalColumns(
      schema: StructType,
      categoricalColumnsSelection: MultipleColumnSelection): Categoricals = {
    val columnNames: Seq[String] = schema.fields.map(_.name)
    val categoricalColumnNames =
      DataFrameColumnsGetter.getColumnNames(schema, categoricalColumnsSelection)
    val categoricalColumnNamesSet = categoricalColumnNames.toSet
    val categoricalColumnIndices = (for {
      (name, index) <- columnNames.zipWithIndex if categoricalColumnNamesSet.contains(name)
    } yield index).toSet

    Categoricals(categoricalColumnIndices, categoricalColumnNames)
  }
}

object ReadDataFrame {
  val recordDelimiterSettingName = "textinputformat.record.delimiter"

  def apply(
      fileFormat: FileFormat,
      filePath: String,
      categoricalColumns: Option[MultipleColumnSelection] = None): ReadDataFrame = {
    val operation = new ReadDataFrame()
    operation.storageTypeParameter.value = StorageType.FILE.toString
    operation.fileFormatParameter.value = fileFormat.toString
    operation.sourceFileParameter.value = filePath
    operation.categoricalColumnsParameter.value = categoricalColumns
    operation
  }

  def apply(
      filePath: String,
      csvColumnSeparator: (ColumnSeparator.ColumnSeparator, Option[String]),
      csvNamesIncluded: Boolean,
      csvShouldConvertToBoolean: Boolean,
      categoricalColumns: Option[MultipleColumnSelection]): ReadDataFrame = {

    val operation = new ReadDataFrame()

    operation.storageTypeParameter.value = StorageType.FILE.toString
    operation.fileFormatParameter.value = FileFormat.CSV.toString
    operation.sourceFileParameter.value = filePath
    operation.csvColumnSeparatorParameter.value = csvColumnSeparator._1.toString
    operation.csvCustomColumnSeparatorParameter.value = csvColumnSeparator._2
    operation.csvNamesIncludedParameter.value = csvNamesIncluded
    operation.csvShouldConvertToBooleanParameter.value = csvShouldConvertToBoolean
    operation.categoricalColumnsParameter.value = categoricalColumns
    operation
  }
}
