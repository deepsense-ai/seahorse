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

import scala.reflect.runtime.universe.Type

import spray.httpx.SprayJsonSupport
import spray.json._

import ai.deepsense.commons.json.{EnumerationSerializer, IdJsonProtocol}
import ai.deepsense.deeplang.DPortPosition._
import ai.deepsense.deeplang.catalogs.doperations.DOperationDescriptor
import ai.deepsense.deeplang.{DPortPosition, TypeUtils}

/**
 * Exposes various json formats of DOperationDescription.
 * Reading from json is not supported.
 */
trait DOperationDescriptorJsonProtocol
  extends DefaultJsonProtocol
  with IdJsonProtocol
  with SprayJsonSupport {

  class DOperationDescriptorShortFormat extends RootJsonFormat[DOperationDescriptor] {
    override def write(obj: DOperationDescriptor): JsValue = {
      JsObject(
        "id" -> obj.id.toJson,
        "name" -> obj.name.toJson,
        "description" -> obj.description.toJson)
    }

    override def read(json: JsValue): DOperationDescriptor = {
      throw new UnsupportedOperationException
    }
  }

  /**
   * Only id and name of operation.
   */
  object DOperationDescriptorShortFormat extends DOperationDescriptorShortFormat

  class DOperationDescriptorBaseFormat extends DOperationDescriptorShortFormat {
    override def write(obj: DOperationDescriptor): JsValue = {
      JsObject(super.write(obj).asJsObject.fields ++ Map(
        "category" -> obj.category.id.toJson,
        "description" -> obj.description.toJson,
        "deterministic" -> false.toJson,  // TODO use real value as soon as it is supported
        "hasDocumentation" -> obj.hasDocumentation.toJson,
        "ports" -> JsObject(
          "input" -> portTypesToJson(obj.inPorts, addRequiredField = true, obj.inPortsLayout),
          "output" -> portTypesToJson(obj.outPorts, addRequiredField = false, obj.outPortsLayout)
        )
      ))
    }

    private def portTypesToJson(
      portTypes: Seq[Type],
      addRequiredField: Boolean,
      portsLayout: Vector[DPortPosition]): JsValue = {
      val required = if (addRequiredField) Some(true) else None
      // TODO use real value as soon as it is supported

      val fields = for (
        (portType, positioning, index) <- (portTypes, portsLayout, Stream.from(0)).zipped)
        yield portToJson(index, required, portType, positioning)

      fields.toList.toJson
    }

    private implicit val positionSerializer = EnumerationSerializer.jsonEnumFormat(DPortPosition)

    private def portToJson(
      index: Int,
      required: Option[Boolean],
      portType: Type,
      position: DPortPosition): JsValue = {
      val fields = Map(
        "portIndex" -> index.toJson,
        "typeQualifier" -> TypeUtils.describeType(portType).toJson,
        "portPosition" -> position.toJson
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
  object DOperationDescriptorBaseFormat extends DOperationDescriptorBaseFormat

  /**
   * Full operation's info.
   */
  object DOperationDescriptorFullFormat extends DOperationDescriptorBaseFormat {
    override def write(obj: DOperationDescriptor): JsValue = {
      JsObject(super.write(obj).asJsObject.fields.updated(
        "parameters", obj.parametersJsonDescription))
    }
  }
}

object DOperationDescriptorJsonProtocol extends DOperationDescriptorJsonProtocol
