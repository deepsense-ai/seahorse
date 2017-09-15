/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.commons.rest

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
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
  def provideApiRouterActorRef(
      @Named("server.startup.timeout") startupTimeout: Long,
      system: ActorSystem): ActorRef = {
    val supervisor =
      system.actorOf(GuiceAkkaExtension(system).props[RestServiceSupervisor], "RestSupervisor")
    val restServiceActorProps = GuiceAkkaExtension(system).props[RestServiceActor]
    implicit val timeout: Timeout = startupTimeout.seconds
    val actorRef = supervisor.ask((restServiceActorProps, "RestServiceActor")).mapTo[ActorRef]
    Await.result(actorRef, timeout.duration)
  }
}
