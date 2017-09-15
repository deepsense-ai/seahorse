/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest.json

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.catalogs.doperations.{DOperationCategory, DOperationCategoryNode, DOperationDescriptor}
import io.deepsense.model.json.workflow.DOperationCategoryNodeJsonProtocol
import DOperationCategoryNodeJsonProtocol._

class DOperationCategoryNodeJsonProtocolSpec extends FlatSpec with Matchers with MockitoSugar {

  "DOperationCategoryNode" should "be correctly serialized to json" in {
    val childCategory =
      new DOperationCategory(DOperationCategory.Id.randomId, "mock child name", None) {}
    val childNode = DOperationCategoryNode(Some(childCategory))

    val operationDescriptor = mock[DOperationDescriptor]
    when(operationDescriptor.id) thenReturn DOperation.Id.randomId
    when(operationDescriptor.name) thenReturn "mock operation descriptor name"

    val node = DOperationCategoryNode(
      None,
      successors = Map(childCategory -> childNode),
      operations = Set(operationDescriptor))

    val expectedJson = JsObject(
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
