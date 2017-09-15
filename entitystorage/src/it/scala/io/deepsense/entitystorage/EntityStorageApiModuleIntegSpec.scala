/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.entitystorage

import io.deepsense.commons.StandardSpec

class EntityStorageApiModuleIntegSpec
  extends StandardSpec
  with EntityStorageIntegTestSupport
  with CassandraTestSupport {
  "EntityStorage guice module" should {
    "create rest service instance" in {
      getRestServiceInstance should not be null
    }
  }
}
