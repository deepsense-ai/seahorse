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

import org.apache.commons.io.FilenameUtils

import ai.deepsense.api.datasourcemanager.model._
import ai.deepsense.deeplang.doperations.ReadDataFrame.ReadDataFrameParameters
import ai.deepsense.deeplang.doperations.WriteDataFrame
import ai.deepsense.deeplang.doperations.inout.CsvParameters.ColumnSeparatorChoice
import ai.deepsense.deeplang.doperations.inout._
import ai.deepsense.deeplang.doperations.readwritedataframe.{FilePath, FileScheme}

trait ToDatasourceConverters {

  implicit def outputStorageTypeChoiceFileToHdfsParams(fileType: OutputStorageTypeChoice.File): HdfsParams = {
    val hdfsParams = new HdfsParams

    hdfsParams.setFileFormat(fileType.getFileFormat())
    hdfsParams.setHdfsPath(fileType.getOutputFile())

    if (hdfsParams.getFileFormat == FileFormat.CSV) {
      hdfsParams.setCsvFileFormatParams(
        fileType.getFileFormat().asInstanceOf[OutputFileFormatChoice.Csv]
      )
    }

    hdfsParams
  }

  implicit def inputStorageTypeChoiceFileToLibraryFileParams(
      fileType: InputStorageTypeChoice.File): LibraryFileParams = {
    val libraryFileParams = new LibraryFileParams

    libraryFileParams.setFileFormat(fileType.getFileFormat())
    libraryFileParams.setLibraryPath(fileType.getSourceFile())

    if (libraryFileParams.getFileFormat == FileFormat.CSV) {
      libraryFileParams.setCsvFileFormatParams(
        fileType.getFileFormat().asInstanceOf[InputFileFormatChoice.Csv]
      )
    }

    libraryFileParams
  }

  implicit def outputStorageTypeChoiceFileToLibraryFileParams(
      fileType: OutputStorageTypeChoice.File): LibraryFileParams = {
    val libraryFileParams = new LibraryFileParams

    libraryFileParams.setFileFormat(fileType.getFileFormat())
    libraryFileParams.setLibraryPath(fileType.getOutputFile())

    if (libraryFileParams.getFileFormat == FileFormat.CSV) {
      libraryFileParams.setCsvFileFormatParams(
        fileType.getFileFormat().asInstanceOf[OutputFileFormatChoice.Csv]
      )
    }

    libraryFileParams
  }

  implicit def convertGoogleSheet(
      googleSheet: GoogleSheetParams
        with HasShouldConvertToBooleanParam
        with NamesIncludedParam): GoogleSpreadsheetParams = {
    val params = new GoogleSpreadsheetParams

    params.setConvert01ToBoolean(googleSheet.getShouldConvertToBoolean)
    params.setGoogleServiceAccountCredentials(googleSheet.getGoogleServiceAccountCredentials())
    params.setGoogleSpreadsheetId(googleSheet.getGoogleSheetId())
    params.setIncludeHeader(googleSheet.getNamesIncluded)

    params
  }

  implicit def convertJdbcParams(jdbcParameters: JdbcParameters): JdbcParams = {
    val jdbcParams = new JdbcParams

    jdbcParams.setDriver(jdbcParameters.getJdbcDriverClassName)
    jdbcParams.setUrl(jdbcParameters.getJdbcUrl)
    jdbcParams.setTable(jdbcParameters.getJdbcTableName)

    jdbcParams
  }

  implicit def rdfParamsToDatasourceParams(readDataFrameParameters: ReadDataFrameParameters): DatasourceParams = {
    val params = new DatasourceParams
    val storageType = readDataFrameParameters.getStorageType()

    storageType match {
      case fileType: InputStorageTypeChoice.File =>
        val FilePath(scheme, path) = fileType.getSourceFile()
        params.setName(FilenameUtils.getName(path))
        scheme match {
          case FileScheme.File |
               FileScheme.FTP |
               FileScheme.HTTP |
               FileScheme.HTTPS =>
            params.setExternalFileParams(fileType)
            params.setDatasourceType(DatasourceType.EXTERNALFILE)
          case FileScheme.HDFS =>
            params.setHdfsParams(fileType)
            params.setDatasourceType(DatasourceType.HDFS)
          case FileScheme.Library =>
            params.setLibraryFileParams(fileType)
            params.setDatasourceType(DatasourceType.LIBRARYFILE)
        }

      case googleSheetType: InputStorageTypeChoice.GoogleSheet =>
        params.setName(googleSheetType.getGoogleSheetId())
        params.setDatasourceType(DatasourceType.GOOGLESPREADSHEET)
        params.setGoogleSpreadsheetParams(googleSheetType)

      case jdbcType: InputStorageTypeChoice.Jdbc =>
        params.setName(jdbcType.getJdbcTableName)
        params.setDatasourceType(DatasourceType.JDBC)
        params.setJdbcParams(jdbcType)
    }

    params
  }

