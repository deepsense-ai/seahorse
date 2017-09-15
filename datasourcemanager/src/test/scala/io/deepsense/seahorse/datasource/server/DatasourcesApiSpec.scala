/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.server

import java.util.UUID

import org.scalatest.{FreeSpec, Matchers}

import io.deepsense.seahorse.datasource.api.{ApiException, DatasourceManagerApi}
import io.deepsense.seahorse.datasource.db.FlywayMigration
import io.deepsense.seahorse.datasource.model.{AccessLevel, Datasource}

class DatasourcesApiSpec extends FreeSpec with Matchers {

  private implicit lazy val api = ApiForTests.api

  "Api consumer" - {
    val userId = UUID.randomUUID()
    val userName = "Alice"
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
  }

}
