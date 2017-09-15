/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest.json

import spray.json._

import io.deepsense.commons.json.UUIDJsonProtocol
import io.deepsense.deeplang.catalogs.doperations.DOperationCategoryNode

trait DOperationCategoryNodeJsonProtocol
  extends DefaultJsonProtocol
  with UUIDJsonProtocol
  with DOperationDescriptorJsonProtocol {

  implicit object DOperationCategoryNodeFormat extends RootJsonFormat[DOperationCategoryNode] {
    private implicit val operationFormat = DOperationDescriptorShortFormat

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
