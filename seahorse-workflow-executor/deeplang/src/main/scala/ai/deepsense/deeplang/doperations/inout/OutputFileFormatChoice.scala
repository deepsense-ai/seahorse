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

import ai.deepsense.deeplang.params.{FileFormat, Param}
import ai.deepsense.deeplang.params.choice.Choice

sealed trait OutputFileFormatChoice extends Choice {
  import OutputFileFormatChoice._

  override val choiceOrder: List[Class[_ <: OutputFileFormatChoice]] = List(
    classOf[Csv],
    classOf[Parquet],
    classOf[Json])
}

object OutputFileFormatChoice {
  class Csv()
      extends OutputFileFormatChoice
      with CsvParameters {
    override val name: String = FileFormat.CSV.toString
    override val params: Array[Param[_]] = Array(csvColumnSeparator, namesIncluded)
  }
  class Parquet() extends OutputFileFormatChoice {
    override val name: String = FileFormat.PARQUET.toString
    override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array()
  }
  class Json() extends OutputFileFormatChoice {
    override val name: String = FileFormat.JSON.toString
    override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array()
  }
}

object OutputFromInputFileFormat {

  def apply(inputFileFormatChoice: InputFileFormatChoice): OutputFileFormatChoice =
    inputFileFormatChoice match {
      case csv: InputFileFormatChoice.Csv =>
        val output = new OutputFileFormatChoice.Csv()
        csv.copyValues(output)
      case json: InputFileFormatChoice.Json => new OutputFileFormatChoice.Json()
      case parquet: InputFileFormatChoice.Parquet => new OutputFileFormatChoice.Parquet()
      case unsupported => throw new IllegalStateException(
        s"Unsupported input file format $inputFileFormatChoice"
      )
    }

}
