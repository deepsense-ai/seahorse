/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.commons.auth.usercontext

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