  implicit def wdfToDatasourceParams(writeDataFrame: WriteDataFrame): DatasourceParams = {
    val params = new DatasourceParams
    val storageType = writeDataFrame.getStorageType()

    storageType match {
      case fileType: OutputStorageTypeChoice.File =>
        val FilePath(scheme, path) = fileType.getOutputFile()
        params.setName(FilenameUtils.getName(path))
        scheme match {
          case FileScheme.File |
               FileScheme.FTP |
               FileScheme.HTTP |
               FileScheme.HTTPS =>
            params.setExternalFileParams(fileType)
            params.setDatasourceType(DatasourceType.EXTERNALFILE)
          case FileScheme.HDFS =>
            params.setHdfsParams(fileType)
            params.setDatasourceType(DatasourceType.HDFS)
          case FileScheme.Library =>
            params.setLibraryFileParams(fileType)
            params.setDatasourceType(DatasourceType.LIBRARYFILE)
        }

      case googleSheetType: OutputStorageTypeChoice.GoogleSheet =>
        params.setName(googleSheetType.getGoogleSheetId())
        params.setDatasourceType(DatasourceType.GOOGLESPREADSHEET)
        params.setGoogleSpreadsheetParams(googleSheetType)

      case jdbcType: OutputStorageTypeChoice.Jdbc =>
        params.setName(jdbcType.getJdbcTableName)
        params.setDatasourceType(DatasourceType.JDBC)
        params.setJdbcParams(jdbcType)
    }

    params
  }


  implicit def convertInputFileFormatChoice(inputFileFormatChoice: InputFileFormatChoice): FileFormat =
    inputFileFormatChoice match {
      case _: InputFileFormatChoice.Csv => FileFormat.CSV
      case _: InputFileFormatChoice.Json => FileFormat.JSON
      case _: InputFileFormatChoice.Parquet => FileFormat.PARQUET
    }

  implicit def convertOutputFileFormatChoice(outputFileFormatChoice: OutputFileFormatChoice): FileFormat =
    outputFileFormatChoice match {
      case _: OutputFileFormatChoice.Csv => FileFormat.CSV
      case _: OutputFileFormatChoice.Json => FileFormat.JSON
      case _: OutputFileFormatChoice.Parquet => FileFormat.PARQUET
    }

  implicit def convertColumnSeparatorChoice(columnSeparatorChoice: ColumnSeparatorChoice): CsvSeparatorType =
    columnSeparatorChoice match {
      case _: ColumnSeparatorChoice.Colon => CsvSeparatorType.COLON
      case _: ColumnSeparatorChoice.Comma => CsvSeparatorType.COMMA
      case _: ColumnSeparatorChoice.Semicolon => CsvSeparatorType.SEMICOLON
      case _: ColumnSeparatorChoice.Space => CsvSeparatorType.SPACE
      case _: ColumnSeparatorChoice.Tab => CsvSeparatorType.TAB
      case _: ColumnSeparatorChoice.Custom => CsvSeparatorType.CUSTOM
    }

  implicit def convertCsvParams(
      csv: CsvParameters): CsvFileFormatParams = {
    val params = new CsvFileFormatParams

    val separatorType = csv.getCsvColumnSeparator()

    params.setSeparatorType(separatorType)

    if (params.getSeparatorType == CsvSeparatorType.CUSTOM) {
      params.setCustomSeparator(
        separatorType.asInstanceOf[ColumnSeparatorChoice.Custom].getCustomColumnSeparator)
    }

    params.setIncludeHeader(csv.getNamesIncluded)

    params.setConvert01ToBoolean(
      csv match {
        case hasShouldConvertParam: HasShouldConvertToBooleanParam =>
          hasShouldConvertParam.getShouldConvertToBoolean
        case _ => false
    })

    params
  }

  implicit def inputStorageTypeChoiceFileToExternalFileParams(
      fileType: InputStorageTypeChoice.File): ExternalFileParams = {
    val externalFileParams = new ExternalFileParams

    externalFileParams.setFileFormat(fileType.getFileFormat())
    externalFileParams.setUrl(fileType.getSourceFile())

    if (externalFileParams.getFileFormat == FileFormat.CSV) {
      externalFileParams.setCsvFileFormatParams(
        fileType.getFileFormat().asInstanceOf[InputFileFormatChoice.Csv]
      )
    }

    externalFileParams
  }

  implicit def outputStorageTypeChoiceFileToExternalFileParams(
      fileType: OutputStorageTypeChoice.File): ExternalFileParams = {
    val externalFileParams = new ExternalFileParams

    externalFileParams.setFileFormat(fileType.getFileFormat())
    externalFileParams.setUrl(fileType.getOutputFile())

    if (externalFileParams.getFileFormat == FileFormat.CSV) {
      externalFileParams.setCsvFileFormatParams(
        convertCsvParams(
        fileType.getFileFormat().asInstanceOf[OutputFileFormatChoice.Csv])
      )
    }

    externalFileParams
  }

  implicit def inputStorageTypeChoiceFileTypeToHdfsParams(fileType: InputStorageTypeChoice.File): HdfsParams = {
    val hdfsParams = new HdfsParams

    hdfsParams.setFileFormat(fileType.getFileFormat())
    hdfsParams.setHdfsPath(fileType.getSourceFile())

    if (hdfsParams.getFileFormat == FileFormat.CSV) {
      hdfsParams.setCsvFileFormatParams(
        fileType.getFileFormat().asInstanceOf[InputFileFormatChoice.Csv]
      )
    }

    hdfsParams
  }

}

object ToDatasourceConverters extends ToDatasourceConverters
