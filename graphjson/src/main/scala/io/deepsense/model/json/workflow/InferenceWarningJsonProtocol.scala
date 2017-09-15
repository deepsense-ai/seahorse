/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.model.json.workflow

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.deeplang.inference.{InferenceWarning, InferenceWarnings}

trait InferenceWarningJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object InferenceWarningMappingFormat extends JsonFormat[InferenceWarning] {
    override def write(warning: InferenceWarning): JsValue = JsString(warning.message)
    override def read(value: JsValue): InferenceWarning = ???
  }
}

trait InferenceWarningsJsonProtocol extends DefaultJsonProtocol
    with SprayJsonSupport
    with InferenceWarningJsonProtocol {

  implicit object InferenceWarningsMappingFormat extends JsonFormat[InferenceWarnings] {
    override def write(warnings: InferenceWarnings): JsValue = warnings.warnings.toJson
    override def read(value: JsValue): InferenceWarnings = ???
  }
}
