package io.deepsense.e2etests.client

import play.api.libs.json.{JsValue => PlayJsValue}
import spray.json.{JsValue => SprayJsValue}

object SprayJsonFromPlayJson {

  import spray.json._

  def apply(playJs: PlayJsValue): SprayJsValue = {
    val jsonString = play.api.libs.json.Json.stringify(playJs)
    jsonString.parseJson
  }

}
