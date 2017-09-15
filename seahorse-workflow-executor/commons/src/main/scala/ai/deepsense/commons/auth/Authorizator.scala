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

package ai.deepsense.commons.auth

import scala.concurrent.Future

import ai.deepsense.commons.auth.exceptions.NoRoleException
import ai.deepsense.commons.auth.usercontext.UserContext

/**
 * Provides methods to simplify authorization mechanism. Represents an authenticated user's rights.
 */
trait Authorizator {
  /**
   * Checks if the authenticated user has the specified role.
   * If yes then invokes the internal function onSuccess passing UserContext
   * corresponding to the user to it as a parameter. Otherwise, fails with
   * [[ai.deepsense.commons.auth.exceptions.NoRoleException NoRoleException]]
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
