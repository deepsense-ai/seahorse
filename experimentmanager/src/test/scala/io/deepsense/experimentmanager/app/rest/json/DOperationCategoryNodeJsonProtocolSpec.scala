/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.experimentmanager.app.rest.json

import java.util.UUID

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

import io.deepsense.deeplang.catalogs.doperations.{DOperationCategory, DOperationCategoryNode, DOperationDescriptor}
import io.deepsense.experimentmanager.app.rest.json.DOperationCategoryNodeJsonProtocol._

class DOperationCategoryNodeJsonProtocolSpec extends FlatSpec with Matchers with MockitoSugar {

  "DOperationCategoryNode" should "be correctly serialized to json" in {
    val category = new DOperationCategory(UUID.randomUUID(), "mock name", None) {}
    val childCategory = new DOperationCategory(UUID.randomUUID(), "mock child name", None) {}
    val childNode = DOperationCategoryNode(Some(childCategory))

    val operationDescriptor = mock[DOperationDescriptor]
    when(operationDescriptor.id) thenReturn UUID.randomUUID
    when(operationDescriptor.name) thenReturn "mock operation descriptor name"

    val node = DOperationCategoryNode(
      Some(category),
      successors = Map(childCategory -> childNode),
      operations = Set(operationDescriptor))

    val expectedJson = JsObject(
      "id" -> JsString(category.id.toString),
      "name" -> JsString(category.name),
      "catalog" -> JsArray(
        JsObject(
          "id" -> JsString(childCategory.id.toString),
          "name" -> JsString(childCategory.name),
          "catalog" -> JsArray(),
          "items" -> JsArray())
      ),
      "items" -> JsArray(
        JsObject(
          "id" -> JsString(operationDescriptor.id.toString),
          "name" -> JsString(operationDescriptor.name)
        )
      )
    )

    node.toJson shouldBe expectedJson
  }
}
