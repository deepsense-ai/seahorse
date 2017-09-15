/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.commons.service.server

import java.time.Duration

import com.typesafe.config.Config

case class JettyConfig(
    port: Int,
    stopTimeout: Duration,
    connectorIdleTimeout: Duration,
    maxFormContentSize: Int)

object JettyConfig {
  def apply(jettyConfig: Config): JettyConfig = new JettyConfig(
    port = jettyConfig.getInt("port"),
    stopTimeout = jettyConfig.getDuration("stopTimeout"),
    connectorIdleTimeout = jettyConfig.getDuration("connectorIdleTimeout"),
    maxFormContentSize = jettyConfig.getInt("maxFormContentSize"))

}
