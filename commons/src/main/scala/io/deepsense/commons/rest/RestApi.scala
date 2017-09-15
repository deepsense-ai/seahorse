/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.commons.rest

import com.datastax.driver.core.exceptions.NoHostAvailableException
import spray.http.StatusCodes
import spray.routing._
import spray.util.LoggingContext

import io.deepsense.commons.auth.directives.AuthDirectives
import io.deepsense.commons.auth.exceptions.{NoRoleException, ResourceAccessDeniedException}
import io.deepsense.commons.auth.usercontext.InvalidTokenException

trait RestApi extends Directives with AuthDirectives {

  def exceptionHandler(implicit log: LoggingContext): ExceptionHandler = {
    ExceptionHandler {
      case _: NoHostAvailableException =>
        complete(StatusCodes.ServiceUnavailable)
      case _: NoRoleException =>
        complete(StatusCodes.Unauthorized)
      case _: ResourceAccessDeniedException =>
        complete(StatusCodes.NotFound)
      case _: InvalidTokenException =>
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
