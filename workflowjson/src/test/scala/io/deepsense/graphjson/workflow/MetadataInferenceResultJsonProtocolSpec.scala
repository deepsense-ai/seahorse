/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphjson.workflow

import spray.json._

import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang.doperables.dataframe.{CommonColumnMetadata, DataFrameMetadata}
import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.inference.{InferenceWarning, InferenceWarnings}
import io.deepsense.deeplang.parameters.ColumnType
import io.deepsense.model.json.workflow.MetadataInferenceResultJsonProtocol
import io.deepsense.models.metadata.MetadataInferenceResult

class MetadataInferenceResultJsonProtocolSpec extends StandardSpec
    with UnitTestSupport
    with MetadataInferenceResultJsonProtocol {

  "MetadataInferenceResult" should {
    "be properly serialized to JSON" in {
      val exampleResult = prepareMetadataInferenceResult()
      exampleResult.toJson shouldBe JsObject(
        "metadata" -> JsArray(
          JsObject(
            "type" -> JsString("DataFrameMetadata"),
            "content" -> JsObject(
              "isExact" -> JsBoolean(true),
              "isColumnCountExact" -> JsBoolean(true),
              "columns" -> JsObject(
                "col1" -> JsObject(
                  "name" -> JsString("col1"),
                  "index" -> JsNumber(0),
                  "columnType" -> JsString("numeric")
                ),
                "col2" -> JsObject(
                  "name" -> JsString("col2"),
                  "index" -> JsNumber(1),
                  "columnType" -> JsString("string")
                )
              )
            )
          )
        ),
        "warnings" -> JsArray(JsString("warning1"), JsString("warning2")),
        "errors" -> JsArray(JsString("error1"), JsString("error2"))
      )
    }
  }

  private def prepareMetadataInferenceResult(): MetadataInferenceResult = {

    val dfMetadata = DataFrameMetadata(
      isExact = true,
      isColumnCountExact = true,
      Map(
        "col1" -> CommonColumnMetadata("col1", Some(0), Some(ColumnType.numeric)),
        "col2" -> CommonColumnMetadata("col2", Some(1), Some(ColumnType.string))
      )
    )

    val warnings = InferenceWarnings(
      Vector(
        new InferenceWarning("warning1") {},
        new InferenceWarning("warning2") {}))

    val errors = Vector(
      new DeepLangException("error1") {},
      new DeepLangException("error2") {})

    MetadataInferenceResult(Seq(Some(dfMetadata)), warnings, errors)
  }

}
