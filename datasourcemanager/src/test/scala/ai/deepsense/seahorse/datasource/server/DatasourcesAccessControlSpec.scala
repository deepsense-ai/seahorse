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

import ai.deepsense.seahorse.datasource.api.{ApiException, DatasourceManagerApi}
import ai.deepsense.seahorse.datasource.db.FlywayMigration
import ai.deepsense.seahorse.datasource.model.{AccessLevel, Visibility}

class DatasourcesAccessControlSpec extends FreeSpec with Matchers {

  lazy val api = ApiForTests.api

  "Api consumer is a subject to access control" in {
    val testEnvironment = createTestEnviroment()
    import testEnvironment._
    {
      info("Alice can access all her datasources")
      val datasources = api.getDatasourcesImpl(alice)
      val datasourcesIds = datasources.map(_.id)
      datasourcesIds should contain(alicesPublicDs)
      datasourcesIds should contain(alicesPrivateDs)

      info("Alice has READ_WRITE access to her datasources")
      datasources.find(_.id == alicesPublicDs).get.accessLevel shouldEqual AccessLevel.writeRead
      datasources.find(_.id == alicesPrivateDs).get.accessLevel shouldEqual AccessLevel.writeRead
    }
    {
      info("Bob can see only public datasources of other users")
      val datasources = api.getDatasourcesImpl(bob)
      val datasourcesIds = datasources.map(_.id)
      datasourcesIds should contain(alicesPublicDs)
      datasourcesIds shouldNot contain(alicesPrivateDs)

      info("Bob has only READ access to other users datasources")
      datasources.find(_.id == alicesPublicDs).get.accessLevel shouldEqual AccessLevel.read
      api.getDatasourceImpl(bob, alicesPublicDs).accessLevel shouldEqual AccessLevel.read
      api.getDatasourceImpl(bob, alicesPublicDs).ownerName shouldEqual aliceName
    }
    {
      info("Bob cannot hack Alices datasources")

      info("Bob cannot get private datasource")

      the[ApiException].thrownBy(
        api.getDatasourceImpl(bob, alicesPrivateDs)
      ).errorCode shouldBe forbiddenErrorCode

      info("Bob cannot edit Alices datasource")

      the[ApiException].thrownBy(
        api.putDatasourceImpl(bob, bobName, alicesPublicDs, TestData.someDatasource())
      ).errorCode shouldBe forbiddenErrorCode

      the[ApiException].thrownBy(
        api.putDatasourceImpl(bob, bobName, alicesPrivateDs, TestData.someDatasource())
      ).errorCode shouldBe forbiddenErrorCode

      info("Bob cannot delete Alices datasources")

      the[ApiException].thrownBy(
        api.deleteDatasourceImpl(bob, alicesPublicDs)
      ).errorCode shouldBe forbiddenErrorCode

      the[ApiException].thrownBy(
        api.deleteDatasourceImpl(bob, alicesPrivateDs)
      ).errorCode shouldBe forbiddenErrorCode
    }
  }

  private def createTestEnviroment() = new {
    val alice = UUID.randomUUID()
    val aliceName = "Alice"

    val alicesPublicDs = UUID.randomUUID()
    val alicesPrivateDs = UUID.randomUUID()
    api.putDatasourceImpl(alice, aliceName, alicesPublicDs, TestData.someDatasource().copy(
      visibility = Visibility.publicVisibility
    ))

    api.putDatasourceImpl(alice, aliceName, alicesPrivateDs, TestData.someDatasource().copy(
      visibility = Visibility.privateVisibility
    ))

    val bobName = "Bob"
    val bob = UUID.randomUUID()
  }

  private val forbiddenErrorCode = 403

}
