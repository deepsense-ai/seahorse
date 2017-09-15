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

import scala.reflect.runtime.universe.{TypeTag, typeOf}

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.catalogs.doperations.{DOperationCategory, DOperationDescriptor}
import io.deepsense.deeplang.params.Params

object HelperTypes {
  class A
  class B
  trait T1
  trait T2
}

class DOperationDescriptorJsonProtocolSpec
  extends FlatSpec
  with MockitoSugar
  with Matchers
  with DOperationDescriptorJsonProtocol {

  "DOperationDescriptor" should "be correctly serialized to json" in {
    val (operationDescriptor, expectedJson) = operationDescriptorWithExpectedJsRepresentation
    operationDescriptor.toJson(DOperationDescriptorFullFormat) shouldBe expectedJson
  }

  it should "be correctly serialized to json omitting its parameters" in {
    val (operationDescriptor, expectedJson) = operationDescriptorWithExpectedJsRepresentation
    val jsonWithoutParameters = JsObject(expectedJson.asJsObject.fields - "parameters")
    operationDescriptor.toJson(DOperationDescriptorBaseFormat) shouldBe jsonWithoutParameters
  }

  private[this] def operationDescriptorWithExpectedJsRepresentation:
  (DOperationDescriptor, JsValue) = {

    import io.deepsense.models.json.workflow.HelperTypes._

    val category = mock[DOperationCategory]
    when(category.id) thenReturn DOperationCategory.Id.randomId

    val parameters = mock[Params]
    val parametersJsRepresentation = JsString("Mock parameters representation")
    when(parameters.paramsToJson) thenReturn parametersJsRepresentation

    val operationDescriptor = DOperationDescriptor(
      DOperation.Id.randomId,
      "operation name",
      "operation description",
      category,
      parameters.paramsToJson,
      Seq(typeOf[A], typeOf[A with T1]),
      Seq(typeOf[B], typeOf[B with T2]))

    def name[T: TypeTag]: String = typeOf[T].typeSymbol.fullName

    val expectedJson = JsObject(
      "id" -> JsString(operationDescriptor.id.toString),
      "name" -> JsString(operationDescriptor.name),
      "category" -> JsString(category.id.toString),
      "description" -> JsString(operationDescriptor.description),
      "deterministic" -> JsBoolean(false),
      "parameters" -> parametersJsRepresentation,
      "ports" -> JsObject(
        "input" -> JsArray(
          JsObject(
            "portIndex" -> JsNumber(0),
            "required" -> JsBoolean(true),
            "typeQualifier" -> JsArray(JsString(name[A]))),
          JsObject(
            "portIndex" -> JsNumber(1),
            "required" -> JsBoolean(true),
            "typeQualifier" -> JsArray(JsString(name[A]), JsString(name[T1])))
        ),
        "output" -> JsArray(
          JsObject(
            "portIndex" -> JsNumber(0),
            "typeQualifier" -> JsArray(JsString(name[B]))),
          JsObject(
            "portIndex" -> JsNumber(1),
            "typeQualifier" -> JsArray(JsString(name[B]), JsString(name[T2])))
        )
      )
    )

    (operationDescriptor, expectedJson)
  }
}
