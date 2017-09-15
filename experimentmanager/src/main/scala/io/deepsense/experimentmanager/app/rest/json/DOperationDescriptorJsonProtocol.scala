/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.experimentmanager.app.rest.json

import scala.reflect.runtime.universe.Type

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.deeplang.catalogs.doperations.DOperationDescriptor

/**
 * Exposes various json formats of DOperationDescription.
 * Reading from json is not supported.
 */
object DOperationDescriptorJsonProtocol
  extends DefaultJsonProtocol
  with UUIDJsonProtocol
  with SprayJsonSupport {

  class ShortFormat extends RootJsonFormat[DOperationDescriptor] {
    override def write(obj: DOperationDescriptor): JsValue = {
      JsObject(
        "id" -> obj.id.toJson,
        "name" -> obj.name.toJson)
    }

    override def read(json: JsValue): DOperationDescriptor = {
      throw new UnsupportedOperationException
    }
  }

  /**
   * Only id and name of operation.
   */
  object ShortFormat extends ShortFormat

  class BaseFormat extends ShortFormat {
    override def write(obj: DOperationDescriptor): JsValue = {
      JsObject(super.write(obj).asJsObject.fields ++ Map(
        "category" -> obj.category.id.toJson,
        "description" -> obj.description.toJson,
        "deterministic" -> false.toJson,  // TODO use real value as soon as it is supported
        "ports" -> JsObject(
          "input" -> portTypesToJson(obj.inPorts, addRequiredField = true),
          "output" -> portTypesToJson(obj.outPorts, addRequiredField = false)
        )
      ))
    }

    private def portTypesToJson(portTypes: Seq[Type], addRequiredField: Boolean): JsValue = {
      val required = if (addRequiredField) Some(true) else None
      // TODO use real value as soon as it is supported

      val fields = for ((portType, index) <- portTypes.zipWithIndex)
      yield portToJson(index, required, portType)
      fields.toJson
    }

    private def portToJson(index: Int, required: Option[Boolean], portType: Type): JsValue = {
      val fields = Map(
        "portIndex" -> index.toJson,
        "typeQualifier" -> DOperationDescriptor.describeType(portType).toJson
      )
      JsObject(required match {
        case Some(value) => fields.updated("required", value.toJson)
        case None => fields
      })
    }
  }

  /**
   * All operation's info except for parameters.
   */
  object BaseFormat extends BaseFormat

  /**
   * Full operation's info.
   */
  object FullFormat extends BaseFormat {
    override def write(obj: DOperationDescriptor): JsValue = {
      JsObject(super.write(obj).asJsObject.fields.updated("parameters", obj.parameters.toJson))
    }
  }
}
