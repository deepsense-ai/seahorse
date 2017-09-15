/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.config

import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule

class TestModule(config: Config) extends ScalaModule {
  def configure(): Unit = {
    bind[TestInjectable]
    install(new ConfigModule {
      override def loadConfig() = {
        config
      }
    })
  }
}


