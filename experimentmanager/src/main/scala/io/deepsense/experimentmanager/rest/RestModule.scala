/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.rest

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule

import io.deepsense.experimentmanager.akka.GuiceAkkaExtension

/**
 * Configures RestServer internals.
 */
class RestModule extends ScalaModule {
  override def configure(): Unit = {
    bind[RestServer]
  }

  @Provides
  @Singleton
  @Named("ApiRouterActorRef")
  def provideApiRouterActorRef(system: ActorSystem): ActorRef = {
    system.actorOf(GuiceAkkaExtension(system).props[RestServiceActor])
  }
}
