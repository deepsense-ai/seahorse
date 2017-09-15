/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.server

import org.scalatest.{FreeSpec, Matchers}

import io.deepsense.seahorse.datasource.api.DefaultApiImpl
import io.deepsense.seahorse.datasource.db.FlywayMigration

class DatasourcesApiSpec extends FreeSpec with Matchers {

  private lazy val api = {
    FlywayMigration.run()
    new DefaultApiImpl()
  }

  "Api consumer" - {
    "can add new datasources" in {
      for (ds <- TestData.someDatasources()) {
        api.putDatasourceImpl(ds.id, ds)
        api.getDatasourcesImpl() should contain (ds)

        info("Add operation is idempotent")
        api.putDatasourceImpl(ds.id, ds)
        api.getDatasourcesImpl() should contain (ds)

        info("Datasource can also be deleted")
        api.deleteDatasourceImpl(ds.id)
        api.getDatasourcesImpl() shouldNot contain (ds)
      }
    }
  }

}
