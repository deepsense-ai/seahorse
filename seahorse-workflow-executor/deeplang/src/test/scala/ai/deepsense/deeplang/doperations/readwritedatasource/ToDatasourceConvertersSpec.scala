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

import org.scalatest.{FreeSpec, Matchers}

import ai.deepsense.api.datasourcemanager.model._
import ai.deepsense.deeplang.doperations.ReadDataFrame.ReadDataFrameParameters
import ai.deepsense.deeplang.doperations.WriteDataFrame
import ai.deepsense.deeplang.doperations.inout.CsvParameters.ColumnSeparatorChoice
import ai.deepsense.deeplang.doperations.inout._
import ai.deepsense.deeplang.params.{Param, Params}

class ToDatasourceConvertersSpec extends FreeSpec with Matchers {

  case class SeparatorTest(
      setup: Setup,
      sepName: String,
      separator: ColumnSeparatorChoice,
      expected: CsvSeparatorType)

  case class InputFileFormatTest(
      setup: Setup,
      formatName: String,
      format: InputFileFormatChoice,
      expected: FileFormat)

  case class OutputFileFormatTest(
      setup: Setup,
      formatName: String,
      format: OutputFileFormatChoice,
      expected: FileFormat)

  case class OutParamsTest[Params](
      setup: Setup,
      paramsName: String,
      conversion: Setup => OutputStorageTypeChoice.File => Params,
      extractPath: Params => String,
      extractFileFormat: Params => FileFormat)

  case class InParamsTest[Params](
      setup: Setup,
      paramsName: String,
      conversion: Setup => InputStorageTypeChoice.File => Params,
      extractPath: Params => String,
      extractFileFormat: Params => FileFormat)

  val uutName = ToDatasourceConverters.getClass.getSimpleName.filterNot(_ == '$')

  trait Setup {
    val uut = ToDatasourceConverters

    def customSeparatorString = "|"

    val csvParameters = new Params with CsvParameters with HasShouldConvertToBooleanParam {
      override def params: Array[Param[_]] = ???
    }

    val csvParametersWithoutShouldConvertToBoolean = new Params with CsvParameters {
      override def params: Array[Param[_]] = ???
    }

    val separatorChoice = new ColumnSeparatorChoice.Custom
    separatorChoice.setCustomColumnSeparator(customSeparatorString)

    val expectedSeparatorType = CsvSeparatorType.CUSTOM

    csvParameters.setCsvColumnSeparator(separatorChoice)
    csvParametersWithoutShouldConvertToBoolean.setCsvColumnSeparator(separatorChoice)

    def shouldConvertToBoolean = true
    csvParameters.setShouldConvertToBoolean(shouldConvertToBoolean)

    def namesIncluded = true
    csvParameters.setNamesIncluded(namesIncluded)
    csvParametersWithoutShouldConvertToBoolean.setNamesIncluded(namesIncluded)

    def path = "library://file_in_library.csv"

    val inputCsvFileFormat = new InputFileFormatChoice.Csv
    inputCsvFileFormat.setNamesIncluded(namesIncluded)
    .setShouldConvertToBoolean(shouldConvertToBoolean)
    .setCsvColumnSeparator(separatorChoice)

    val outputCsvFileFormat = new OutputFileFormatChoice.Csv
    outputCsvFileFormat.setNamesIncluded(namesIncluded)
    .setCsvColumnSeparator(separatorChoice)

    val inputFileChoice = new InputStorageTypeChoice.File
    inputFileChoice.setSourceFile(path)
    .setFileFormat(inputCsvFileFormat)

    val outputFileChoice = new OutputStorageTypeChoice.File
    outputFileChoice.setOutputFile(path)
    .setFileFormat(outputCsvFileFormat)

    val googleSheetParams = new Params
      with GoogleSheetParams
      with NamesIncludedParam
      with HasShouldConvertToBooleanParam {
      override def params: Array[Param[_]] = ???
    }

    googleSheetParams.setShouldConvertToBoolean(shouldConvertToBoolean)
    .setNamesIncluded(namesIncluded)

