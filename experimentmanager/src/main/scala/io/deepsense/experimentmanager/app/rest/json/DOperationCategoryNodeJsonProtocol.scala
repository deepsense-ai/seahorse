/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.experimentmanager.app.rest.json

import spray.json.{DefaultJsonProtocol, JsObject, JsValue}
import spray.json._

import io.deepsense.deeplang.catalogs.doperations.DOperationCategoryNode

trait DOperationCategoryNodeJsonProtocol extends DefaultJsonProtocol with UUIDJsonProtocol {

  implicit object DOperationCategoryNodeFormat extends RootJsonFormat[DOperationCategoryNode] {
    private implicit val operationFormat = DOperationDescriptorJsonProtocol.ShortFormat

    override def write(obj: DOperationCategoryNode): JsValue = {
      val fields = Map(
        "items" -> obj.operations.toJson,
        "catalog" -> obj.successors.values.toJson)
      val allFields = obj.category match {
        case Some(value) => fields ++ Map(
          "id" -> obj.category.get.id.toJson,
          "name" -> obj.category.get.name.toJson)
        case None => fields
      }
      JsObject(allFields)
    }

    override def read(json: JsValue): DOperationCategoryNode = {
      throw new UnsupportedOperationException
    }
  }
}

object DOperationCategoryNodeJsonProtocol extends DOperationCategoryNodeJsonProtocol
