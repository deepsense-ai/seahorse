/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.auth

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import io.deepsense.commons.utils.Logging

class KerberosInitiator
  extends AuthContextInitiator with Logging {

  def init(userId: String, token: String): Future[String] = {
    Future {
      val ccache = s"/tmp/krb5/${userId}.ccache"
      val exitCode = Runtime.getRuntime
        .exec(s"ktinit -c $ccache -t $token").waitFor
      if (exitCode != 0) {
        throw new Exception(s"Failed to execute ktinit (exit code: $exitCode).")
      }
      ccache
    }
  }
}
