/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.lang3.StringUtils
import spray.http.HttpHeaders.`Content-Disposition`
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.unmarshalling.Unmarshaller
import spray.json._
import spray.routing.{ExceptionHandler, PathMatchers, Route}
import spray.util.LoggingContext

import io.deepsense.commons.auth.directives._
import io.deepsense.commons.auth.usercontext.TokenTranslator
import io.deepsense.commons.models.Id
import io.deepsense.commons.rest.{RestApiAbstractAuth, RestComponent}
import io.deepsense.graph.CyclicGraphException
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow._
import io.deepsense.models.workflows._
import io.deepsense.workflowmanager.WorkflowManagerProvider
import io.deepsense.workflowmanager.exceptions._
import io.deepsense.workflowmanager.util.WorkflowVersionUtil

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
  with WorkflowWithResultsJsonProtocol
  with MetadataInferenceResultJsonProtocol
  with WorkflowWithSavedResultsJsonProtocol
  with Cors
  with WorkflowVersionUtil {

  self: AbstractAuthDirectives =>

  assert(StringUtils.isNoneBlank(workflowsApiPrefix))
  private val workflowsPathPrefixMatcher = PathMatchers.separateOnSlashes(workflowsApiPrefix)
  private val reportsPathPrefixMatcher = PathMatchers.separateOnSlashes(reportsApiPrefix)
  private val workflowFileMultipartId = "workflowFile"
  private val workflowDownloadName = "workflow.json"

  private val WorkflowWithResultsUploadUnmarshaller: Unmarshaller[WorkflowWithResults] =
    Unmarshaller.delegate[MultipartFormData, WorkflowWithResults](`multipart/form-data`) {
      case multipartFormData =>
        val stringData = selectFormPart(multipartFormData, workflowFileMultipartId)
        versionedWorkflowWithResultsReader.read(JsonParser(ParserInput(stringData)))
    }

  private val WorkflowUploadUnmarshaller: Unmarshaller[Workflow] =
    Unmarshaller.delegate[MultipartFormData, Workflow](`multipart/form-data`) {
      case multipartFormData =>
        val stringData = selectFormPart(multipartFormData, workflowFileMultipartId)
        versionedWorkflowReader.read(JsonParser(ParserInput(stringData)))
    }

  private val versionedWorkflowUnmarashaler: Unmarshaller[Workflow] =
    sprayJsonUnmarshallerConverter[Workflow](versionedWorkflowReader)

  def route: Route = {
    cors {
      handleRejections(rejectionHandler) {
        handleExceptions(exceptionHandler) {
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
                  onComplete (workflowManagerProvider.forContext(userContext).get(workflowId)) {
                    checkEither orElse {
                      case Success(Some(Right(r))) => complete(StatusCodes.OK, r)
                    }
                  }
                }
              } ~
              put {
                withUserContext { userContext =>
                  implicit val unmarshaller = versionedWorkflowUnmarashaler
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
                  onComplete(
                    workflowManagerProvider
                      .forContext(userContext)
                      .download(workflowId)
                  ) {
                    case Success(workflowWithVariables) =>
                      if (workflowWithVariables.isDefined) {
                        val outputFileName = workflowWithVariables.get match {
                          case Left(stringWorkflow) => workflowDownloadName
                          case Right(objectWorkflow) => workflowFileName(objectWorkflow)
                        }
                        respondWithMediaType(`application/json`) {
                          complete(
                            StatusCodes.OK,
                            Seq(
                              `Content-Disposition`(
                                "attachment",
                                Map("filename" -> outputFileName))),
                            workflowWithVariables.get)
                        }
                      } else {
                        complete(StatusCodes.NotFound)
                      }
                    case Failure(exception) => failWith(exception)
                  }
                }
              }
            } ~
            path("upload") {
              post {
                withUserContext { userContext =>
                  implicit val unmarshaller = WorkflowUploadUnmarshaller
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
                  onComplete(
                    workflowManagerProvider.forContext(userContext)
                      .getLatestExecutionReport(workflowId)) {
                      checkEither orElse {
                        case Success(Some(Right(r))) => complete(StatusCodes.OK, r)
                      }
                  }
                }
              }
            } ~
            path("report" / "upload") {
              post {
                withUserContext { userContext =>
                  implicit val unmarshaller = WorkflowWithResultsUploadUnmarshaller
                  entity(as[WorkflowWithResults]) { workflowWithResults => {
                      onComplete(workflowManagerProvider
                        .forContext(userContext)
                        .saveWorkflowResults(workflowWithResults)) {
                        case Success(saved) => complete(StatusCodes.Created, saved)
                        case Failure(exception) => failWith(exception)
                      }
                    }
                  }
                }
              }
            } ~
            pathEndOrSingleSlash {
              post {
                withUserContext { userContext =>
                  implicit val format = versionedWorkflowUnmarashaler
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
                  onComplete(
                    workflowManagerProvider.forContext(userContext).getExecutionReport(reportId)) {
                    checkEither orElse {
                      case Success(Some(Right(r))) => complete(StatusCodes.OK, r)
                    }
                  }
                }
              }
            } ~
            path(JavaUUID / "download") { idParameter =>
              val reportId = ExecutionReportWithId.Id(idParameter)
              get {
                withUserContext { userContext =>
                  respondWithMediaType(`application/json`) {
                    respondWithHeader(
                      `Content-Disposition`("attachment", Map("filename" -> "report.json"))) {
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
    }
  }

  def checkEither: PartialFunction[Try[Option[Either[String, Any]]], Route] = {
    case Success(Some(Left(s))) => complete(StatusCodes.Conflict, s)
    case Success(None) => complete(StatusCodes.NotFound)
    case Failure(exception) => failWith(exception)
  }

  override def exceptionHandler(implicit log: LoggingContext): ExceptionHandler = {
    ExceptionHandler {
        case e: WorkflowNotFoundException =>
          complete(StatusCodes.NotFound, e.failureDescription)
        case e: WorkflowRunningException =>
          complete(StatusCodes.Conflict, e.failureDescription)
        case e: FileNotFoundException =>
          complete(StatusCodes.NotFound, e.failureDescription)
        case e: CyclicGraphException =>
          complete(StatusCodes.BadRequest, e.failureDescription)
        case e: WorkflowVersionNotSupportedException =>
          complete(StatusCodes.BadRequest, e.failureDescription)
    } orElse super.exceptionHandler(log)
  }

  private def selectFormPart(multipartFormData: MultipartFormData, partName: String): String =
    multipartFormData.fields
      .filter(_.name.get == partName)
      .map(_.entity.asString)
      .mkString

  private def workflowFileName(workflow: WorkflowWithVariables): String = {
    val thirdPartyData = workflow.thirdPartyData.data.parseJson
    // TODO DS-1486 Add "name" and "description" fields to Workflow
    Try(thirdPartyData.asJsObject
      .fields("gui").asJsObject
      .fields("name").asInstanceOf[JsString].value) match {
      case Success(name) => name.replaceAll("[^ a-zA-Z0-9.-]", "_") + ".json"
      case Failure(_) => workflowDownloadName
    }
  }
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
