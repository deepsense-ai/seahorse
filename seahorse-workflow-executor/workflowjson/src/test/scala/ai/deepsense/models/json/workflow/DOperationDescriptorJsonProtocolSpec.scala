/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.models.json.workflow

import scala.reflect.runtime.universe.{TypeTag, typeOf}

import ai.deepsense.deeplang.catalogs.SortPriority
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import ai.deepsense.deeplang.{DOperation, DPortPosition}
import ai.deepsense.deeplang.catalogs.doperations.{DOperationCategory, DOperationDescriptor}
import ai.deepsense.deeplang.params.Params

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

    import ai.deepsense.models.json.workflow.HelperTypes._

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
      SortPriority.coreDefault,
      hasDocumentation = false,
      parameters.paramsToJson,
      Seq(typeOf[A], typeOf[A with T1]),
      Vector(DPortPosition.Left, DPortPosition.Center),
      Seq(typeOf[B], typeOf[B with T2]),
      Vector(DPortPosition.Right, DPortPosition.Center))

    def name[T: TypeTag]: String = typeOf[T].typeSymbol.fullName

    val expectedJson = JsObject(
      "id" -> JsString(operationDescriptor.id.toString),
      "name" -> JsString(operationDescriptor.name),
      "category" -> JsString(category.id.toString),
      "description" -> JsString(operationDescriptor.description),
      "deterministic" -> JsBoolean(false),
      "hasDocumentation" -> JsBoolean(false),
      "parameters" -> parametersJsRepresentation,
      "ports" -> JsObject(
        "input" -> JsArray(
          JsObject(
            "portIndex" -> JsNumber(0),
            "required" -> JsBoolean(true),
            "typeQualifier" -> JsArray(JsString(name[A])),
            "portPosition" -> JsString("left")
          ),
          JsObject(
            "portIndex" -> JsNumber(1),
            "required" -> JsBoolean(true),
            "typeQualifier" -> JsArray(JsString(name[A]), JsString(name[T1])),
            "portPosition" -> JsString("center")
          )
        ),
        "output" -> JsArray(
          JsObject(
            "portIndex" -> JsNumber(0),
            "typeQualifier" -> JsArray(JsString(name[B])),
            "portPosition" -> JsString("right")
          ),
          JsObject(
            "portIndex" -> JsNumber(1),
            "typeQualifier" -> JsArray(JsString(name[B]), JsString(name[T2])),
            "portPosition" -> JsString("center")
          )
        )
      )
    )

    (operationDescriptor, expectedJson)
  }
}
