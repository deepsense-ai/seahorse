/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import io.deepsense.sessionmanager.rest.requests.ClusterDetails

object TestData {

  lazy val someClusterDetails = ClusterDetails(
    clusterType = "mesos",
    id = "some-id",
    name = "some-name",
    uri = "localhost",
    userIP = "127.0.0.1"
  )

}
