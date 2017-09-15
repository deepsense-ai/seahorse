/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.commons.rest

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}

import io.deepsense.commons.akka.GuiceAkkaExtension

/**
 * Configures RestServer internals.
 */
class RestModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[RestServer])
  }

  @Provides
  @Singleton
  @Named("ApiRouterActorRef")
  def provideApiRouterActorRef(system: ActorSystem): ActorRef = {
    system.actorOf(GuiceAkkaExtension(system).props[RestServiceActor])
  }
}
