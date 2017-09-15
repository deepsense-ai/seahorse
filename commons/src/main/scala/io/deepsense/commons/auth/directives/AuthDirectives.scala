/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.auth.directives

import scala.concurrent.Future

import shapeless._
import spray.http.StatusCodes
import spray.routing.Directive1
import spray.routing.Directives._

import io.deepsense.commons.auth.usercontext._

trait AbstractAuthDirectives {
  val TokenHeader = "X-Auth-Token"

  def withUserContext: Directive1[Future[UserContext]]
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
}

trait InsecureAuthDirectives extends AbstractAuthDirectives  {

  val UserIdHeader = "X-Seahorse-UserId"
  val UserNameHeader = "X-Seahorse-UserName"

  def withUserContext: Directive1[Future[UserContext]] = {
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

    optionalHeaderValueByName(UserIdHeader).flatMap {
      case Some(userId) => optionalHeaderValueByName(UserNameHeader).flatMap {
        case Some(userName) =>
          val user = User(
            id = userId,
            name = userName,
            email = None,
            enabled = Some(true),
            tenantId = Some(tenantId))
          val context = Future.successful(
            UserContextStruct(
              Token("godmode", Some(tenant)),
              tenant,
              user,
              godsRoles.map(Role(_)).toSet
            ))
          provide(context)
        case None => complete(StatusCodes.BadRequest)
      }
      case None => complete(StatusCodes.BadRequest)
    }
  }
}
