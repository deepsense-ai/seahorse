/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import java.util.UUID

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner.SessionConfig

object TestData {

  def someUserId(): String = UUID.randomUUID().toString

  def someSessionConfig(): SessionConfig = SessionConfig(
    UUID.randomUUID(), someUserId()
  )

  lazy val someClusterDetails = ClusterDetails(
    clusterType = "mesos",
    id = Some(1),
    name = "some-name",
    uri = "localhost",
    userIP = "127.0.0.1"
  )

  lazy val localClusterDetails = ClusterDetails(
    clusterType = "local",
    id = Some(2),
    name = "some-other-name",
    uri = "uri-doesnt-matter",
    userIP = "127.0.0.1"
  )

}