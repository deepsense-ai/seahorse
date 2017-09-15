/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.experimentmanager.app.rest.json

import scala.reflect.runtime.universe.typeOf

import java.util.UUID

import org.mockito.Mockito._
import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.mock.MockitoSugar
import spray.json._

import io.deepsense.deeplang.catalogs.doperations.{DOperationCategory, DOperationDescriptor}
import io.deepsense.deeplang.parameters.ParametersSchema

class DOperationDescriptorJsonProtocolSpec
  extends FlatSpec
  with MockitoSugar
  with Matchers
  with DOperationDescriptorJsonProtocol {

  object HelperTypes {
    class A
    class B
    trait T1
    trait T2
  }

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

    import HelperTypes._

    val category = mock[DOperationCategory]
    when(category.id) thenReturn UUID.randomUUID

    val parameters = mock[ParametersSchema]
    val parametersJsRepresentation = JsString("Mock parameters representation")
    when(parameters.toJson) thenReturn parametersJsRepresentation

    val operationDescriptor = DOperationDescriptor(
      UUID.randomUUID, "operation name", "operation description", category, parameters,
      Seq(typeOf[A], typeOf[A with T1]), Seq(typeOf[B], typeOf[B with T2]))

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
            "typeQualifier" -> JsArray(JsString("A"))),
          JsObject(
            "portIndex" -> JsNumber(1),
            "required" -> JsBoolean(true),
            "typeQualifier" -> JsArray(JsString("A"), JsString("T1")))
        ),
        "output" -> JsArray(
          JsObject(
            "portIndex" -> JsNumber(0),
            "typeQualifier" -> JsArray(JsString("B"))),
          JsObject(
            "portIndex" -> JsNumber(1),
            "typeQualifier" -> JsArray(JsString("B"), JsString("T2")))
        )
      )
    )

    (operationDescriptor, expectedJson)
  }
}
