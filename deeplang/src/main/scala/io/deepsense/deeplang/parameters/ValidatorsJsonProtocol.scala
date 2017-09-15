/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.parameters

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

  val rangeValidatorFormat = jsonFormat(
    RangeValidator, "begin", "end", "beginIncluded", "endIncluded", "step")
  val regexValidatorFormat = jsonFormat(RegexValidator, "regex")
}
