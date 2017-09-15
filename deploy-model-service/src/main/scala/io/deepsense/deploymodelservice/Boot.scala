/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deploymodelservice

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http

object Boot extends App {
  implicit val system = ActorSystem("deploy-model-service")
  val service = system.actorOf(Props[DeployModelServiceActor], "deployModelService")
  import scala.concurrent.duration._
  implicit val timeout = Timeout(5.seconds)
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8082)
}
