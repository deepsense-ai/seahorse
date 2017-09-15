/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.execution

import scala.util.{Failure, Success, Try}

import com.google.inject.AbstractModule
import com.typesafe.config.ConfigFactory

class ExecutionModule extends AbstractModule {
  override def configure(): Unit = {
    val config = ConfigFactory.load
    Try(config.getBoolean("runningexperiments.override.with.mock")) match {
      case Failure(exception) => new RunningExperimentsActorModule
      case Success(value) => if (value) {
        install(new MockRunningExperimentsActorModule)
      } else {
        install(new RunningExperimentsActorModule)
      }
    }
  }
}
