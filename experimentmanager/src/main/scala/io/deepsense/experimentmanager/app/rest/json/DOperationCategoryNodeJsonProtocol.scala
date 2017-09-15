/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.experimentmanager.app.rest.json

import spray.json.{DefaultJsonProtocol, JsObject, JsValue, JsonFormat}
import spray.json._

import io.deepsense.deeplang.catalogs.doperations.DOperationCategoryNode

object DOperationCategoryNodeJsonProtocol extends DefaultJsonProtocol {

  implicit object DOperationCategoryNodeFormat extends JsonFormat[DOperationCategoryNode] {
    private implicit val operationFormat = DOperationDescriptorJsonProtocol.ShortFormat

    override def write(obj: DOperationCategoryNode): JsValue = {
      JsObject(
        "id" -> obj.category.get.id.toString.toJson,
        "name" -> obj.category.get.name.toJson,
        "items" -> obj.operations.toJson,
        "catalog" -> obj.successors.values.toJson)
    }

    override def read(json: JsValue): DOperationCategoryNode = {
      throw new UnsupportedOperationException
    }
  }
}
