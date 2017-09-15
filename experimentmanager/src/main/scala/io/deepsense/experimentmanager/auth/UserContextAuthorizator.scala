/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.auth

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted

import io.deepsense.experimentmanager.auth.exceptions.NoRoleException
import io.deepsense.experimentmanager.auth.usercontext.UserContext

/**
 * Authorizator that wraps UserContext.
 * @param userContext UserContext to wrap around.
 * @param ec Execution context used to process futures.
 */
class UserContextAuthorizator @Inject()(
    @Assisted userContext: Future[UserContext])
    (implicit ec: ExecutionContext)
  extends Authorizator {

  override def withRole[T](role: String)(onSuccess: UserContext => Future[T]): Future[T] = {
    userContext.flatMap(uc => {
      if (uc.roles.map(_.name).contains(role)) {
        onSuccess(uc)
      } else {
        Future.failed(NoRoleException(uc, role))
      }
    })
  }
}

trait AuthorizatorProvider {
  def forContext(userContext: Future[UserContext]): Authorizator
}
