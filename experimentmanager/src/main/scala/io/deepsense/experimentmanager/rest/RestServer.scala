/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.rest

import akka.actor.{ActorRef, ActorSystem}
import akka.io.IO
import com.google.inject.Inject
import com.google.inject.name.Named
import spray.can.Http

/**
 * RestServer binds an actor to Http messages.
 */
class RestServer @Inject()(
  @Named("server.host") host: String,
  @Named("server.port") port: Int,
  @Named("ApiRouterActorRef") routerRef: ActorRef
) (implicit actorSystem: ActorSystem) {
  def start(): Unit = {
    IO(Http) ! Http.Bind(routerRef, host, port)
  }
}
