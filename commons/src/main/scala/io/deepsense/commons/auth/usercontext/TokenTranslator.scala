/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.commons.auth.usercontext

import scala.concurrent.Future

/**
 * Translates a token to a user context.
 */
trait TokenTranslator {
  def translate(token: String): Future[UserContext]
}

abstract class TokenTranslatorException(message: String) extends Throwable(message)
abstract class InvalidTokenException(message: String, token: String)
  extends TokenTranslatorException(message)
case class CannotGetUserException(token: String)
  extends InvalidTokenException("TokenApi.getUserOfToken returned null for token: " + token, token)
case class CannotGetTokenException(token: String)
  extends InvalidTokenException("TokenApi.get returned null for token: " + token, token)
case class NoTenantSpecifiedException(token: String)
  extends InvalidTokenException("Tenant is null! Token: " + token, token)
