/**
 * Copyright 2016, deepsense.io
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
import java.net.UnknownHostException
import java.util.UUID

import scala.reflect.runtime.{universe => ru}

import org.apache.spark.sql.{DataFrame => SparkDataFrame}

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.documentation.OperationDocumentation
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.ReadDataFrame.ReadDataFrameParameters
import io.deepsense.deeplang.doperations.exceptions.{DeepSenseIOException, DeepSenseUnknownHostException}
import io.deepsense.deeplang.doperations.inout._
import io.deepsense.deeplang.doperations.readwritedataframe.filestorage.DataFrameFromFileReader
import io.deepsense.deeplang.doperations.readwritedataframe.googlestorage.DataFrameFromGoogleSheetReader
import io.deepsense.deeplang.doperations.readwritedataframe.validators.{FilePathHasValidFileScheme, ParquetSupportedOnClusterOnly}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.choice.ChoiceParam
import io.deepsense.deeplang.params.{Param, Params}
import io.deepsense.deeplang.{DKnowledge, DOperation0To1, ExecutionContext}

// TODO Remake this case class into class
case class ReadDataFrame()
    extends DOperation0To1[DataFrame]
    with ReadDataFrameParameters
    with Params
    with OperationDocumentation {

  override val id: Id = "c48dd54c-6aef-42df-ad7a-42fc59a09f0e"
  override val name: String = "Read DataFrame"
  override val description: String =
    "Reads a DataFrame from a file or database"

  override val since: Version = Version(0, 4, 0)

  val params: Array[Param[_]] = Array(storageType)
  setDefault(storageType, new InputStorageTypeChoice.File())

  override def execute()(context: ExecutionContext): DataFrame = {
    implicit val ec = context

    try {
      val dataframe = getStorageType() match {
        case jdbcChoice: InputStorageTypeChoice.Jdbc => readFromJdbc(jdbcChoice)
        case googleSheet: InputStorageTypeChoice.GoogleSheet => DataFrameFromGoogleSheetReader.readFromGoogleSheet(
          googleSheet
        )
        case fileChoice: InputStorageTypeChoice.File => DataFrameFromFileReader.readFromFile(fileChoice)
      }
      DataFrame.fromSparkDataFrame(dataframe)
    } catch {
      case e: UnknownHostException => throw DeepSenseUnknownHostException(e)
      case e: IOException => throw DeepSenseIOException(e)
    }
  }

  override def inferKnowledge()(context: InferContext):
      (DKnowledge[DataFrame], InferenceWarnings) = {
    FilePathHasValidFileScheme.validate(this)
    ParquetSupportedOnClusterOnly.validate(this)
    super.inferKnowledge()(context)
  }

  private def readFromJdbc
      (jdbcChoice: InputStorageTypeChoice.Jdbc)
      (implicit context: ExecutionContext): SparkDataFrame =
    context.sparkSQLSession
      .read.format("jdbc")
      .option("driver", jdbcChoice.getJdbcDriverClassName)
      .option("url", jdbcChoice.getJdbcUrl)
      .option("dbtable", jdbcChoice.getJdbcTableName)
      .load()

  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

object ReadDataFrame {
  val recordDelimiterSettingName = "textinputformat.record.delimiter"

  trait ReadDataFrameParameters {
    this: Params =>

    val storageType = ChoiceParam[InputStorageTypeChoice](
      name = "data storage type",
      description = Some("Storage type."))

    def getStorageType(): InputStorageTypeChoice = $(storageType)
    def setStorageType(value: InputStorageTypeChoice): this.type = set(storageType, value)
  }

  def apply(
      fileName: String,
      csvColumnSeparator: CsvParameters.ColumnSeparatorChoice,
      csvNamesIncluded: Boolean,
      csvConvertToBoolean: Boolean) : ReadDataFrame = {
    new ReadDataFrame()
      .setStorageType(
        new InputStorageTypeChoice.File()
          .setSourceFile(fileName)
          .setFileFormat(
            new InputFileFormatChoice.Csv()
              .setCsvColumnSeparator(csvColumnSeparator)
              .setNamesIncluded(csvNamesIncluded)
              .setShouldConvertToBoolean(csvConvertToBoolean)))
  }
}
