/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.models.json.workflow

import spray.json._

import io.deepsense.deeplang.doperables.dataframe.{CommonColumnMetadata, DataFrameMetadata}
import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.inference.{InferenceWarning, InferenceWarnings}
import io.deepsense.deeplang.parameters.ColumnType
import io.deepsense.models.json.{StandardSpec, UnitTestSupport}
import io.deepsense.models.metadata.MetadataInferenceResult

class MetadataInferenceResultJsonProtocolSpec
  extends StandardSpec
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
