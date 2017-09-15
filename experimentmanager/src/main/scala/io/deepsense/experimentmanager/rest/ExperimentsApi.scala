/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.rest

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.lang3.StringUtils
import spray.http.StatusCodes
import spray.routing.{ExceptionHandler, PathMatchers}
import spray.util.LoggingContext

import io.deepsense.commons.auth.usercontext.TokenTranslator
import io.deepsense.commons.exception.FailureDescription
import io.deepsense.commons.json.envelope.Envelope
import io.deepsense.commons.models.Id
import io.deepsense.commons.rest.{RestApi, RestComponent}
import io.deepsense.deeplang.InferContext
import io.deepsense.experimentmanager.ExperimentManagerProvider
import io.deepsense.experimentmanager.exceptions.{ExperimentNotFoundException,
  ExperimentRunningException}
import io.deepsense.experimentmanager.rest.actions.Action
import io.deepsense.experimentmanager.rest.json.ExperimentJsonProtocol
import io.deepsense.graphjson.GraphJsonProtocol.GraphReader
import io.deepsense.models.experiments.{Experiment, InputExperiment}

/**
 * Exposes Experiment Manager through a REST API.
 */
class ExperimentsApi @Inject() (
    val tokenTranslator: TokenTranslator,
    experimentManagerProvider: ExperimentManagerProvider,
    @Named("experiments.api.prefix") apiPrefix: String,
    override val graphReader: GraphReader,
    override val inferContext: InferContext)
    (implicit ec: ExecutionContext)
  extends RestApi with RestComponent with ExperimentJsonProtocol {

  assert(StringUtils.isNoneBlank(apiPrefix))
  private val pathPrefixMatcher = PathMatchers.separateOnSlashes(apiPrefix)

  def route = {
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        path("") {
          get {
            complete("Experiment Manager")
          }
        } ~
        pathPrefix(pathPrefixMatcher) {
          path(JavaUUID) { idParameter =>
            val experimentId = Id(idParameter)
            get {
              withUserContext { userContext =>
                complete(experimentManagerProvider
                  .forContext(userContext)
                  .get(experimentId)
                  .map(_.map(Envelope(_))))
              }
            } ~
            put {
              withUserContext { userContext =>
                entity(as[InputExperiment]) { inputExperiment =>
                  complete {experimentManagerProvider
                      .forContext(userContext)
                      .update(experimentId, inputExperiment)
                      .map(Envelope(_))
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
          path(JavaUUID / "action") { idParameter =>
            val experimentId = Id(idParameter)
            post {
              withUserContext { userContext =>
                entity(as[Action]) { action =>
                  onComplete(action.run(experimentId, experimentManagerProvider
                    .forContext(userContext))) {
                    case Success(experiment) => complete(
                      StatusCodes.Accepted, Envelope(experiment))
                    case Failure(exception) => failWith(exception)
                  }
                }
              }
            }
          } ~
          pathEndOrSingleSlash {
            post {
              withUserContext { userContext =>
                entity(as[InputExperiment]) { inputExperiment =>
                  onComplete(experimentManagerProvider
                    .forContext(userContext).create(inputExperiment)) {
                    case Success(experiment) => complete(
                      StatusCodes.Created, Envelope(experiment))
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
                    .experiments(limitInt, pageInt, statusEnum))
                }
              }
            }
          }
        }
      }
    }
  }

  override def exceptionHandler(implicit log: LoggingContext): ExceptionHandler = {
    super.exceptionHandler(log) orElse ExceptionHandler {
        case e: ExperimentNotFoundException =>
          complete(StatusCodes.NotFound, FailureDescription.fromException(e))
        case e: ExperimentRunningException =>
          complete(StatusCodes.Conflict, FailureDescription.fromException(e))
    }
  }
}
