/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.commons.akka

import scala.concurrent.ExecutionContext
import scala.util.Try

import akka.actor.{ActorRefFactory, ActorSystem}
import com.google.inject
import com.google.inject.{AbstractModule, Injector, Provider, Provides}
import com.typesafe.config.{Config, ConfigException}

/**
 * This module defines the bindings required to support Guice injectable Akka actors.
 * It is a core module required to bootstrap the spray router for rest support.
 *
 * This includes:
 *
 * - ActorSystem      - a singleton instance of the root actor system
 * - ActorRefFactory  - the same instance bound as a ActorRefFactory.  (Guice will
 *                      only inject exact type matches, so we must bind the
 *                      actor system to ActorRefFactory even though ActorSystem
 *                      extends ActorRefFactory).
 * - ExecutionContext - a singleton instance of the execution context provided
 *                      by the root actor system.
 *
 * Taken from: https://github.com/ehalpern/sandbox (MIT licence)
 * @author Eric Halpern (eric.halpern@gmail.com)
 */
class AkkaModule extends AbstractModule {

  def configure(): Unit = {
    // All of the bindings for this module are defined using the
    // [[https://github.com/google/guice/wiki/ProvidesMethods provider methods]]
    // below.
  }

  /**
   * Provides the singleton root-actor-system to be injected whenever an ActorSystem
   * is required.  This method also registers the GuiceAkkaExtension
   * to be used for instantiating guice injected actors.
   */
  @Provides
  @inject.Singleton
  def provideActorSystem(injector: Injector, config: Config) : ActorSystem = {
    val system: ActorSystem = Try(config.getConfig("deepsense")).map(akkaConfig =>
      ActorSystem("root-actor-system", akkaConfig)
    ).recoverWith {
      case _: ConfigException.Missing => Try(ActorSystem("root-actor-system"))
    }.get
    // initialize and register extension to allow akka to create actors using Guice
    GuiceAkkaExtension(system).initialize(injector)
    system
  }

  /**
   * Provides a singleton factory to be injected whenever an ActorRefFactory
   * is required.
   */
  @Provides
  @inject.Singleton
  def provideActorRefFactory(systemProvider: Provider[ActorSystem]): ActorRefFactory = {
    systemProvider.get
  }

  /**
   * Provides a singleton execution context to be injected whenever an ExecutionContext
   * is required.
   */
  @Provides
  @inject.Singleton
  def provideExecutionContext(systemProvider: Provider[ActorSystem]): ExecutionContext = {
    systemProvider.get.dispatcher
  }
}
