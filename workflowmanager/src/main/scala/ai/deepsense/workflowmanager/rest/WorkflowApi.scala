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

package ai.deepsense.workflowmanager.rest

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.lang3.StringUtils
import spray.http.HttpHeaders.{RawHeader, `Content-Disposition`}
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller
import spray.json._
import spray.routing._
import spray.routing.authentication.{BasicAuth, UserPass}
import spray.util.LoggingContext

import ai.deepsense.commons.auth.directives._
import ai.deepsense.commons.auth.usercontext.TokenTranslator
import ai.deepsense.commons.json.envelope.{Envelope, EnvelopeJsonFormat}
import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.commons.rest.ClusterDetailsJsonProtocol._
import ai.deepsense.commons.rest.{Cors, RestApiAbstractAuth, RestComponent}
import ai.deepsense.commons.utils.Version
import ai.deepsense.graph.{CyclicGraphException, DeeplangGraph}
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.json.workflow._
import ai.deepsense.models.json.workflow.exceptions.WorkflowVersionException
import ai.deepsense.models.workflows._
import ai.deepsense.workflowmanager.exceptions._
import ai.deepsense.workflowmanager.model.{WorkflowDescription, WorkflowDescriptionJsonProtocol, WorkflowPreset, WorkflowPresetJsonProtocol}
import ai.deepsense.workflowmanager.{PresetService, WorkflowManagerProvider}


/**
 * Exposes Workflow Manager through a REST API.
 */
