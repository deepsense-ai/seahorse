/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations

import java.util.UUID

import ai.deepsense.deeplang.UnitSpec

class WriteDatasourceSpec extends UnitSpec {

  "WriteDatasource.getDatasourcesId" should {
    "return declared datasources" when {
      "datasource param is defined" in {
        val someDatasourceId = UUID.randomUUID()
        val wds = WriteDatasource().setDatasourceId(someDatasourceId)
        wds.getDatasourcesIds shouldBe Set(someDatasourceId)
      }
    }
    "return empty set" when {
      "datasource param is not defined" in {
        val wds = ReadDatasource()
        wds.getDatasourcesIds shouldBe empty
      }
    }
  }
}
