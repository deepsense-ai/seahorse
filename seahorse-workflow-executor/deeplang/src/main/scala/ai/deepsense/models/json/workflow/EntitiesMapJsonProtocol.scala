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
import ai.deepsense.commons.models.Entity
import ai.deepsense.models.workflows.EntitiesMap
import ai.deepsense.reportlib.model.ReportJsonProtocol

trait EntitiesMapJsonProtocol extends IdJsonProtocol {
  import ReportJsonProtocol._

  implicit val entitiesMapEntryFormat = jsonFormat2(EntitiesMap.Entry)

  implicit val entitiesMapFormat = new JsonFormat[EntitiesMap] {
    override def write(obj: EntitiesMap): JsValue = {
      obj.entities.toJson
    }

    override def read(json: JsValue): EntitiesMap = {
      val jsObject = json.asJsObject
      val entities = jsObject.fields.map { case (key, value) =>
        val id = Entity.Id.fromString(key)
        val entry = value.convertTo[EntitiesMap.Entry]
        (id, entry)
      }
      EntitiesMap(entities)
    }
  }
}

object EntitiesMapJsonProtocol extends EntitiesMapJsonProtocol
