/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import java.util.UUID

import io.deepsense.sessionmanager.rest.requests.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner.SessionConfig

object TestData {

  def someUserId(): String = UUID.randomUUID().toString

  def someSessionConfig(): SessionConfig = SessionConfig(
    UUID.randomUUID(), someUserId()
  )

  lazy val someClusterDetails = ClusterDetails(
    clusterType = "mesos",
    id = "some-id",
    name = "some-name",
    uri = "localhost",
    userIP = "127.0.0.1"
  )

}
