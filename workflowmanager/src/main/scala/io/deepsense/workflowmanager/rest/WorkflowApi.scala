/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import com.google.inject.Inject
import com.google.inject.name.Named
import io.deepsense.commons.auth.directives.{AbstractAuthDirectives, AuthDirectives, InsecureAuthDirectives}
import io.deepsense.commons.auth.usercontext.TokenTranslator
import io.deepsense.commons.models.Id
import io.deepsense.commons.rest.{RestApiAbstractAuth, RestComponent}
import io.deepsense.graph.CyclicGraphException
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow._
import io.deepsense.models.workflows.{Workflow, WorkflowWithResults}
import io.deepsense.workflowmanager.WorkflowManagerProvider
import io.deepsense.workflowmanager.exceptions.{FileNotFoundException, WorkflowNotFoundException, WorkflowRunningException}
import io.deepsense.workflowmanager.json.WorkflowWithSavedResultsJsonProtocol
import io.deepsense.workflowmanager.model.ExecutionReportWithId
import org.apache.commons.lang3.StringUtils
import spray.http.HttpHeaders.`Content-Disposition`
import spray.http.MediaTypes._
import spray.http.{MultipartFormData, StatusCodes}
import spray.httpx.unmarshalling.Unmarshaller
import spray.json.{JsonParser, ParserInput}
import spray.routing.{ExceptionHandler, PathMatchers, Route}
import spray.util.LoggingContext

/**
 * Exposes Workflow Manager through a REST API.
 */
abstract class WorkflowApi @Inject() (
    val tokenTranslator: TokenTranslator,
    workflowManagerProvider: WorkflowManagerProvider,
    @Named("workflows.api.prefix") workflowsApiPrefix: String,
    @Named("reports.api.prefix") reportsApiPrefix: String,
    override val graphReader: GraphReader)
    (implicit ec: ExecutionContext)
  extends RestApiAbstractAuth
  with RestComponent
  with WorkflowJsonProtocol
  with WorkflowWithKnowledgeJsonProtocol
  with WorkflowWithVariablesJsonProtocol
  with MetadataInferenceResultJsonProtocol
  with WorkflowWithSavedResultsJsonProtocol
  with Cors {

  self: AbstractAuthDirectives =>

  assert(StringUtils.isNoneBlank(workflowsApiPrefix))
  private val workflowsPathPrefixMatcher = PathMatchers.separateOnSlashes(workflowsApiPrefix)
  private val reportsPathPrefixMatcher = PathMatchers.separateOnSlashes(reportsApiPrefix)

  private val workflowFileMultipartId = "workflowFile"

  private val WorkflowWithResultsUnmarshaller: Unmarshaller[WorkflowWithResults] =
    Unmarshaller.delegate[MultipartFormData, WorkflowWithResults](`multipart/form-data`) {
      case multipartFormData =>
        workflowWithResultsFormat.read(JsonParser(ParserInput(
          selectFormPart(multipartFormData, workflowFileMultipartId))))
    }

  private val WorkflowUnmarshaller: Unmarshaller[Workflow] =
    Unmarshaller.delegate[MultipartFormData, Workflow](`multipart/form-data`) {
    case multipartFormData =>
      workflowFormat.read(JsonParser(ParserInput(
        selectFormPart(multipartFormData, workflowFileMultipartId))))
  }

  def route: Route = {
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        cors {
          path("") {
            get {
              complete("Workflow Manager")
            }
          } ~
          pathPrefix(workflowsPathPrefixMatcher) {
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
                  respondWithHeader(
                    `Content-Disposition`("attachment", Map("filename" -> "workflow.json"))) {
                      complete {
                        workflowManagerProvider
                          .forContext(userContext)
                          .download(workflowId)
                      }
                  }
                }
              }
            } ~
            path("upload") {
              post {
                withUserContext { userContext =>
                  implicit val unmarshaller = WorkflowUnmarshaller
                  entity(as[Workflow]) { workflow =>
                    onComplete(
                      workflowManagerProvider
                        .forContext(userContext)
                        .create(workflow)) {
                      case Success(workflowWithKnowledge) => complete(
                        StatusCodes.Created, workflowWithKnowledge)
                      case Failure(exception) => failWith(exception)
                    }
                  }
                }
              }
            } ~
            path(JavaUUID / "report") { idParameter =>
              val workflowId = Id(idParameter)
              get {
                withUserContext { userContext =>
                  complete {
                    workflowManagerProvider.forContext(userContext)
                      .getLatestExecutionReport(workflowId)
                  }
                }
              }
            } ~
            path("report" / "upload") {
              post {
                withUserContext { userContext =>
                  implicit val unmarshaller = WorkflowWithResultsUnmarshaller
                  entity(as[WorkflowWithResults]) { workflowWithResults =>
                    onComplete(workflowManagerProvider
                      .forContext(userContext)
                      .saveWorkflowResults(workflowWithResults)) {
                        case Success(saved) => complete(StatusCodes.Created, saved)
                        case Failure(exception) => failWith(exception)
                      }
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
          } ~
          pathPrefix(reportsPathPrefixMatcher) {
            path(JavaUUID) { idParameter =>
              val reportId = ExecutionReportWithId.Id(idParameter)
              get {
                withUserContext { userContext =>
                  complete {
                    workflowManagerProvider.forContext(userContext).getExecutionReport(reportId)
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

  private def selectFormPart(multipartFormData: MultipartFormData, partName: String): String =
    multipartFormData.fields
      .filter(_.name.get == partName)
      .map(_.entity.asString)
      .mkString
}

class SecureWorkflowApi @Inject() (
  tokenTranslator: TokenTranslator,
  workflowManagerProvider: WorkflowManagerProvider,
  @Named("workflows.api.prefix") workflowsApiPrefix: String,
  @Named("reports.api.prefix") reportsApiPrefix: String,
  override val graphReader: GraphReader)
  (implicit ec: ExecutionContext)
  extends WorkflowApi(
    tokenTranslator,
    workflowManagerProvider,
    workflowsApiPrefix,
    reportsApiPrefix,
    graphReader)
  with AuthDirectives

class InsecureWorkflowApi @Inject() (
  tokenTranslator: TokenTranslator,
  workflowManagerProvider: WorkflowManagerProvider,
  @Named("workflows.api.prefix") workflowsApiPrefix: String,
  @Named("reports.api.prefix") reportsApiPrefix: String,
  override val graphReader: GraphReader)
  (implicit ec: ExecutionContext)
  extends WorkflowApi(
    tokenTranslator,
    workflowManagerProvider,
    workflowsApiPrefix,
    reportsApiPrefix,
    graphReader)
  with InsecureAuthDirectives
