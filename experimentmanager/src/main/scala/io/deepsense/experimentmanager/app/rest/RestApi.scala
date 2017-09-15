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
import spray.routing.{Directives, ExceptionHandler}
import spray.util.LoggingContext
import spray.json._
import io.deepsense.experimentmanager.app.rest.json.RestJsonProtocol._

import io.deepsense.experimentmanager.app.ExperimentManager
import io.deepsense.experimentmanager.app.exceptions.ExperimentNotFoundException
import io.deepsense.experimentmanager.app.models.{Experiment, Id, InputExperiment}
import io.deepsense.experimentmanager.app.rest.actions.Action
import io.deepsense.experimentmanager.rest.RestComponent

/**
 * Exposes Experiment Manager through a REST API.
 */
class RestApi @Inject() (experimentManager: ExperimentManager)(implicit ec: ExecutionContext)
    extends Directives with RestComponent {

  def route = {
    handleExceptions(exceptionHandler) {
      path("") {
        get {
          complete {
            "This is Experiment Manager. " +
              "Try /experiments " +
              "or /experiments/:id " +
              "eg. /experiments/b93b65f4-0a92-4ced-b244-98b79674cde8"
          }
        }
      } ~
      path("experiments" / JavaUUID) { idParameter =>
        val experimentId = Id(idParameter)
        get {
          complete(experimentManager.get(experimentId))
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
              complete {
                experimentManager.update(experiment)
              }
            }
          }
        } ~
        delete {
          onComplete(experimentManager.delete(experimentId)) {
            case Success(_) => complete("")
            case Failure(exception) => failWith(exception)
          }
        }
      } ~
      path("experiments" / JavaUUID / "action") { idParameter =>
        val experimentId = Id(idParameter)
        post {
          entity(as[Action]) { action =>
            onComplete(action.run(experimentId, experimentManager)) {
              case Success(experiment) => complete(StatusCodes.Accepted, experiment)
              case Failure(exception) => failWith(exception)
            }
          }
        }
      } ~
      path("experiments") {
        post {
          entity(as[InputExperiment]) { inputExperiment =>
            onComplete(experimentManager.create(inputExperiment)) {
              case Success(experiment) => complete(StatusCodes.Created, experiment)
              case Failure(exception) => failWith(exception)
            }
          }
        } ~
        get {
          parameters('limit.?, 'page.?, 'status.?) { (limit, page, status) =>
            // TODO Validate params. Eg. check if the limit is not to big.
            val limitInt = limit.map(_.toInt)
            val pageInt = page.map(_.toInt)
            val statusEnum = status.map(Experiment.Status.withName)
            complete(experimentManager.experiments(limitInt, pageInt, statusEnum).map(_.toList))
          }
        }
      }
    }
  }

  private def exceptionHandler(implicit log: LoggingContext): ExceptionHandler = {
    ExceptionHandler {
      case e: ExperimentNotFoundException =>
        complete(StatusCodes.NotFound, RestException.fromException(e))
    }
 }
}
