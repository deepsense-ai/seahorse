/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

package io.deepsense.modeldeploying

import scala.concurrent.duration._

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http

object Boot extends App {

  // We need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // Create and start our service actor
  val service = system.actorOf(Props[ModelDeployingServiceActor], "modelDeployingService")

  implicit val timeout = Timeout(5.seconds)
  // Start a new HTTP server on port 8082 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8082)
}
