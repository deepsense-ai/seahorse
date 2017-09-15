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

package ai.deepsense.seahorse.datasource.server

import java.util.UUID

import org.scalatest.{FreeSpec, Matchers}
import spray.http.StatusCodes

import ai.deepsense.seahorse.datasource.api.{ApiException, ApiExceptionWithJsonBody}
import ai.deepsense.seahorse.datasource.db.dbio.Get
import ai.deepsense.seahorse.datasource.model.{Error, Visibility}

class DatasourcesApiSpec extends FreeSpec with Matchers {

  private implicit lazy val api = ApiForTests.api

  "Api consumer" - {
    val userId = UUID.randomUUID()
    val userName = "Alice"

    "cannot see private datasources that don't belong to him" in {
      val otherUserId = UUID.randomUUID()
      for {
        dsParams <- TestData.someDatasources(Some(Visibility.privateVisibility))
      } {
        val id = UUID.randomUUID()
        api.putDatasourceImpl(otherUserId, userName, id, dsParams)

        (the[ApiException] thrownBy api.getDatasourceImpl(userId, id)).errorCode shouldBe StatusCodes.Forbidden.intValue
      }

    }

    "can manage his datasources" in {
      for (dsParams <- TestData.someDatasources()) {
        val id = UUID.randomUUID()
        info("User can add datasource")
        api.putDatasourceImpl(userId, userName, id, dsParams)
        api.getDatasourcesImpl(userId).find(_.id == id).get.params shouldEqual dsParams
        api.getDatasourceImpl(userId, id).params shouldEqual dsParams

        info("Add operation is idempotent")
        api.putDatasourceImpl(userId, userName, id, dsParams)
        api.getDatasourcesImpl(userId).find(_.id == id).get.params shouldEqual dsParams
        api.getDatasourceImpl(userId, id).params shouldEqual dsParams

        info("User can also delete datasource")
        api.deleteDatasourceImpl(userId, id)
        api.getDatasourcesImpl(userId).find(_.id == id) shouldBe empty

        info("Once datasource not exists all operations yield 404")
        the[ApiException].thrownBy(
          api.getDatasourceImpl(userId, id)
        ).errorCode shouldBe 404
        the[ApiException].thrownBy(
          api.deleteDatasourceImpl(userId, id)
        ).errorCode shouldBe 404
      }
    }
    "cannot add datasource with multichar separator" in {
      val id = UUID.randomUUID()
      val putDatasourceExpectedError =
        Error(code = 400, message = "CSV custom separator should be single character")
      val putDatasourceException = intercept[ApiExceptionWithJsonBody] {
        api.putDatasourceImpl(userId, userName, id, TestData.multicharSeparatorLibraryCsvDatasource)
      }
      putDatasourceException.error shouldBe putDatasourceExpectedError
      info("Operation yields 404 for wrong datasource")
      the[ApiException].thrownBy(
        api.getDatasourceImpl(userId, id)
      ).errorCode shouldBe 404
    }
  }

  "Privileged api consumer" - {
    val userId = UUID.randomUUID()
    val userName = "Alice"
    val privilegedUserId = Get.privilegedUsers.head

    "can Get private data sources not owned by him" in {
      for (dsParams <- TestData.someDatasources(Some(Visibility.privateVisibility))) {
        val id = UUID.randomUUID()
        api.putDatasourceImpl(userId, userName, id, dsParams)
        api.getDatasourceImpl(privilegedUserId, id)
      }
    }
  }

}
