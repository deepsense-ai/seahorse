/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import com.typesafe.config.ConfigFactory

object HdfsForIntegTestsProperties {

  private lazy val config = ConfigFactory.load

  lazy val MasterHostname = config.getString("master.hostname")

  lazy val HdfsNameNodePort = config.getString("hdfs.name.node.port")
}
