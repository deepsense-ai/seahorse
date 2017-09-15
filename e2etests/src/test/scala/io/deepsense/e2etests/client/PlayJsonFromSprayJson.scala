package io.deepsense.e2etests.client

import play.api.libs.json.{JsValue => PlayJsValue}
import spray.json.{JsValue => SprayJsValue}

object PlayJsonFromSprayJson {

  def apply(js: SprayJsValue): PlayJsValue = {
    play.api.libs.json.Json.parse(js.compactPrint)
  }

}