    def googleSheetId = "googleSheetId"
    def googleSheetCredentials = "googleSheetCredentials"
    googleSheetParams.setGoogleServiceAccountCredentials(googleSheetCredentials)
    .setGoogleSheetId(googleSheetId)

    val jdbcParams = new Params with JdbcParameters {
      override def params: Array[Param[_]] = ???
    }

    def jdbcTableName = "table"
    def jdbcDriver = "jdbc.driver.Driver"
    def jdbcUrl = "jdbc:scheme:db"
    jdbcParams.setJdbcTableName(jdbcTableName)
    .setJdbcDriverClassName(jdbcDriver)
    .setJdbcUrl(jdbcUrl)
  }

  def allSeparatorTests(s: => Setup): Seq[SeparatorTest] = Seq[SeparatorTest](
    {
      val setup = s
      SeparatorTest(setup, "custom",
        {
          val sep = new ColumnSeparatorChoice.Custom
          sep.setCustomColumnSeparator(setup.customSeparatorString)
          sep
        },
        CsvSeparatorType.CUSTOM)
    },

    SeparatorTest(s, "comma", new ColumnSeparatorChoice.Comma, CsvSeparatorType.COMMA),
    SeparatorTest(s, "colon", new ColumnSeparatorChoice.Colon, CsvSeparatorType.COLON),
    SeparatorTest(s, "semi-colon", new ColumnSeparatorChoice.Semicolon, CsvSeparatorType.SEMICOLON),
    SeparatorTest(s, "space", new ColumnSeparatorChoice.Space, CsvSeparatorType.SPACE),
    SeparatorTest(s, "tab", new ColumnSeparatorChoice.Tab, CsvSeparatorType.TAB)
  )

  def allInputFileFormatTests(s: => Setup): Seq[InputFileFormatTest] = Seq[InputFileFormatTest](
    InputFileFormatTest(s, "csv", new InputFileFormatChoice.Csv, FileFormat.CSV),
    InputFileFormatTest(s, "json", new InputFileFormatChoice.Json, FileFormat.JSON),
    InputFileFormatTest(s, "parquet", new InputFileFormatChoice.Parquet, FileFormat.PARQUET)
  )

  def allOutputFileFormatTests(s: => Setup): Seq[OutputFileFormatTest] = Seq[OutputFileFormatTest](
    OutputFileFormatTest(s, "csv", new OutputFileFormatChoice.Csv, FileFormat.CSV),
    OutputFileFormatTest(s, "json", new OutputFileFormatChoice.Json, FileFormat.JSON),
    OutputFileFormatTest(s, "parquet", new OutputFileFormatChoice.Parquet, FileFormat.PARQUET)
  )

  def registerOutputFileFormatTest(outputFileFormatTest: OutputFileFormatTest): Unit = {
    val OutputFileFormatTest(setup, formatName, format, expected) = outputFileFormatTest
    s"convert $formatName output file format" in {
      val converted = setup.uut.convertOutputFileFormatChoice(format)

      converted shouldBe expected
    }
  }

  def registerOutputFileFormatTests(outputFileFormatTests: Seq[OutputFileFormatTest]): Unit =
    outputFileFormatTests.foreach(registerOutputFileFormatTest)

  def registerInputFileFormatTest(inputFileFormatTest: InputFileFormatTest): Unit = {
    val InputFileFormatTest(setup, formatName, format, expected) = inputFileFormatTest
    s"convert $formatName input file format" in {
      val converted = setup.uut.convertInputFileFormatChoice(format)

      converted shouldBe expected
    }
  }

  def registerInputFileFormatTests(inputFileFormatTests: Seq[InputFileFormatTest]): Unit =
    inputFileFormatTests.foreach(registerInputFileFormatTest)

  def registerSeparatorTest(separatorTest: SeparatorTest): Unit = {
    val SeparatorTest(setup, sepName, sep, expected) = separatorTest
    s"convert $sepName csv separator" in {
      val converted = setup.uut.convertColumnSeparatorChoice(sep)
      converted shouldBe expected
    }
  }

