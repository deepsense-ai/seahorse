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

package io.deepsense.models.json.workflow

import scala.collection.immutable.ListMap

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.catalogs.doperations.{DOperationCategory, DOperationCategoryNode, DOperationDescriptor}
import io.deepsense.models.json.workflow.DOperationCategoryNodeJsonProtocol._

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
      successors = ListMap(childCategory -> childNode),
      operations = List(operationDescriptor))

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
