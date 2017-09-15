/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations.inout

import ai.deepsense.deeplang.doperations.readwritedataframe.googlestorage._
import ai.deepsense.deeplang.params.choice.{Choice, ChoiceParam}
import ai.deepsense.deeplang.params.library.SaveToLibraryParam
import ai.deepsense.deeplang.params.{BooleanParam, Param, StorageType, StringParam}

sealed trait OutputStorageTypeChoice extends Choice {
  import OutputStorageTypeChoice._

  override val choiceOrder: List[Class[_ <: OutputStorageTypeChoice]] = List(
    classOf[File],
    classOf[Jdbc],
    classOf[GoogleSheet]
  )
}

object OutputStorageTypeChoice {

  class File() extends OutputStorageTypeChoice {

    override val name: String = StorageType.FILE.toString

    val outputFile = SaveToLibraryParam(
      name = "output file",
      description = Some("Output file path."))

    def getOutputFile(): String = $(outputFile)
    def setOutputFile(value: String): this.type = set(outputFile, value)

    val shouldOverwrite = BooleanParam(
      name = "overwrite",
      description = Some("Should saving a file overwrite an existing file with the same name?")
    )
    setDefault(shouldOverwrite, true)

    def getShouldOverwrite: Boolean = $(shouldOverwrite)
    def setShouldOverwrite(value: Boolean): this.type = set(shouldOverwrite, value)

    val fileFormat = ChoiceParam[OutputFileFormatChoice](
      name = "format",
      description = Some("Format of the output file."))
    setDefault(fileFormat, new OutputFileFormatChoice.Csv())

    def getFileFormat(): OutputFileFormatChoice = $(fileFormat)
    def setFileFormat(value: OutputFileFormatChoice): this.type = set(fileFormat, value)

    override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array(outputFile, shouldOverwrite, fileFormat)
  }

  class Jdbc()
    extends OutputStorageTypeChoice
    with JdbcParameters {

    val shouldOverwrite = BooleanParam(
      name = "overwrite",
      description = Some("Should saving a table overwrite an existing table with the same name?")
    )
    setDefault(shouldOverwrite, true)

    def getShouldOverwrite: Boolean = $(shouldOverwrite)
    def setShouldOverwrite(value: Boolean): this.type = set(shouldOverwrite, value)

    override val name: String = StorageType.JDBC.toString
    override val params: Array[Param[_]] =
      Array(jdbcUrl, jdbcDriverClassName, jdbcTableName, shouldOverwrite)
  }

  class GoogleSheet()
    extends OutputStorageTypeChoice with GoogleSheetParams with NamesIncludedParam
    with HasShouldConvertToBooleanParam  {

    override val name = "Google Sheet"
    override lazy val params: Array[Param[_]] = Array(
      googleSheetId, serviceAccountCredentials, namesIncluded, shouldConvertToBoolean
    )

  }

}
