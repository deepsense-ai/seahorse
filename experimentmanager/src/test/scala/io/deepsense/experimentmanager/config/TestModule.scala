/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.config

import com.google.inject.AbstractModule
import com.typesafe.config.Config

class TestModule(config: Config) extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[TestInjectable])
    install(new ConfigModule {
      override def loadConfig() = {
        config
      }
    })
  }
}


