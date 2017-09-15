/**
 * Copyright (c) 2015, CodiLime Inc.
 */
package io.deepsense.experimentmanager.rest.json

import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoriesMapping
import io.deepsense.deeplang.doperables.dataframe.{ColumnMetadata, DataFrameMetadata}
import io.deepsense.deeplang.parameters.ColumnType
import org.scalatest.mock.MockitoSugar
import org.scalatest._
import spray.json._

class DataFrameMetadataJsonProtocolSpec extends FlatSpec with Matchers with MockitoSugar {

  "DataFrameMetadata with full knowledge" should "be correctly serialized to json" in {
    val metadata = DataFrameMetadata(true, true,
      Seq(ColumnMetadata(Some("x"), Some(ColumnType.categorical))),
      Map(0 -> CategoriesMapping(Seq("a", "b", "c"))))
    val serializingVisitor = new MetadataJsonSerializingVisitor

    val expectedJson = JsObject(
      "type" -> JsString("DataFrameMetadata"),
      "content" -> JsObject(
        "isExact" -> JsBoolean(true),
        "isColumnCountExact" -> JsBoolean(true),
        "columns" -> JsArray(
          JsObject(
            "name" -> JsString("x"),
            "columnType" -> JsString("categorical"))),
        "categoricalMappings" -> JsObject(
          "0" -> JsArray(Vector("a", "b", "c").map(JsString(_)))
        )
      )
    )

    serializingVisitor.visit(metadata) shouldBe expectedJson
  }

  "DataFrameMetadata with partial knowledge" should "be correctly serialized to json" in {
    val metadata = DataFrameMetadata(false, false,
      Seq(ColumnMetadata(None, None)),
      Map(0 -> CategoriesMapping(Seq("a", "b", "c"))))
    val serializingVisitor = new MetadataJsonSerializingVisitor

    val expectedJson = JsObject(
      "type" -> JsString("DataFrameMetadata"),
      "content" -> JsObject(
        "isExact" -> JsBoolean(false),
        "isColumnCountExact" -> JsBoolean(false),
        "columns" -> JsArray(
          JsObject(
            "name" -> JsNull,
            "columnType" -> JsNull)),
        "categoricalMappings" -> JsObject(
          "0" -> JsArray(Vector("a", "b", "c").map(JsString(_)))
        )
      )
    )

    serializingVisitor.visit(metadata) shouldBe expectedJson
  }

}
