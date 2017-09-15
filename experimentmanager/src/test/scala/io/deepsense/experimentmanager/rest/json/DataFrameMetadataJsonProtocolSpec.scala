/**
 * Copyright (c) 2015, CodiLime Inc.
 */
package io.deepsense.experimentmanager.rest.json

import io.deepsense.deeplang.DOperable.AbstractMetadata
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoriesMapping
import io.deepsense.deeplang.doperables.dataframe.{ColumnMetadata, DataFrameMetadata}
import io.deepsense.deeplang.parameters.ColumnType
import org.scalatest.mock.MockitoSugar
import org.scalatest._
import spray.json._

class DataFrameMetadataJsonProtocolSpec extends FlatSpec with Matchers with MockitoSugar {

  "DataFrameMetadata with full knowledge" should "be correctly serialized to json" in {
    val metadata: AbstractMetadata = DataFrameMetadata(true, true,
      Map("x" -> ColumnMetadata("x", Some(0), Some(ColumnType.categorical))),
      Map("x" -> CategoriesMapping(Seq("a", "b", "c"))))

    val expectedJson = JsObject(
      "type" -> JsString("DataFrameMetadata"),
      "content" -> JsObject(
        "isExact" -> JsBoolean(true),
        "isColumnCountExact" -> JsBoolean(true),
        "columns" -> JsObject(
          "x" -> JsObject(
            "name" -> JsString("x"),
            "index" -> JsNumber(0),
            "columnType" -> JsString("categorical"))),
        "categoricalMappings" -> JsObject(
          "x" -> JsArray(Vector("a", "b", "c").map(JsString(_)))
        )
      )
    )

    metadata.serializeToJson shouldBe expectedJson
  }

  "DataFrameMetadata with partial knowledge" should "be correctly serialized to json" in {
    val metadata: AbstractMetadata = DataFrameMetadata(false, false,
      Map("x" -> ColumnMetadata("x", None, None)),
      Map("x" -> CategoriesMapping(Seq("a", "b", "c"))))

    val expectedJson = JsObject(
      "type" -> JsString("DataFrameMetadata"),
      "content" -> JsObject(
        "isExact" -> JsBoolean(false),
        "isColumnCountExact" -> JsBoolean(false),
        "columns" -> JsObject(
          "x" -> JsObject(
            "name" -> JsString("x"),
            "index" -> JsNull,
            "columnType" -> JsNull)),
        "categoricalMappings" -> JsObject(
          "x" -> JsArray(Vector("a", "b", "c").map(JsString(_)))
        )
      )
    )

    metadata.serializeToJson shouldBe expectedJson
  }

}
