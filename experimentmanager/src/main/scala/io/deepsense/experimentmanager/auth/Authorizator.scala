/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.auth

import scala.concurrent.Future

import io.deepsense.experimentmanager.auth.usercontext.UserContext

trait Authorizator {
  def withRole[T](role: String)(f: (UserContext) => Future[T]): Future[T]
}
