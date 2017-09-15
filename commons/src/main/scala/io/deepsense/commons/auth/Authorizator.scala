/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.auth

import scala.concurrent.Future

import io.deepsense.commons.auth.exceptions.NoRoleException
import io.deepsense.commons.auth.usercontext.UserContext

/**
 * Provides methods to simplify authorization mechanism. Represents an authenticated user's rights.
 */
trait Authorizator {
  /**
   * Checks if the authenticated user has the specified role.
   * If yes then invokes the internal function onSuccess passing UserContext
   * corresponding to the user to it as a parameter. Otherwise, fails with
   * [[NoRoleException]]
   * @param role A role that the user should have.
   * @param onSuccess A function that is invoked when the user has the expected role.
   * @tparam T Return type of onSuccess function.
   * @return Result of onSuccess function when the user has the expected role
   *         or a failed future when the user does not have the role.
   */
  def withRole[T](role: String)(onSuccess: (UserContext) => Future[T]): Future[T]
}

trait AuthorizatorProvider {
  def forContext(userContext: Future[UserContext]): Authorizator
}
