/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang.doperables.dataframe

import org.scalatest._
import org.scalatest.mock.MockitoSugar
import spray.json._

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.DOperable.AbstractMetadata
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoriesMapping

class DataFrameMetadataJsonProtocolSpec extends FlatSpec with Matchers with MockitoSugar {

  "DataFrameMetadata with full knowledge" should "be correctly serialized to json" in {
    val fixture = fullKnowledgeFixture
    fixture.metadata.serializeToJson shouldBe fixture.json
  }

  "DataFrameMetadata with partial knowledge" should "be correctly serialized to json" in {
    val fixture = partialKnowledgeFixture
    fixture.metadata.serializeToJson shouldBe fixture.json
  }

  "DataFrameMetadata with full knowledge" should "be correctly deserialized from json" in {
    val fixture = fullKnowledgeFixture
    DataFrameMetadata.deserializeFromJson(fixture.json) shouldBe fixture.metadata
  }

  "DataFrameMetadata with partial knowledge" should "be correctly deserialized from json" in {
    val fixture = partialKnowledgeFixture
    DataFrameMetadata.deserializeFromJson(fixture.json) shouldBe fixture.metadata
  }

  case class Fixture(json: JsValue, metadata: AbstractMetadata)

  private def fullKnowledgeFixture: Fixture = Fixture(
    metadata =
      DataFrameMetadata(
        true, true,
        Map("x" ->
          CategoricalColumnMetadata("x", Some(0), Some(CategoriesMapping(Seq("a", "b", "c")))),
          "y" ->
            CommonColumnMetadata("y", Some(1), Some(ColumnType.numeric))
        )
      ),
    json = JsObject(
      "type" -> JsString("DataFrameMetadata"),
      "content" -> JsObject(
        "isExact" -> JsBoolean(true),
        "isColumnCountExact" -> JsBoolean(true),
        "columns" -> JsObject(
          "x" -> JsObject(
            "name" -> JsString("x"),
            "index" -> JsNumber(0),
            "columnType" -> JsString("categorical"),
            "categories" -> JsArray(Vector("a", "b", "c").map(JsString(_)))
          ),
          "y" -> JsObject(
            "name" -> JsString("y"),
            "index" -> JsNumber(1),
            "columnType" -> JsString("numeric")
          )
        )
      )
    )
  )

  private def partialKnowledgeFixture: Fixture = Fixture(
    metadata = DataFrameMetadata(
      false, false,
      Map(
        "x" -> CategoricalColumnMetadata("x", None, None),
        "y" -> CommonColumnMetadata("y", None, None)
      )
    ),
      json = JsObject(
      "type" -> JsString("DataFrameMetadata"),
      "content" -> JsObject(
        "isExact" -> JsBoolean(false),
        "isColumnCountExact" -> JsBoolean(false),
        "columns" -> JsObject(
          "x" -> JsObject(
            "name" -> JsString("x"),
            "index" -> JsNull,
            "columnType" -> JsString("categorical"),
            "categories" -> JsNull
          ),
          "y" -> JsObject(
            "name" -> JsString("y"),
            "index" -> JsNull,
            "columnType" -> JsNull
          )
        )
      )
    )
  )
}
