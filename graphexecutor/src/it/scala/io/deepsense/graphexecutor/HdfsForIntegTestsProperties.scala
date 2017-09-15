/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import com.typesafe.config.ConfigFactory

object HdfsForIntegTestsProperties {

  private lazy val config = ConfigFactory.load("testEnvironment.conf")

  lazy val MasterHostname = config.getString("master.hostname")

  lazy val MasterIp = config.getString("master.ip")

  lazy val HdfsNameNodePort = config.getString("hdfs.name.node.port")
}
