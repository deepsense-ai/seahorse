/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.rest

import spray.http.StatusCodes
import spray.routing._
import spray.util.LoggingContext

import io.deepsense.experimentmanager.auth.directives.AuthDirectives
import io.deepsense.experimentmanager.auth.exceptions.{NoRoleException, ResourceAccessDeniedException}
import io.deepsense.experimentmanager.auth.usercontext.InvalidTokenException

trait RestService extends Directives with AuthDirectives {

  def exceptionHandler(implicit log: LoggingContext): ExceptionHandler = {
    ExceptionHandler {
      case e: NoRoleException =>
        complete(StatusCodes.Unauthorized)
      case e: ResourceAccessDeniedException =>
        complete(StatusCodes.NotFound)
      case e: InvalidTokenException =>
        // Works as a wildcard for all ITEs. Can be expanded for logging
        complete(StatusCodes.Unauthorized)
    }
  }

  val rejectionHandler: RejectionHandler = {
    RejectionHandler {
      case MissingHeaderRejection(param) :: _ if param == TokenHeader =>
        complete(StatusCodes.Unauthorized, s"Request is missing required header '$param'")
      case ValidationRejection(message, cause) :: _ =>
        complete(StatusCodes.BadRequest)
    }
  }

}
