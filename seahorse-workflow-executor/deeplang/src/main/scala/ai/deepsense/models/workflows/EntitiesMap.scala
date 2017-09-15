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

package ai.deepsense.models.workflows

import ai.deepsense.commons.models.Entity
import ai.deepsense.deeplang.DOperable
import ai.deepsense.reportlib.model.ReportContent

/**
 * Wraps a map of entities created during workflow execution.
 * It maps an entity id into a pair of its class name and report.
 */
case class EntitiesMap(entities: Map[Entity.Id, EntitiesMap.Entry] = Map()) {

  def subMap(keys: Set[Entity.Id]): EntitiesMap = {
    EntitiesMap(keys.intersect(entities.keySet).map(key => key -> entities(key)).toMap)
  }
}

object EntitiesMap {
  case class Entry(className: String, report: Option[ReportContent] = None)

  def apply(
      results: Map[Entity.Id, DOperable],
      reports: Map[Entity.Id, ReportContent]): EntitiesMap = {
    EntitiesMap(results.map { case (id, entity) =>
      val entry = EntitiesMap.Entry(
        entity.getClass.getCanonicalName,
        reports.get(id))
      (id, entry)
    })
  }
}
