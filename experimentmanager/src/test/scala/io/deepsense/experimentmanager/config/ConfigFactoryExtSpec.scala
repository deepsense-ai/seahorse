/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.config

import com.typesafe.config.ConfigFactory
import org.scalatest.{FunSpec, Matchers}

class ConfigFactoryExtSpec extends FunSpec with Matchers {
  describe ("-Denv=test specified") {

    // Specify test environment to load test.conf
    System.setProperty("env", "test")
    ConfigFactoryExt.enableEnvOverride()

    // System property should override value in config file
    System.setProperty("overridden.by.system.prop", "system")

    ConfigFactory.invalidateCaches()
    val config = ConfigFactory.load

    it ("Value in test.conf should override value in application.conf") {
      config.getString("defined.in.all.confs") should equal ("test")
    }

    it ("Value only in test.conf should be defined") {
      config.getString("defined.in.test.conf") should equal ("test")
    }

    it ("Value only in application.conf should be defined") {
      config.getString("defined.in.application.conf") should equal ("application")
    }

    it ("System property should override value in files") {
      config.getString("overridden.by.system.prop") should equal ("system")
    }

    it ("test.conf value gets substituted into application.conf property value") {
      config.getString("server.url") should equal ("https://test.crowdpac.com")
    }
  }
}
