/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.rest

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import com.google.inject.Inject
import spray.http.StatusCodes
import spray.routing.{ValidationRejection, MissingHeaderRejection, MissingQueryParamRejection, RejectionHandler, Directives, ExceptionHandler}
import spray.util.LoggingContext

import io.deepsense.experimentmanager.app.ExperimentManagerProvider
import io.deepsense.experimentmanager.app.exceptions.ExperimentNotFoundException
import io.deepsense.experimentmanager.app.models.{Experiment, Id, InputExperiment}
import io.deepsense.experimentmanager.app.rest.actions.Action
import io.deepsense.experimentmanager.app.rest.json.RestJsonProtocol._
import io.deepsense.experimentmanager.auth.directives.AuthDirectives
import io.deepsense.experimentmanager.auth.exceptions.{ResourceAccessDeniedException, NoRoleException, AuthException}
import io.deepsense.experimentmanager.auth.usercontext.{InvalidTokenException, TokenTranslator}
import io.deepsense.experimentmanager.rest.RestComponent

/**
 * Exposes Experiment Manager through a REST API.
 */
class RestApi @Inject() (
    val tokenTranslator: TokenTranslator,
    experimentManagerProvider: ExperimentManagerProvider)
    (implicit ec: ExecutionContext)
  extends Directives with RestComponent with AuthDirectives {


  def route = {
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        path("") {
          get {
            complete("Experiment Manager")
          }
        } ~
        path("experiments" / JavaUUID) { idParameter =>
          val experimentId = Id(idParameter)
          get {
            withUserContext { userContext =>
              complete(experimentManagerProvider
                .forContext(userContext)
                .get(experimentId))
            }
          } ~
          put {
            entity(as[Experiment]) { experiment =>
              validate(
                experiment.id.equals(experimentId), // TODO Return Json
                // For now, when you PUT an experiment on a UUID other than the specified
                // in the URL there will be a validation exception. As a result a "Bad request"
                // will be returned. The problem is (is it a problem?) that the response body
                // (so the description of the error) is a text message not a Json (see the
                // second argument of validate(...)). If we do not want this kind of
                // behavior then we have to implement own Rejection Handler (in sense of Spray).
                "Experiment's Id from Json does not match Id from request's URL") {
                withUserContext { userContext =>
                  complete {
                    experimentManagerProvider
                      .forContext(userContext)
                      .update(experiment)
                  }
                }
              }
            }
          } ~
          delete {
            withUserContext { userContext =>
              onComplete(
                experimentManagerProvider
                  .forContext(userContext)
                  .delete(experimentId)) {
                case Success(result) => result match {
                  case true => complete(StatusCodes.OK)
                  case false => complete(StatusCodes.NotFound)
                }
                case Failure(exception) => failWith(exception)
              }
            }
          }
        } ~
        path("experiments" / JavaUUID / "action") { idParameter =>
          val experimentId = Id(idParameter)
          post {
            entity(as[Action]) { action =>
              withUserContext { userContext =>
                onComplete(action.run(experimentId, experimentManagerProvider
                  .forContext(userContext))) {
                  case Success(experiment) => complete(StatusCodes.Accepted, experiment)
                  case Failure(exception) => failWith(exception)
                }
              }
            }
          }
        } ~
        path("experiments") {
          post {
            entity(as[InputExperiment]) { inputExperiment =>
              withUserContext { userContext =>
                onComplete(experimentManagerProvider
                  .forContext(userContext).create(inputExperiment)) {
                  case Success(experiment) => complete(StatusCodes.Created, experiment)
                  case Failure(exception) => failWith(exception)
                }
              }
            }
          } ~
          get {
            withUserContext { userContext =>
              parameters('limit.?, 'page.?, 'status.?) { (limit, page, status) =>
                val limitInt = limit.map(_.toInt)
                val pageInt = page.map(_.toInt)
                val statusEnum = status.map(Experiment.Status.withName)
                complete(experimentManagerProvider
                  .forContext(userContext)
                  .experiments(limitInt, pageInt, statusEnum).map(_.toList))
              }
            }
          }
        }
      }
    }
  }

  private def exceptionHandler(implicit log: LoggingContext): ExceptionHandler = {
    ExceptionHandler {
      case e: ExperimentNotFoundException =>
        complete(StatusCodes.NotFound, RestException.fromException(e))
      case e: NoRoleException =>
        complete(StatusCodes.Unauthorized)
      case e: ResourceAccessDeniedException =>
        complete(StatusCodes.NotFound)
      case e: InvalidTokenException =>
        // Works as a wildcard for all ITEs. Can be expanded for logging
        complete(StatusCodes.Unauthorized)
    }
  }

  private val rejectionHandler: RejectionHandler = {
    RejectionHandler {
      case MissingHeaderRejection(param) :: _ if param == TokenHeader =>
        complete(StatusCodes.Unauthorized, s"Request is missing required header '$param'")
      case ValidationRejection(message, cause) :: _ =>
        complete(StatusCodes.BadRequest)
    }
  }
}
