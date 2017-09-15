/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations.readwritedatasource

import ai.deepsense.api.datasourcemanager.model._
import ai.deepsense.deeplang.doperations.inout.CsvParameters.ColumnSeparatorChoice
import ai.deepsense.deeplang.doperations.inout.{InputFileFormatChoice, InputStorageTypeChoice, OutputFileFormatChoice, OutputStorageTypeChoice}

object FromDatasourceConverters {

  trait DatasourceParams {
    def path: String
    def fileFormat: FileFormat
    def csvFileFormatParams: CsvFileFormatParams
  }

  implicit def hdfsParamsToInputStorageParams(params: HdfsParams): DatasourceParams =
    new DatasourceParams {
      override def path: String = params.getHdfsPath
      override def fileFormat: FileFormat = params.getFileFormat
      override def csvFileFormatParams: CsvFileFormatParams = params.getCsvFileFormatParams
    }

  implicit def externalFileParamsToInputStorageParams(params: ExternalFileParams): DatasourceParams =
    new DatasourceParams {
      override def path: String = params.getUrl
      override def fileFormat: FileFormat = params.getFileFormat
      override def csvFileFormatParams: CsvFileFormatParams = params.getCsvFileFormatParams
    }

  implicit def libraryParamsToInputStorageParams(params: LibraryFileParams): DatasourceParams =
    new DatasourceParams {
      override def path: String = params.getLibraryPath
      override def fileFormat: FileFormat = params.getFileFormat
      override def csvFileFormatParams: CsvFileFormatParams = params.getCsvFileFormatParams
    }

  // Similar code in Input/Output TODO DRY
  object InputFileStorageType {
    def get(inputStorageParams: DatasourceParams): InputStorageTypeChoice.File =
      new InputStorageTypeChoice.File()
        .setSourceFile(inputStorageParams.path)
        .setFileFormat(inputStorageParams.fileFormat match {
          case FileFormat.JSON => new InputFileFormatChoice.Json()
          case FileFormat.PARQUET => new InputFileFormatChoice.Parquet()
          case FileFormat.CSV => csvFormatChoice(inputStorageParams.csvFileFormatParams)
        })

    private def csvFormatChoice(csvParams: CsvFileFormatParams): InputFileFormatChoice.Csv =
      new InputFileFormatChoice.Csv()
        .setNamesIncluded(csvParams.getIncludeHeader)
        .setShouldConvertToBoolean(csvParams.getConvert01ToBoolean)
        .setCsvColumnSeparator(csvParams.getSeparatorType match {
          case CsvSeparatorType.COLON => ColumnSeparatorChoice.Colon()
          case CsvSeparatorType.COMMA => ColumnSeparatorChoice.Comma()
          case CsvSeparatorType.SEMICOLON => ColumnSeparatorChoice.Semicolon()
          case CsvSeparatorType.SPACE => ColumnSeparatorChoice.Space()
          case CsvSeparatorType.TAB => ColumnSeparatorChoice.Tab()
          case CsvSeparatorType.CUSTOM =>
            ColumnSeparatorChoice.Custom()
              .setCustomColumnSeparator(csvParams.getCustomSeparator)
        })
  }

  object OutputFileStorageType {
    def get(inputStorageParams: DatasourceParams): OutputStorageTypeChoice.File =
      new OutputStorageTypeChoice.File()
        .setOutputFile(inputStorageParams.path)
        .setFileFormat(inputStorageParams.fileFormat match {
          case FileFormat.JSON => new OutputFileFormatChoice.Json()
          case FileFormat.PARQUET => new OutputFileFormatChoice.Parquet()
          case FileFormat.CSV => csvFormatChoice(inputStorageParams.csvFileFormatParams)
        })

    private def csvFormatChoice(csvParams: CsvFileFormatParams): OutputFileFormatChoice.Csv =
      new OutputFileFormatChoice.Csv()
        .setNamesIncluded(csvParams.getIncludeHeader)
        .setCsvColumnSeparator(csvParams.getSeparatorType match {
          case CsvSeparatorType.COLON => ColumnSeparatorChoice.Colon()
          case CsvSeparatorType.COMMA => ColumnSeparatorChoice.Comma()
          case CsvSeparatorType.SEMICOLON => ColumnSeparatorChoice.Semicolon()
          case CsvSeparatorType.SPACE => ColumnSeparatorChoice.Space()
          case CsvSeparatorType.TAB => ColumnSeparatorChoice.Tab()
          case CsvSeparatorType.CUSTOM =>
            ColumnSeparatorChoice.Custom()
              .setCustomColumnSeparator(csvParams.getCustomSeparator)
        })
  }

}
