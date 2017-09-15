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
import java.util.{Properties, UUID}

import scala.reflect.runtime.{universe => ru}

import com.google.common.io.Files
import org.apache.spark.SparkException
import org.apache.spark.sql.SaveMode

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.{DeepSenseIOException, WriteFileException}
import io.deepsense.deeplang.doperations.inout.OutputFileFormatChoice.Csv
import io.deepsense.deeplang.doperations.inout.OutputFileFormatChoice._
import io.deepsense.deeplang.doperations.inout.OutputStorageTypeChoice.{File, GoogleSheet}
import io.deepsense.deeplang.doperations.inout._
import io.deepsense.deeplang.doperations.readwritedataframe._
import io.deepsense.deeplang.doperations.readwritedataframe.filestorage.csv.{CsvSchemaInferencerAfterReading, CsvSchemaStringifierBeforeCsvWriting}
import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.Params
import io.deepsense.deeplang.params.choice.ChoiceParam
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperations.inout.CsvParameters.ColumnSeparatorChoice
import io.deepsense.deeplang.doperations.readwritedataframe.filestorage.DataFrameToFileWriter
import io.deepsense.deeplang.doperations.readwritedataframe.googlestorage.{DataFrameFromGoogleSheetReader, DataFrameToGoogleSheetWriter}
import io.deepsense.deeplang.doperations.readwritedataframe.validators.{FilePathHasValidFileScheme, ParquetSupportedOnClusterOnly}

class WriteDataFrame()
  extends DOperation1To0[DataFrame]
  with Params {

  override val id: Id = "9e460036-95cc-42c5-ba64-5bc767a40e4e"
  override val name: String = "Write DataFrame"
  override val description: String = "Writes a DataFrame to a file or database"

  override val since: Version = Version(0, 4, 0)

  val storageType = ChoiceParam[OutputStorageTypeChoice](
    name = "data storage type",
    description = "Storage type.")

  def getStorageType(): OutputStorageTypeChoice = $(storageType)
  def setStorageType(value: OutputStorageTypeChoice): this.type = set(storageType, value)

  val params = declareParams(storageType)
  setDefault(storageType, new OutputStorageTypeChoice.File())

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): Unit = {
    import OutputStorageTypeChoice._
    try {
      getStorageType() match {
        case jdbcChoice: Jdbc => writeToJdbc(jdbcChoice, context, dataFrame)
        case googleSheetChoice: GoogleSheet => DataFrameToGoogleSheetWriter.writeToGoogleSheet(
          googleSheetChoice, context, dataFrame
        )
        case fileChoice: File => DataFrameToFileWriter.writeToFile(fileChoice, context, dataFrame)
      }
    } catch {
      case e: IOException =>
        logger.error(s"WriteDataFrame error. Could not write file to designated storage", e)
        throw DeepSenseIOException(e)
    }
  }

  private def writeToJdbc(
      jdbcChoice: OutputStorageTypeChoice.Jdbc,
      context: ExecutionContext,
      dataFrame: DataFrame): Unit = {
    val properties = new Properties()
    properties.setProperty("driver", jdbcChoice.getJdbcDriverClassName)

    val jdbcUrl = jdbcChoice.getJdbcUrl
    val jdbcTableName = jdbcChoice.getJdbcTableName

    dataFrame.sparkDataFrame.write.jdbc(jdbcUrl, jdbcTableName, properties)
  }

  override protected def _inferKnowledge(
      context: InferContext)(k0: DKnowledge[DataFrame]): (Unit, InferenceWarnings) = {
    FilePathHasValidFileScheme.validate(this)
    ParquetSupportedOnClusterOnly.validate(this)
    super._inferKnowledge(context)(k0)
  }

}
