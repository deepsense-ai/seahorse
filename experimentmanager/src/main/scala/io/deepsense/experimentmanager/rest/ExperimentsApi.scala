/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest

import io.deepsense.experimentmanager.rest.metadata.MetadataInference

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.lang3.StringUtils
import spray.http.StatusCodes
import spray.routing.{ExceptionHandler, PathMatchers, Route}
import spray.util.LoggingContext

import io.deepsense.commons.auth.usercontext.TokenTranslator
import io.deepsense.commons.json.envelope.Envelope
import io.deepsense.commons.models.Id
import io.deepsense.commons.rest.{RestApi, RestComponent}
import io.deepsense.deeplang.InferContext
import io.deepsense.experimentmanager.ExperimentManagerProvider
import io.deepsense.experimentmanager.exceptions.{ExperimentNotFoundException, ExperimentRunningException}
import io.deepsense.experimentmanager.rest.actions.Action
import io.deepsense.experimentmanager.rest.json.ExperimentJsonProtocol
import io.deepsense.graphjson.GraphJsonProtocol.GraphReader
import io.deepsense.models.experiments.{Experiment, InputExperiment}
import io.deepsense.experimentmanager.rest.json.{AbstractMetadataJsonProtocol, MetadataSeqEnvelopeJsonProtocol}

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
  extends RestApi
  with RestComponent
  with ExperimentJsonProtocol
  with AbstractMetadataJsonProtocol
  with MetadataSeqEnvelopeJsonProtocol {

  assert(StringUtils.isNoneBlank(apiPrefix))
  private val pathPrefixMatcher = PathMatchers.separateOnSlashes(apiPrefix)

  def route: Route = {
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
                entity(as[Envelope[InputExperiment]]) { envelope =>
                  complete {experimentManagerProvider
                      .forContext(userContext)
                      .update(experimentId, envelope.content)
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
          path(JavaUUID / "metadata") { idParameter =>
            val experimentId = Id(idParameter)
            get {
              withUserContext { userContext =>
                parameters('nodeId, 'portIndex.as[Int]) { (nodeId, portIndex) =>
                  complete {
                    experimentManagerProvider
                      .forContext(userContext)
                      .get(experimentId)
                      .map(_.map((experiment =>
                        Envelope(MetadataInference.run(experiment, nodeId, portIndex)))))
                  }
                }
              }
            }
          } ~
          pathEndOrSingleSlash {
            post {
              withUserContext { userContext =>
                entity(as[Envelope[InputExperiment]]) { envelope =>
                  onComplete(experimentManagerProvider
                    .forContext(userContext).create(envelope.content)) {
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
          complete(StatusCodes.NotFound, e.failureDescription)
        case e: ExperimentRunningException =>
          complete(StatusCodes.Conflict, e.failureDescription)
    }
  }
}
