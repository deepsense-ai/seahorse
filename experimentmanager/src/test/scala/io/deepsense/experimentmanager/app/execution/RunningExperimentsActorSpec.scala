/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.execution

import io.deepsense.experimentmanager.{StandardSpec, UnitTestSupport}

class RunningExperimentsActorSpec extends StandardSpec with UnitTestSupport {

  "RunningExperimentsActor" should {
    "launch experiment" when {
      "received Launch" in pending
    }
    "abort experiment" when {
      "received Abort" in pending
    }
    "send status" when {
      "received GetStatus" in pending
    }
    "list experiments of tenant" when {
      "received ListExperiments with tenantId" in pending
    }
    "list experiments of all tenants" when {
      "received ListExperiments without tenantId" in pending
    }
  }
}