abstract class WorkflowApi @Inject() (
    val tokenTranslator: TokenTranslator,
    workflowManagerProvider: WorkflowManagerProvider,
    @Named("workflows.api.prefix") workflowsApiPrefix: String,
    @Named("reports.api.prefix") reportsApiPrefix: String,
    @Named("auth.user") authUser: String,
    @Named("auth.pass") authPass: String,
    private val presetService: PresetService,
    override val graphReader: GraphReader)
    (implicit ec: ExecutionContext)
  extends RestApiAbstractAuth
  with RestComponent
  with WorkflowJsonProtocol
  with WorkflowWithVariablesJsonProtocol
  with WorkflowWithResultsJsonProtocol
  with WorkflowPresetJsonProtocol
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

  private val versionedWorkflowWithResultsUnmarashaler: Unmarshaller[WorkflowWithResults] =
    sprayJsonUnmarshallerConverter[WorkflowWithResults](versionedWorkflowWithResultsReader)

  private val workflowDescriptionUnmarashaler: Unmarshaller[WorkflowDescription] =
    sprayJsonUnmarshallerConverter[WorkflowDescription](
      WorkflowDescriptionJsonProtocol.workflowDescriptionJsonFormat)

  implicit private val envelopeWorkflowIdJsonFormat =
    new EnvelopeJsonFormat[Workflow.Id]("workflowId")

  private val presetPathPrefixMatcher = PathMatchers.separateOnSlashes("v1/presets")
  def respondWithPresetId(presetId: Long) =
    respondWithHeader(RawHeader("Location", presetId.toString))

  def route: Route = {
    cors {
      handleRejections(rejectionHandler) {
        handleExceptions(exceptionHandler) {
          basicAuth { _ =>
            path("") {
              get {
                complete("Workflow Manager")
              }
            } ~
            pathPrefix(presetPathPrefixMatcher) {
              path(LongNumber) { presetId =>
                get {
                  complete(presetService.getPreset(presetId))
                } ~
                delete {
                  complete {
                    presetService.removePreset(presetId)
                    StatusCodes.OK
                  }
                } ~
                post {
                  entity(as[ClusterDetails]) { request =>
                    val preset = presetService.updatePreset(presetId, request)
                    onSuccess(preset) {
                      case _ => complete(StatusCodes.OK)
                    }
                  }
                }
              } ~
              pathEndOrSingleSlash {
                get {
                  complete(presetService.listPresets())
                } ~
                post {
                  entity(as[ClusterDetails]) { request =>
                    val preset = presetService.createPreset(request)
                    onSuccess(preset) {
                      case newPresetId => respondWithPresetId(newPresetId) {
                        complete(StatusCodes.Created)
                      }
                    }
                  }
                }
              }
            } ~
            pathPrefix(workflowsPathPrefixMatcher) {
              path(JavaUUID) { workflowId =>
                get {
                  withUserId { userContext =>
                    onComplete(workflowManagerProvider.forContext(userContext).get(workflowId)) {
                      case Failure(exception) =>
                        logger.info("Get Workflow & results failed", exception)
                        failWith(exception)
                      case Success(workflowWithResults) =>
                        logger.info("Get Workflow & results")
                        complete(workflowWithResults)
                    }
                  }
                } ~
                put {
                  withUserId { userContext =>
                    implicit val unmarshaller = versionedWorkflowWithResultsUnmarashaler
                    entity(as[WorkflowWithResults]) {
                      workflowWithResults =>
                        onComplete(workflowManagerProvider
                          .forContext(userContext)
                          .updateStructAndStates(workflowId, workflowWithResults)) {
                          case Failure(exception) =>
                            logger.info("Workflow & results update failed", exception)
                            failWith(exception)
                          case Success(_) =>
                            logger.info("Workflow & results updated")
                            complete(StatusCodes.OK)
                        }
                    }
                  }
                } ~
                delete {
                  withUserId { userContext =>
                    onSuccess(workflowManagerProvider.forContext(userContext).delete(workflowId)) {
                      case true => complete(StatusCodes.OK)
                      case false => complete(StatusCodes.NotFound)
                    }
                  }
                }
              } ~
              path(JavaUUID / "info") { workflowId =>
                get {
                  withUserId { userContext =>
                    onComplete(workflowManagerProvider.forContext(userContext).getInfo(workflowId)) {
                      case Failure(exception) =>
                        logger.info("Get Workflow info failed", exception)
                        failWith(exception)
                      case Success(workflow) =>
                        logger.info("Get Workflow info")
                        complete(workflow)
                    }
                  }
                }
              } ~
              pathPrefix(JavaUUID / "download") { workflowId =>
                get {
                  val exportDatasource = parameter(Symbol("export-datasources").as[Boolean])
                  (withUserId & exportDatasource) { (userContext, exportDatasources) =>
                    val futureWorkflow =
                      workflowManagerProvider.forContext(userContext).download(workflowId, exportDatasources)
                    onSuccess(futureWorkflow) { w =>
                      w.map(workflowWithVariables =>
                        respondWithMediaType(`application/json`) {
                          complete(
                            StatusCodes.OK,
                            Seq(
                              `Content-Disposition`(
                                "attachment",
                                Map("filename" -> workflowFileName(workflowWithVariables)))),
                            workflowWithVariables)
                        }
                      ).getOrElse(complete(StatusCodes.NotFound))
                    }
                  }
                }
              } ~
              path(JavaUUID / "clone") { workflowId =>
                post {
                  withUserContext { userContext =>
                    implicit val unmarshaller = workflowDescriptionUnmarashaler
                    entity(as[WorkflowDescription]) { workflowDescription =>
                      onSuccess(workflowManagerProvider.forContext(userContext)
                        .clone(workflowId, workflowDescription)) {
                        case Some(workflowWithVariables) =>
                          val envelopedWorkflowId = Envelope(workflowWithVariables.id)
                          complete(StatusCodes.Created, envelopedWorkflowId)
                        case None =>
                          complete(StatusCodes.NotFound)
                      }
                    }
                  }
                }
              } ~
              path(JavaUUID / "preset") { workflowId =>
                get {
                  withUserId { userContext =>
                    val preset = presetService.getWorkflowsPreset(workflowId)
                    complete(preset)
                  }
                } ~
                post {
                  withUserId { userContext =>
                    entity(as[WorkflowPreset]) {
                      workflowPreset => {
                        if (workflowId != workflowPreset.id.value) {
                          logger.info("workflowId in URI and workflow preset are different")
                          complete(StatusCodes.BadRequest)
                        } else {
                          onComplete(presetService.saveWorkflowsPreset(
                            userContext, workflowId, workflowPreset)) {
                            case Failure(exception) =>
                              logger.info("Workflow & preset update failed", exception)
                              failWith(exception)
                            case Success(_) =>
                              complete(StatusCodes.OK)
                          }
                        }
                      }
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
                        implicit val unmarshaller = WorkflowUploadUnmarshaller
                        entity(as[Workflow]) { workflow =>
                          val futureWorkflowId =
                            workflowManagerProvider.forContext(userContext).create(workflow)
                          onSuccess(futureWorkflowId) { workflowId =>
                            val envelopedWorkflowId = Envelope(workflowId)
                            complete(StatusCodes.Created, envelopedWorkflowId)
                          }
                        }
                      }
                    }
                  }
                }
              } ~
              path(JavaUUID / "notebook" / JavaUUID) { (workflowId, nodeId) =>
                get {
                  withUserId { userContext =>
                    complete {
                      workflowManagerProvider.forContext(userContext)
                        .getNotebook(workflowId, nodeId)
                    }
                  }
                } ~
                post {
                  withUserContext { userContext =>
                    entity(as[String]) { notebook =>
                      onSuccess(workflowManagerProvider.forContext(userContext)
                        .saveNotebook(workflowId, nodeId, notebook)) { _ =>
                        complete(StatusCodes.Created)
                      }
                    }
                  }
                }
              } ~
              path(JavaUUID / "notebook" / JavaUUID / "copy" / JavaUUID) {
                (workflowId, nodeId, destinationNodeId) =>
                  post {
                    withUserContext { userContext =>
                      onSuccess(workflowManagerProvider.forContext(userContext)
                        .copyNotebook(workflowId, nodeId, destinationNodeId)) { _ =>
                        complete(StatusCodes.Created)
                      }
                    }
                  }
              } ~
              pathEndOrSingleSlash {
                get {
                  withUserId { userContext =>
                    onSuccess(workflowManagerProvider.forContext(userContext).list()) { workflows =>
                      complete(StatusCodes.OK, workflows)
                    }
                  }
                } ~
                post {
                  withUserContext { userContext =>
                    implicit val format = versionedWorkflowUnmarashaler
                    entity(as[Workflow]) { workflow =>
                      onSuccess(workflowManagerProvider
                        .forContext(userContext).create(workflow)) { workflowId =>
                        complete(StatusCodes.Created, Envelope(workflowId))
                      }
                    }
                  }
                }
              }
            } ~
            pathPrefix(reportsPathPrefixMatcher) {
              path(JavaUUID) { workflowId =>
                put {
                  withUserId { userContext =>
                    entity(as[ExecutionReport]) {
                      executioReport =>
                        onComplete(
                          workflowManagerProvider
                          .forContext(userContext)
                          .updateStates(workflowId, executioReport)) {
                          case Failure(exception) =>
                            logger.info("updateStates failed", exception)
                            failWith(exception)
                          case Success(_) =>
                            logger.info("updateStates succeeded")
                            complete(StatusCodes.OK)
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
        case e: WorkflowOwnerMismatchException =>
          complete(StatusCodes.Unauthorized, e.failureDescription)
    } orElse super.exceptionHandler(log)
  }

  private def basicAuth: Directive1[Unit] =
    authenticate(BasicAuth(userPassAuthenticator _, realm = "Workflow Manager"))

  private def userPassAuthenticator(userPass: Option[UserPass]): Future[Option[Unit]] =
    Future {
      userPass match {
        case Some(UserPass(user, pass)) if user == authUser && pass == authPass => Some(())
        case _ => None
      }
    }

  private def selectFormPart(multipartFormData: MultipartFormData, partName: String): String =
    multipartFormData.fields
      .filter(_.name.get == partName)
      .map(_.entity.asString(HttpCharsets.`UTF-8`))
      .mkString

  private def workflowFileName(workflow: WorkflowWithVariables): String = {
    val thirdPartyData = workflow.thirdPartyData
    // TODO DS-1486 Add "name" and "description" fields to Workflow
    Try(thirdPartyData
      .fields("gui").asJsObject
      .fields("name").asInstanceOf[JsString].value) match {
      case Success(name) => name.replaceAll("[^a-zA-Z0-9.-]", "_") + ".json"
      case Failure(_) => workflowDownloadName
    }
  }
}

class SecureWorkflowApi @Inject() (
  tokenTranslator: TokenTranslator,
  workflowManagerProvider: WorkflowManagerProvider,
  @Named("workflows.api.prefix") workflowsApiPrefix: String,
  @Named("reports.api.prefix") reportsApiPrefix: String,
  @Named("auth.user") authUser: String,
  @Named("auth.pass") authPass: String,
  private val presetService: PresetService,
  override val graphReader: GraphReader)
  (implicit ec: ExecutionContext)
  extends WorkflowApi(
    tokenTranslator,
    workflowManagerProvider,
    workflowsApiPrefix,
    reportsApiPrefix,
    authUser,
    authPass,
    presetService,
    graphReader)
  with AuthDirectives

class InsecureWorkflowApi @Inject() (
  tokenTranslator: TokenTranslator,
  workflowManagerProvider: WorkflowManagerProvider,
  @Named("workflows.api.prefix") workflowsApiPrefix: String,
  @Named("reports.api.prefix") reportsApiPrefix: String,
  @Named("auth.user") authUser: String,
  @Named("auth.pass") authPass: String,
  private val presetService: PresetService,
  override val graphReader: GraphReader)
  (implicit ec: ExecutionContext)
  extends WorkflowApi(
    tokenTranslator,
    workflowManagerProvider,
    workflowsApiPrefix,
    reportsApiPrefix,
    authUser,
    authPass,
    presetService,
    graphReader)
  with InsecureAuthDirectives