  def registerSeparatorTests(separators: Seq[SeparatorTest]): Unit =
    separators.foreach(registerSeparatorTest)

  def registerOutParamsTest[Params](outParamsTest: OutParamsTest[Params]): Unit = {
    val OutParamsTest(setup, paramsName, conversion, extractPath, extractFileFormat) = outParamsTest

    s"convert output $paramsName" in {
      val choice = new OutputStorageTypeChoice.File
      choice.setOutputFile(setup.path)
      choice.setFileFormat(setup.outputCsvFileFormat)
      val converted = conversion(setup)(choice)
      extractPath(converted) shouldBe setup.path
      extractFileFormat(converted) shouldBe FileFormat.CSV
    }
  }

  def registerInParamsTest[Params](inParamsTest: InParamsTest[Params]): Unit = {
    val InParamsTest(setup, paramsName, conversion, extractPath, extractFileFormat) = inParamsTest

    s"convert input $paramsName" in {
      val choice = new InputStorageTypeChoice.File
      choice.setSourceFile(setup.path)
      choice.setFileFormat(setup.inputCsvFileFormat)
      val converted = conversion(setup)(choice)
      extractPath(converted) shouldBe setup.path
      extractFileFormat(converted) shouldBe FileFormat.CSV
    }
  }

  s"A $uutName should" - {

    registerSeparatorTests(allSeparatorTests(new Setup {}))
    registerInputFileFormatTests(allInputFileFormatTests(new Setup {}))
    registerOutputFileFormatTests(allOutputFileFormatTests(new Setup {}))
    registerOutParamsTest(
      OutParamsTest[HdfsParams](
        new Setup {
          override val path = "hdfs://hdfs_file"
        },
        "hdfs params",
        _.uut.outputStorageTypeChoiceFileToHdfsParams,
        _.getHdfsPath,
        _.getFileFormat
      )
    )

    registerOutParamsTest(
      OutParamsTest[LibraryFileParams](
        new Setup {
          override val path = "library://library_file"
        },
        "library params",
        _.uut.outputStorageTypeChoiceFileToLibraryFileParams,
        _.getLibraryPath,
        _.getFileFormat
      )
    )

    registerOutParamsTest(
      OutParamsTest[ExternalFileParams](
        new Setup {
          override val path = "ftp://externalfile"
        },
        "external file params",
        _.uut.outputStorageTypeChoiceFileToExternalFileParams,
        _.getUrl,
        _.getFileFormat
      )
    )

    registerInParamsTest(
      InParamsTest[HdfsParams](
        new Setup {
          override val path = "hdfs://hdfs_file"
        },
        "hdfs params",
        _.uut.inputStorageTypeChoiceFileTypeToHdfsParams,
        _.getHdfsPath,
        _.getFileFormat
      )
    )

    registerInParamsTest(
      InParamsTest[LibraryFileParams](
        new Setup {
          override val path = "library://library_file"
        },
        "library params",
        _.uut.inputStorageTypeChoiceFileToLibraryFileParams,
        _.getLibraryPath,
        _.getFileFormat
      )
    )

    registerInParamsTest(
      InParamsTest[ExternalFileParams](
        new Setup {
          override val path = "ftp://externalfile"
        },
        "external file params",
        _.uut.inputStorageTypeChoiceFileToExternalFileParams,
        _.getUrl,
        _.getFileFormat
      )
    )

    "convert google spreadsheet parameters" in {
      new Setup {
        val converted = uut.convertGoogleSheet(googleSheetParams)

        converted.getConvert01ToBoolean shouldEqual shouldConvertToBoolean
        converted.getIncludeHeader shouldEqual namesIncluded
        converted.getGoogleServiceAccountCredentials shouldBe googleSheetCredentials
        converted.getGoogleSpreadsheetId shouldBe googleSheetId
      }
    }

    "convert jdbc parameters" in {
      new Setup {
        val converted = uut.convertJdbcParams(jdbcParams)

        converted.getDriver shouldBe jdbcDriver
        converted.getTable shouldBe jdbcTableName
        converted.getUrl shouldBe jdbcUrl
      }
    }

    "convert csv parameters which don't have 'shouldConvertToBoolean' parameter" in {
      new Setup {
        val converted = uut.convertCsvParams(csvParametersWithoutShouldConvertToBoolean)

        converted.getCustomSeparator shouldBe customSeparatorString
        converted.getConvert01ToBoolean shouldBe false
        converted.getIncludeHeader shouldEqual namesIncluded
        converted.getSeparatorType shouldBe CsvSeparatorType.CUSTOM
      }
    }

    "convert csv parameters" in {
      new Setup {
        val converted = uut.convertCsvParams(csvParameters)

        converted.getCustomSeparator shouldBe customSeparatorString
        converted.getConvert01ToBoolean shouldEqual shouldConvertToBoolean
        converted.getIncludeHeader shouldEqual namesIncluded
        converted.getSeparatorType shouldBe CsvSeparatorType.CUSTOM
      }
    }

    "and above all should" - {

      "convert library WriteDataFrame to DatasourceParams" in {
        new Setup {
          val writeDataFrame = new WriteDataFrame
          writeDataFrame.setStorageType(outputFileChoice)

          val dataSourceParams = uut.wdfToDatasourceParams(writeDataFrame)
          dataSourceParams.getDatasourceType shouldBe DatasourceType.LIBRARYFILE

          dataSourceParams.getLibraryFileParams should not be null
        }
      }

      "convert external file WriteDataFrame to DatasourceParams" in {
        new Setup {
          override def path = "ftp://externalfile"

          val writeDataFrame = new WriteDataFrame

          writeDataFrame.setStorageType(outputFileChoice)

          val datasourceParams = uut.wdfToDatasourceParams(writeDataFrame)
          datasourceParams.getDatasourceType shouldBe DatasourceType.EXTERNALFILE

          val externalFileParams = datasourceParams.getExternalFileParams

          externalFileParams should not be null

        }
      }

      "convert hdfs file WriteDataFrame to DatasourceParams" in {
        new Setup {
          override def path = "hdfs://hdfs_file"

          val writeDataFrame = new WriteDataFrame

          writeDataFrame.setStorageType(outputFileChoice)

          val datasourceParams = uut.wdfToDatasourceParams(writeDataFrame)
          datasourceParams.getDatasourceType shouldBe DatasourceType.HDFS

          val hdfsParams = datasourceParams.getHdfsParams

          hdfsParams should not be null

        }
      }

      "convert google sheet WriteDataFrame to DatasourceParams" in {
        new Setup {
          val outputGoogleSheetFileChoice = new OutputStorageTypeChoice.GoogleSheet
          outputGoogleSheetFileChoice.setGoogleSheetId(googleSheetId)
          outputGoogleSheetFileChoice.setGoogleServiceAccountCredentials(googleSheetCredentials)
          outputGoogleSheetFileChoice.setShouldConvertToBoolean(shouldConvertToBoolean)
          outputGoogleSheetFileChoice.setNamesIncluded(namesIncluded)
          val writeDataFrame = new WriteDataFrame
          writeDataFrame.setStorageType(outputGoogleSheetFileChoice)

          val datasourceParams = uut.wdfToDatasourceParams(writeDataFrame)
          datasourceParams.getDatasourceType shouldBe DatasourceType.GOOGLESPREADSHEET

          datasourceParams.getGoogleSpreadsheetParams should not be null

        }
      }

      "convert google sheet ReadDataFrameParams to DatasourceParams" in {
        new Setup {
          val inputGoogleSheetFileChoice = new InputStorageTypeChoice.GoogleSheet
          inputGoogleSheetFileChoice.setGoogleSheetId(googleSheetId)
          inputGoogleSheetFileChoice.setGoogleServiceAccountCredentials(googleSheetCredentials)
          inputGoogleSheetFileChoice.setShouldConvertToBoolean(shouldConvertToBoolean)
          inputGoogleSheetFileChoice.setNamesIncluded(namesIncluded)
          val readDataFrameParams = new Params with ReadDataFrameParameters {
            override def params: Array[Param[_]] = ???
          }

          readDataFrameParams.setStorageType(inputGoogleSheetFileChoice)

          val datasourceParams = uut.rdfParamsToDatasourceParams(readDataFrameParams)
          datasourceParams.getDatasourceType shouldBe DatasourceType.GOOGLESPREADSHEET

          datasourceParams.getGoogleSpreadsheetParams should not be null

        }
      }

      "convert jdbc ReadDataFrameParams to DatasourceParams" in  {
        new Setup {
          val jdbcFileChoice = new InputStorageTypeChoice.Jdbc
          jdbcFileChoice.setJdbcUrl(jdbcUrl)
          jdbcFileChoice.setJdbcDriverClassName(jdbcDriver)
          jdbcFileChoice.setJdbcTableName(jdbcTableName)

          val readDataFrameParams = new Params with ReadDataFrameParameters {
            override def params: Array[Param[_]] = ???
          }

          readDataFrameParams.setStorageType(jdbcFileChoice)

          val datasourceParams = uut.rdfParamsToDatasourceParams(readDataFrameParams)
          datasourceParams.getDatasourceType shouldBe DatasourceType.JDBC

          datasourceParams.getJdbcParams should not be null

        }
      }

      "convert jdbc WriteDataFrame to DatasourceParams" in  {
        new Setup {
          val jdbcFileChoice = new OutputStorageTypeChoice.Jdbc
          jdbcFileChoice.setJdbcUrl(jdbcUrl)
          jdbcFileChoice.setJdbcDriverClassName(jdbcDriver)
          jdbcFileChoice.setJdbcTableName(jdbcTableName)

          val writeDataframe = new WriteDataFrame

          writeDataframe.setStorageType(jdbcFileChoice)

          val datasourceParams = uut.wdfToDatasourceParams(writeDataframe)
          datasourceParams.getDatasourceType shouldBe DatasourceType.JDBC

          datasourceParams.getJdbcParams should not be null

        }
      }


      "convert library ReadDataFrameParams to DatasourceParams" in {
        new Setup {
          val readDataFrameParameters = new Params with ReadDataFrameParameters {
            override def params: Array[Param[_]] = ???
          }

          readDataFrameParameters.setStorageType(inputFileChoice)

          val datasourceParams = uut.rdfParamsToDatasourceParams(readDataFrameParameters)
          datasourceParams.getDatasourceType shouldBe DatasourceType.LIBRARYFILE

          val libraryParams = datasourceParams.getLibraryFileParams

          libraryParams should not be null

        }
      }

      "convert external file ReadDataFrameParams to DatasourceParams" in {
        new Setup {
          override def path = "ftp://externalfile"

          val readDataFrameParameters = new Params with ReadDataFrameParameters {
            override def params: Array[Param[_]] = ???
          }

          readDataFrameParameters.setStorageType(inputFileChoice)

          val datasourceParams = uut.rdfParamsToDatasourceParams(readDataFrameParameters)
          datasourceParams.getDatasourceType shouldBe DatasourceType.EXTERNALFILE

          val externalFileParams = datasourceParams.getExternalFileParams

          externalFileParams should not be null

        }
      }

      "convert hdfs file ReadDataFrameParams to DatasourceParams" in {
        new Setup {
          override def path = "hdfs://hdfs_file"

          val readDataFrameParameters = new Params with ReadDataFrameParameters {
            override def params: Array[Param[_]] = ???
          }

          readDataFrameParameters.setStorageType(inputFileChoice)

          val datasourceParams = uut.rdfParamsToDatasourceParams(readDataFrameParameters)
          datasourceParams.getDatasourceType shouldBe DatasourceType.HDFS

          val hdfsParams = datasourceParams.getHdfsParams

          hdfsParams should not be null

        }
      }
    }
  }
}
