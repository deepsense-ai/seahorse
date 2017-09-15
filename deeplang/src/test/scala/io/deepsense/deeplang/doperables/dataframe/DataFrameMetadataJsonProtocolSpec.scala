/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import org.scalatest._
import org.scalatest.mock.MockitoSugar
import spray.json._

import io.deepsense.deeplang.DOperable.AbstractMetadata
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoriesMapping
import io.deepsense.deeplang.parameters.ColumnType

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
