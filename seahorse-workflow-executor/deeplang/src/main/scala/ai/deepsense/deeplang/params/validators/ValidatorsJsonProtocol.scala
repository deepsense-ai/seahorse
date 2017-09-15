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

package ai.deepsense.deeplang.params.validators

import scala.util.matching.Regex

import spray.json._

object ValidatorsJsonProtocol extends DefaultJsonProtocol {
  implicit object RegexJsonFormat extends JsonFormat[Regex] {
    override def write(regex: Regex): JsValue = regex.toString.toJson

    /**
     * This method is not implemented on purpose - RegexJsonFormat is only needed
     * for writing inside [[regexValidatorFormat]].
     */
    override def read(json: JsValue): Regex = ???
  }

  implicit val rangeValidatorFormat = jsonFormat(
    RangeValidator.apply, "begin", "end", "beginIncluded", "endIncluded", "step")

  implicit val regexValidatorFormat = jsonFormat(RegexValidator, "regex")

  implicit val arrayLengthValidator = jsonFormat(ArrayLengthValidator.apply, "min", "max")

  implicit val complexArrayValidator = new JsonFormat[ComplexArrayValidator] {
    def write(v: ComplexArrayValidator): JsValue = {
      v.rangeValidator.configurationToJson
    }
    def read(json: JsValue): ComplexArrayValidator = ???
  }
//  TODO DS-3225 Complex Array Validator serialization
//  implicit val complexArrayValidator =
//    jsonFormat(ComplexArrayValidator.apply, "rangeValidator", "arrayLengthValidator")
}
