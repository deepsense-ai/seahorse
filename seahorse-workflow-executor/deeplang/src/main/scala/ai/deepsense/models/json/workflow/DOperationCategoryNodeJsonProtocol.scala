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

import spray.json._

import ai.deepsense.commons.json.IdJsonProtocol
import ai.deepsense.deeplang.catalogs.doperations.DOperationCategoryNode

trait DOperationCategoryNodeJsonProtocol
  extends DefaultJsonProtocol
  with IdJsonProtocol
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
