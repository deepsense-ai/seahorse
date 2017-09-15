/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.rest

import com.datastax.driver.core.exceptions.NoHostAvailableException
import org.jclouds.http.HttpResponseException
import spray.http.{MediaTypes, StatusCodes}
import spray.routing._
import spray.util.LoggingContext

import io.deepsense.commons.auth.directives.{AbstractAuthDirectives, AuthDirectives}
import io.deepsense.commons.auth.exceptions.{NoRoleException, ResourceAccessDeniedException}
import io.deepsense.commons.auth.usercontext.InvalidTokenException
import io.deepsense.commons.exception.DeepSenseException
import io.deepsense.commons.exception.json.FailureDescriptionJsonProtocol
import io.deepsense.commons.utils.Logging

trait RestApiAbstractAuth
  extends Directives
  with Logging
  with FailureDescriptionJsonProtocol {
  suite: AbstractAuthDirectives =>

  def exceptionHandler(implicit log: LoggingContext): ExceptionHandler = {
    ExceptionHandler {
      case e: HttpResponseException =>
        logger.error("Could not contact Keystone!", e)
        complete(StatusCodes.ServiceUnavailable)
      case e: NoHostAvailableException =>
        logger.error("Could not contact Cassandra!", e)
        complete(StatusCodes.ServiceUnavailable)
      case e: NoRoleException =>
        logger.warn("A user does not have the expected role", e)
        complete(StatusCodes.Unauthorized)
      case e: ResourceAccessDeniedException =>
        logger.warn("A user tried to access a resource he does not have right to", e)
        complete(StatusCodes.NotFound)
      case e: InvalidTokenException =>
        logger.warn("Invalid token was send by the user", e)
        complete(StatusCodes.Unauthorized)
    }
  }

  val rejectionHandler: RejectionHandler = {
    RejectionHandler {
      case rejection @ (MalformedRequestContentRejection(message, Some(throwable)) :: _) =>
        logger.info(message, throwable)
        throwable match {
          case deepSenseException: DeepSenseException =>
            respondWithMediaType(MediaTypes.`application/json`) {
              complete(StatusCodes.BadRequest, deepSenseException.failureDescription)
            }
          case _ => RejectionHandler.Default(rejection)
        }

      case MissingHeaderRejection(param) :: _ if param == TokenHeader =>
        logger.info(s"A request was rejected because did not contain '$TokenHeader' header")
        complete(StatusCodes.Unauthorized, s"Request is missing required header '$param'")

      case ValidationRejection(rejectionMessage, cause) :: _ =>
        val message = s"A request was rejected because it was invalid: '$rejectionMessage'."
        cause match {
          case Some(throwable) => logger.info(message, throwable)
          case None => logger.info(message)
        }
        complete(StatusCodes.BadRequest)
    }
  }
}

trait RestApi extends RestApiAbstractAuth with AuthDirectives
