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

import org.mockito.Matchers._
import org.mockito.Mockito._

import ai.deepsense.commons.datasource.DatasourceTestData
import ai.deepsense.commons.rest.client.datasources.DatasourceClient
import ai.deepsense.deeplang.exceptions.DeepLangMultiException
import ai.deepsense.deeplang.inference.InferContext
import ai.deepsense.deeplang.{DKnowledge, DOperable, UnitSpec}

class ReadDatasourceSpec extends UnitSpec {

  "ReadDatasource.getDatasourcesId" should {
    "return declared datasources" when {
      "datasource param is defined" in {
        val someDatasourceId = UUID.randomUUID()
        val rds = ReadDatasource().setDatasourceId(someDatasourceId)
        rds.getDatasourcesIds shouldBe Set(someDatasourceId)
      }
    }
    "return empty set" when {
      "datasource param is not defined" in {
        val rds = ReadDatasource()
        rds.getDatasourcesIds shouldBe empty
      }
    }
  }

  "ReadDatasource.inferKnowledge" should {
    "throw DeepLangMultiException" when {
      "separator contains more than two chars" in {
        val context = mock[InferContext]
        val datasourceClient = mock[DatasourceClient]
        when(context.datasourceClient).thenReturn(datasourceClient)
        val ds = DatasourceTestData.multicharSeparatorLibraryCsvDatasource
        when(datasourceClient.getDatasource(any())).thenReturn(Some(ds))
        val readDatasource = ReadDatasource()
        readDatasource.setDatasourceId(UUID.randomUUID)
        val multilangException = intercept[DeepLangMultiException] {
          readDatasource.inferKnowledgeUntyped(Vector.empty[DKnowledge[DOperable]])(context)
        }
        multilangException.exceptions(0).message shouldBe
            "Parameter value `,,` does not match regex `.`."
      }
    }
  }

}
