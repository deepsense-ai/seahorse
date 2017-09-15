/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.server

import io.deepsense.seahorse.datasource.api.DatasourceManagerApi
import io.deepsense.seahorse.datasource.db.FlywayMigration

object ApiForTests {

  implicit lazy val api = {
    FlywayMigration.run()
    new DatasourceManagerApi()
  }

}
