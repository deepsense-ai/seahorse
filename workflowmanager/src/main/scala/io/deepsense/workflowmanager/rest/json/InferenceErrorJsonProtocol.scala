/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest.json

import io.deepsense.deeplang.exceptions.DeepLangException
import spray.httpx.SprayJsonSupport
import spray.json.{JsString, JsValue, JsonFormat, DefaultJsonProtocol}

trait InferenceErrorJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object InferenceErrorMappingFormat extends JsonFormat[DeepLangException] {
    override def write(exc: DeepLangException): JsValue = JsString(exc.message)
    override def read(value: JsValue): DeepLangException = ???
  }
}
