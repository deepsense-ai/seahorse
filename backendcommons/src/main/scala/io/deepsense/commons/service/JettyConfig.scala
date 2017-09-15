/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.commons.service

import com.typesafe.config.Config

class JettyConfig(jettyConfig: Config) {

  val port = jettyConfig.getInt("port")
  val stopTimeout = jettyConfig.getDuration("stopTimeout")
  val connectorIdleTimeout = jettyConfig.getDuration("connectorIdleTimeout")
  val maxFormContentSize = jettyConfig.getInt("maxFormContentSize")

}
