/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.models.workflows

import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import io.deepsense.commons.models.Entity
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.reportlib.model.ReportContent

class EntitiesMapSpec
  extends WordSpec
  with Matchers
  with MockitoSugar {

  "EntitiesMap" should {
    "be correctly created from results and reports" when {

      val entity1Id = Entity.Id.randomId
      val doperable1 = new DataFrame()
      val report1 = mock[ReportContent]

      val entity2Id = Entity.Id.randomId
      val doperable2 = new DataFrame()

      val results = Map(entity1Id -> doperable1, entity2Id -> doperable2)
      val reports = Map(entity1Id -> report1)

      EntitiesMap(results, reports) shouldBe EntitiesMap(Map(
        entity1Id -> EntitiesMap.Entry(
          "io.deepsense.deeplang.doperables.dataframe.DataFrame", Some(report1)),
        entity2Id -> EntitiesMap.Entry(
          "io.deepsense.deeplang.doperables.dataframe.DataFrame", None)
      ))
    }
  }
}
