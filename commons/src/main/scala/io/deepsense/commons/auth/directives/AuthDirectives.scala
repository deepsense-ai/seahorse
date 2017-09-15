/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.auth.directives

import scala.concurrent.Future

import shapeless._
import spray.routing.Directive1
import spray.routing.Directives._

import io.deepsense.commons.auth.usercontext.{TokenTranslator, UserContext}

trait AuthDirectives {
  val TokenHeader = "X-Auth-Token"

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
