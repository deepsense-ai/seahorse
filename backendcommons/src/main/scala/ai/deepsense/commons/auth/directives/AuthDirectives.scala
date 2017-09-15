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

package ai.deepsense.commons.auth.directives

import scala.concurrent.Future

import shapeless._
import spray.http.StatusCodes
import spray.routing.Directive1
import spray.routing.Directives._

import ai.deepsense.commons.auth.usercontext._
import ai.deepsense.commons.models.Id

trait AbstractAuthDirectives {
  val TokenHeader = AuthDirectives.TokenHeader

  def withUserContext: Directive1[Future[UserContext]]

  def withUserId: Directive1[Future[UserContext]]
}

trait AuthDirectives extends AbstractAuthDirectives {
  def tokenTranslator: TokenTranslator

  /**
   * Retrieves Auth Token from Http headers (X-Auth-Token) and asynchronously
   * translates it to UserContext. When the required header is missing
   * the request is rejected with a [[spray.routing.MissingHeaderRejection]].
   */
  def withUserContext: Directive1[Future[UserContext]] = {
    headerValueByName(TokenHeader).hmap {
      case rawToken :: HNil => tokenTranslator.translate(rawToken)
    }
  }

  def withUserId: Directive1[Future[UserContext]] = ???
}

trait InsecureAuthDirectives extends AbstractAuthDirectives  {

  val UserIdHeader = AuthDirectives.UserIdHeader
  val UserNameHeader = AuthDirectives.UserNameHeader

  val tenantId = "olympus"
  val tenant: Tenant = Tenant(tenantId, tenantId, tenantId, Some(true))
  val godsRoles = Seq(
    "workflows:get",
    "workflows:update",
    "workflows:create",
    "workflows:list",
    "workflows:delete",
    "workflows:launch",
    "workflows:abort",
    "entities:get",
    "entities:create",
    "entities:update",
    "entities:delete",
    "admin")

  private def user(id: String, name: String): User = {
    User(
      id = id,
      name = name,
      email = None,
      enabled = Some(true),
      tenantId = Some(tenantId))
  }

  private def context(userId: String, userName: String): Future[UserContextStruct] = {
    Future.successful(
      UserContextStruct(
        Token("godmode", Some(tenant)),
        tenant,
        user(userId, userName),
        godsRoles.map(Role(_)).toSet
      ))
  }

  def withUserId: Directive1[Future[UserContext]] = {
    optionalHeaderValueByName(UserIdHeader).flatMap {
      case Some(userId) =>
        optionalHeaderValueByName(UserNameHeader).flatMap {
          case userName =>
            provide(context(userId, userName.getOrElse("?")))
        }
      case None => complete(StatusCodes.BadRequest)
    }
  }

  def withUserContext: Directive1[Future[UserContext]] = {
    optionalHeaderValueByName(UserIdHeader).flatMap {
      case Some(userId) => optionalHeaderValueByName(UserNameHeader).flatMap {
        case Some(userName) =>
          provide(context(userId, userName))
        case None => complete(StatusCodes.BadRequest)
      }
      case None => complete(StatusCodes.BadRequest)
    }
  }
}

object AuthDirectives {
  val UserIdHeader = "X-Seahorse-UserId"
  val UserNameHeader = "X-Seahorse-UserName"
  val TokenHeader = "X-Auth-Token"
}
