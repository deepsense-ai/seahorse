/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.lang3.StringUtils
import spray.http.HttpHeaders.`Content-Disposition`
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller
import spray.json._
import spray.routing.{ExceptionHandler, PathMatchers, Route}
import spray.util.LoggingContext

import io.deepsense.commons.auth.directives._
import io.deepsense.commons.auth.usercontext.TokenTranslator
import io.deepsense.commons.json.envelope.Envelope
import io.deepsense.commons.rest.{RestApiAbstractAuth, RestComponent}
import io.deepsense.commons.utils.Version
import io.deepsense.graph.CyclicGraphException
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow._
import io.deepsense.models.json.workflow.exceptions.WorkflowVersionException
import io.deepsense.models.workflows._
import io.deepsense.workflowmanager.WorkflowManagerProvider
import io.deepsense.workflowmanager.exceptions._
import io.deepsense.workflowmanager.rest.protocols.ResultsUploadTimeJsonProtocol

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
  with DOperationEnvelopesJsonProtocol
  with Cors
  with WorkflowVersionUtil {

  self: AbstractAuthDirectives =>

  override def currentVersion: Version = CurrentBuild.version

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

  private val JsObjectUnmarshaller: Unmarshaller[JsObject] =
    Unmarshaller.delegate[MultipartFormData, JsObject](`multipart/form-data`) {
      case multipartFormData =>
        val stringData = selectFormPart(multipartFormData, workflowFileMultipartId)
        JsonParser(ParserInput(stringData)).asJsObject
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
            path(JavaUUID) { workflowId =>
              get {
                withUserContext { userContext =>
                  workflowManagerProvider.forContext(userContext).get(workflowId)
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
            path(JavaUUID / "download") { workflowId =>
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
            path(JavaUUID / "results-upload-time") { idParameter =>
              get {
                withUserContext { userContext =>
                  onSuccess(workflowManagerProvider
                    .forContext(userContext)
                    .getResultsUploadTime(idParameter)) { lastExecution =>
                    import ResultsUploadTimeJsonProtocol.lastExecutionWriter
                    complete(lastExecution.map(Envelope(_)))
                  }

                }
              }
            } ~
            path("upload") {
              post {
                withUserContext {
                  userContext => {
                    implicit val unmarshaller = JsObjectUnmarshaller
                    entity(as[JsObject]) { jsObject =>
                      val containsExecutionReport = jsObject.getFields("executionReport").nonEmpty

                      val workflowWithResultsEntity = if (containsExecutionReport) {
                        implicit val unmarshaller = WorkflowWithResultsUploadUnmarshaller
                        Some(entity(as[WorkflowWithResults]))
                      } else {
                        None
                      }

                      implicit val unmarshaller = WorkflowUploadUnmarshaller
                      entity(as[Workflow]) { workflow =>
                        onComplete(
                          workflowManagerProvider.forContext(userContext).create(workflow)) {
                          case Failure(exception) => failWith(exception)
                          case Success(workflowWithKnowledge) =>
                            if (containsExecutionReport) {
                              workflowWithResultsEntity.get {
                                workflowWithResults => {
                                  onComplete(workflowManagerProvider
                                    .forContext(userContext)
                                    .saveWorkflowResults(workflowWithResults)) {
                                    case Failure(exception) =>
                                      logger.info("Uploaded workflow contained execution report, " +
                                        "but execution report upload failed", exception)
                                      failWith(exception)
                                    case Success(_) =>
                                      logger.info("Uploaded workflow contained execution report, " +
                                        "and execution report upload succeed")
                                      // Return workflowWithKnowledge instead of wotkflowWithResults
                                      complete(StatusCodes.Created, workflowWithKnowledge)
                                  }
                                }
                              }
                            } else {
                              complete(StatusCodes.Created, workflowWithKnowledge)
                            }
                        }
                      }
                    }
                  }
                }
              }
            } ~
            path(JavaUUID / "report") { workflowId =>
              get {
                withUserContext { userContext =>
                    workflowManagerProvider.forContext(userContext)
                      .getLatestExecutionReport(workflowId)
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
            path(JavaUUID / "notebook" / JavaUUID) { (workflowId, nodeId) =>
              get {
                withUserContext { userContext =>
                  complete {
                    workflowManagerProvider.forContext(userContext)
                      .getNotebook(workflowId, nodeId)
                  }
                }
              } ~
              post {
                withUserContext { userContext =>
                  entity(as[String]) { notebook =>
                    onComplete(workflowManagerProvider.forContext(userContext)
                      .saveNotebook(workflowId, nodeId, notebook)) {
                      case Success(_) => complete(StatusCodes.Created)
                      case Failure(exception) => failWith(exception)
                    }
                  }
                }
              }
            } ~
            pathEndOrSingleSlash {
              get {
                withUserContext { userContext =>
                  onComplete(workflowManagerProvider.forContext(userContext).list()) {
                    case Success(workflows) => complete(StatusCodes.OK, workflows)
                    case Failure(exception) => failWith(exception)
                  }
                }
              } ~
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
            path(JavaUUID) { reportId =>
              get {
                withUserContext { userContext =>
                    workflowManagerProvider.forContext(userContext).getExecutionReport(reportId)
                }
              }
            } ~
            path(JavaUUID / "download") { reportId =>
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

  implicit def checkEither[T : Marshaller](x: Future[Option[Either[String, T]]]): Route = {
    onSuccess(x) {
      case Some(Left(s)) => complete(StatusCodes.Conflict, s)
      case Some(Right(r)) => complete(StatusCodes.OK, r)
      case None => complete(StatusCodes.NotFound)
    }
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
        case e: WorkflowVersionException =>
          complete(StatusCodes.BadRequest, e.failureDescription)
    } orElse super.exceptionHandler(log)
  }

  private def selectFormPart(multipartFormData: MultipartFormData, partName: String): String =
    multipartFormData.fields
      .filter(_.name.get == partName)
      .map(_.entity.asString(HttpCharsets.`UTF-8`))
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
