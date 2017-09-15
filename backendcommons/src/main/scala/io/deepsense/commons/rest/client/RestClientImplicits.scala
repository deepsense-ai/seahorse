/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.commons.rest.client

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import akka.util.Timeout

trait RestClientImplicits {

  implicit val ctx: ExecutionContext
  implicit val as: ActorSystem
  implicit val timeout: Timeout
}
