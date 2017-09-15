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

package io.deepsense.deeplang.doperations.inout

import io.deepsense.deeplang.parameters.StorageType
import io.deepsense.deeplang.params.{Param, StringParam}
import io.deepsense.deeplang.params.choice.{ChoiceParam, Choice}

sealed trait InputStorageTypeChoice extends Choice {
  import InputStorageTypeChoice._

  override val choiceOrder: List[Class[_ <: InputStorageTypeChoice]] = List(
    classOf[File],
    classOf[Jdbc],
    classOf[Cassandra])
}

object InputStorageTypeChoice {

  case class File() extends InputStorageTypeChoice {

    override val name: String = StorageType.FILE.toString

    val sourceFile = StringParam(
      name = "source",
      description = "Path to the DataFrame file")

    def getSourceFile: String = $(sourceFile)
    def setSourceFile(value: String): this.type = set(sourceFile, value)

    val fileFormat = ChoiceParam[InputFileFormatChoice](
      name = "format",
      description = "Format of the input file")
    setDefault(fileFormat, InputFileFormatChoice.Csv())

    def getFileFormat: InputFileFormatChoice = $(fileFormat)
    def setFileFormat(value: InputFileFormatChoice): this.type = set(fileFormat, value)

    override val params = declareParams(sourceFile, fileFormat)
  }

  case class Jdbc()
    extends InputStorageTypeChoice
    with JdbcParameters {

    override val name: String = StorageType.JDBC.toString
    override val params: Array[Param[_]] =
      declareParams(jdbcUrl, jdbcDriverClassName, jdbcTableName)
  }

  case class Cassandra()
    extends InputStorageTypeChoice
    with CassandraParameters {

    override val name: String = StorageType.CASSANDRA.toString
    override val params: Array[Param[_]] =
      declareParams(cassandraKeyspace, cassandraTable)
  }
}
