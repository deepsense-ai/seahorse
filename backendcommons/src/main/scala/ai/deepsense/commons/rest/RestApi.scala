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

package ai.deepsense.commons.rest

import org.jclouds.http.HttpResponseException
import spray.http._
import spray.json.DeserializationException
import spray.json.JsonParser.ParsingException
import spray.routing
import spray.routing._
import spray.util.LoggingContext

import ai.deepsense.commons.auth.directives.{AbstractAuthDirectives, AuthDirectives}
import ai.deepsense.commons.auth.exceptions.{NoRoleException, ResourceAccessDeniedException}
import ai.deepsense.commons.auth.usercontext.InvalidTokenException
import ai.deepsense.commons.exception.json.FailureDescriptionJsonProtocol
import ai.deepsense.commons.exception.{DeepSenseException, DeepSenseFailure, FailureCode, FailureDescription}
import ai.deepsense.commons.utils.Logging

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
    def jsonFailureDescription(
        statusCode: StatusCode,
        description: FailureDescription): routing.Route = {
      respondWithMediaType(MediaTypes.`application/json`) {
        complete(statusCode, description)
      }
    }

    def handleMalformedRequestContentRejection(
        message: String,
        cause: Option[Throwable]): routing.Route = {

      val code = cause match {
        case Some(_: DeepSenseException)
             | Some(_: DeserializationException)
             | Some(_: NoSuchElementException)
             | Some(_: ParsingException)
             | Some(_: IllegalArgumentException) => StatusCodes.BadRequest
        case _ => StatusCodes.InternalServerError
      }

      val description = cause match {
        case Some(x: DeepSenseException) => x.failureDescription
        case Some(_: ParsingException) =>
          FailureDescription(
            DeepSenseFailure.Id.randomId,
            FailureCode.UnexpectedError,
            "Malformed request",
            Some(s"The request content does not seem to be JSON: $message"),
            Map())
        case Some(_: DeserializationException) | Some(_: IllegalArgumentException) =>
          FailureDescription(
            DeepSenseFailure.Id.randomId,
            FailureCode.UnexpectedError,
            "Malformed request",
            Some(s"The request content was malformed: $message"),
            Map())
        case _ =>
          FailureDescription(
            DeepSenseFailure.Id.randomId,
            FailureCode.UnexpectedError,
            "Internal Server Error",
            Some("The request could not be processed " +
              s"because of internal server error: $message"),
            Map())
      }

      val logMessage = s"MalformedRequestContentRejection (${description.id}): $message"
      cause match {
        case Some(_: DeepSenseException)
             | Some(_: DeserializationException)
             | Some(_: ParsingException)
             | Some(_: IllegalArgumentException) => logger.info(logMessage, cause.get)
        case Some(e) => logger.error(logMessage, cause.get)
        case _ => logger.info(logMessage)
      }

      jsonFailureDescription(code, description)
    }

    RejectionHandler {
      case MalformedRequestContentRejection(message, cause) :: _ =>
        handleMalformedRequestContentRejection(message, cause)

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
