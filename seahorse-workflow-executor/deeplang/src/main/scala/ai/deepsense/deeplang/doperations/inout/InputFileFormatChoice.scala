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

sealed trait InputFileFormatChoice extends Choice {
  override val choiceOrder: List[Class[_ <: InputFileFormatChoice]] =
    InputFileFormatChoice.choiceOrder
}

object InputFileFormatChoice {
  class Csv()
    extends InputFileFormatChoice
    with CsvParameters
    with HasShouldConvertToBooleanParam {

    override val name: String = FileFormat.CSV.toString
    override val params: Array[Param[_]] =
      Array(
        csvColumnSeparator,
        namesIncluded,
        shouldConvertToBoolean)
  }
  class Parquet() extends InputFileFormatChoice {
    override val name: String = FileFormat.PARQUET.toString
    override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array()
  }
  class Json() extends InputFileFormatChoice {
    override val name: String = FileFormat.JSON.toString
    override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array()
  }

  val choiceOrder: List[Class[_ <: InputFileFormatChoice]] = List(
    classOf[Csv],
    classOf[Parquet],
    classOf[Json]
  )
}
