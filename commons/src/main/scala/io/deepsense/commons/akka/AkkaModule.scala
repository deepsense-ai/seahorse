/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.commons.akka

import scala.concurrent.ExecutionContext

import akka.actor.{ActorRefFactory, ActorSystem}
import com.google.inject
import com.google.inject.{AbstractModule, Injector, Provider, Provides}

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
  def provideActorSystem(injector: Injector) : ActorSystem = {
    val system = ActorSystem("root-actor-system")
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
