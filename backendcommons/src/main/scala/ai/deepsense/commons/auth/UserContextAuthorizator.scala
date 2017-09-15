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

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted

import ai.deepsense.commons.auth.exceptions.NoRoleException
import ai.deepsense.commons.auth.usercontext.UserContext

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
