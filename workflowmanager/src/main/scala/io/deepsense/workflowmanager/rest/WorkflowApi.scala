/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.lang3.StringUtils
import spray.http.StatusCodes
import spray.routing.{ExceptionHandler, PathMatchers, Route}
import spray.util.LoggingContext

import io.deepsense.commons.auth.usercontext.TokenTranslator
import io.deepsense.commons.models.Id
import io.deepsense.commons.rest.{RestApi, RestComponent}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.graph.CyclicGraphException
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.{MetadataInferenceResultJsonProtocol, WorkflowJsonProtocol, WorkflowWithKnowledgeJsonProtocol, WorkflowWithVariablesJsonProtocol}
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.WorkflowManagerProvider
import io.deepsense.workflowmanager.exceptions.{FileNotFoundException, WorkflowNotFoundException, WorkflowRunningException}

/**
 * Exposes Workflow Manager through a REST API.
 */
class WorkflowApi @Inject() (
    val tokenTranslator: TokenTranslator,
    workflowManagerProvider: WorkflowManagerProvider,
    @Named("workflows.api.prefix") apiPrefix: String,
    override val graphReader: GraphReader,
    override val inferContext: InferContext)
    (implicit ec: ExecutionContext)
  extends RestApi
  with RestComponent
  with WorkflowJsonProtocol
  with WorkflowWithKnowledgeJsonProtocol
  with WorkflowWithVariablesJsonProtocol
  with MetadataInferenceResultJsonProtocol {

  assert(StringUtils.isNoneBlank(apiPrefix))
  private val pathPrefixMatcher = PathMatchers.separateOnSlashes(apiPrefix)

  def route: Route = {
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        path("") {
          get {
            complete("Workflow Manager")
          }
        } ~
        pathPrefix(pathPrefixMatcher) {
          path(JavaUUID) { idParameter =>
            val workflowId = Id(idParameter)
            get {
              withUserContext { userContext =>
                complete {
                  workflowManagerProvider
                    .forContext(userContext)
                    .get(workflowId)
                }
              }
            } ~
            put {
              withUserContext { userContext =>
                entity(as[Workflow]) { workflow =>
                  complete {
                    workflowManagerProvider
                      .forContext(userContext)
                      .update(workflowId, workflow)
                  }
                }
              }
            } ~
            delete {
              withUserContext { userContext =>
                onComplete(
                  workflowManagerProvider
                    .forContext(userContext)
                    .delete(workflowId)) {
                  case Success(result) => result match {
                    case true => complete(StatusCodes.OK)
                    case false => complete(StatusCodes.NotFound)
                  }
                  case Failure(exception) => failWith(exception)
                }
              }
            }
          } ~
          path(JavaUUID / "download") { idParameter =>
            val workflowId = Id(idParameter)
            get {
              withUserContext { userContext =>
                complete {
                  workflowManagerProvider
                    .forContext(userContext)
                    .download(workflowId)
                }
              }
            }
          } ~
          pathEndOrSingleSlash {
            post {
              withUserContext { userContext =>
                entity(as[Workflow]) { workflow =>
                  onComplete(workflowManagerProvider
                    .forContext(userContext).create(workflow)) {
                    case Success(workflowWithKnowledge) => complete(
                      StatusCodes.Created, workflowWithKnowledge)
                    case Failure(exception) => failWith(exception)
                  }
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
        case e: WorkflowNotFoundException =>
          complete(StatusCodes.NotFound, e.failureDescription)
        case e: WorkflowRunningException =>
          complete(StatusCodes.Conflict, e.failureDescription)
        case e: FileNotFoundException =>
          complete(StatusCodes.NotFound, e.failureDescription)
        case e: CyclicGraphException =>
          complete(StatusCodes.BadRequest, e.failureDescription)
    }
  }
}
