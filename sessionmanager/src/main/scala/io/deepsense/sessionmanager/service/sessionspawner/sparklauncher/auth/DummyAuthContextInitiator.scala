/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.auth

import scala.concurrent.Future

class DummyAuthContextInitiator extends AuthContextInitiator {

  def init(userId: String, token: String): Future[String] = Future.successful("/tmp/dummy")
}
