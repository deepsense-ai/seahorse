/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest

import scala.collection.concurrent.TrieMap
import scala.concurrent._

import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import spray.http.HttpHeaders.{RawHeader, `Content-Disposition`}
import spray.http._
import spray.json._
import spray.routing.Route

import io.deepsense.commons.auth.usercontext.{TokenTranslator, UserContext}
import io.deepsense.commons.auth.{AuthorizatorProvider, UserContextAuthorizator}
import io.deepsense.commons.buildinfo.BuildInfo
import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import io.deepsense.commons.models.Id
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang.DOperationCategories
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.doperations.FileToDataFrame
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.graph._
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow._
import io.deepsense.models.workflows._
import io.deepsense.workflowmanager.storage._
import io.deepsense.workflowmanager.{WorkflowManager, WorkflowManagerImpl, WorkflowManagerProvider}

class WorkflowsApiSpec
  extends StandardSpec
  with UnitTestSupport
  with ApiSpecSupport
  with WorkflowJsonProtocol
  with InferredStateJsonProtocol
  with WorkflowWithVariablesJsonProtocol
  with WorkflowWithSavedResultsJsonProtocol {

  val catalog = DOperationsCatalog()
  catalog.registerDOperation[FileToDataFrame](
    DOperationCategories.IO,
    "Converts a file to a DataFrame"
  )

  val dOperableCatalog = new DOperableCatalog
  val inferContext: InferContext = InferContext.forTypeInference(dOperableCatalog)
  override val graphReader: GraphReader = new GraphReader(catalog)

  val workflowAId = Workflow.Id.randomId
  val workflowAName = "Very nice workflow&*workflow"
  val workflowA: Workflow = newWorkflow()
  val workflowAWithResults = newWorkflowWithResults(workflowAId, workflowA)
  val nodeAId = Node.Id.randomId

  val workflowB = newWorkflow()
  val workflowBId = Workflow.Id.randomId
  val nodeBId = Node.Id.randomId
  val workflowBWithSavedResults = WorkflowWithSavedResults(
    ExecutionReportWithId.Id.randomId,
    newWorkflowWithResults(workflowBId, workflowB))

  val workflowWithoutNotebookId = Workflow.Id.randomId
  val workflowWithoutNotebook = newWorkflow()

  val noVersionWorkflowId = Workflow.Id.randomId
  val incorrectVersionFormatWorkflowId = Workflow.Id.randomId
  val noVersionWorkflowResultId = Workflow.Id.randomId
  val obsoleteVersionWorkflowResultId = Workflow.Id.randomId
  val incorrectVersionFormatWorkflowResultId = Workflow.Id.randomId

  val noVersionWorkflowJson = JsObject(
    "foo" -> JsString("bar"),
    "thirdPartyData" -> JsObject("notebooks" -> JsObject()))
  val obsoleteVersionWorkflowJson =
    JsObject(
      "metadata" -> JsObject("apiVersion" -> JsString("0.0.0")),
      "thirdPartyData" -> JsObject("notebooks" -> JsObject()))
  val obsoleteVersionWorkflowWithNotebookJson = obsoleteVersionWorkflowJson.copy()
  val incorrectVersionFormatWorkflowJson =
    JsObject(
      "metadata" -> JsObject("apiVersion" -> JsString("foobar")),
      "thirdPartyData" -> JsObject("notebooks" -> JsObject()))

  val noVersionWorkflow = noVersionWorkflowJson.prettyPrint
  val obsoleteVersionWorkflow = obsoleteVersionWorkflowJson.prettyPrint
  val obsoleteVersionWorkflowWithNotebook = obsoleteVersionWorkflowWithNotebookJson.prettyPrint
  val incorrectVersionFormatWorkflow = incorrectVersionFormatWorkflowJson.prettyPrint
  val noVersionWorkflowResult = noVersionWorkflow
  val obsoleteVersionWorkflowResult = obsoleteVersionWorkflow
  val incorrectVersionFormatWorkflowResult = incorrectVersionFormatWorkflow

  val notebookA = "{ \"notebook A content\": {} }"
  val notebookB = "{ \"notebook B content\": {} }"
  val obsoleteNotebook = "{ \"obsolete notebook content\": {} }"

  def newWorkflow(
      apiVersion: String = BuildInfo.version,
      name: String = workflowAName): Workflow = {
    val node1 = Node(Node.Id.randomId, FileToDataFrame())
    val node2 = Node(Node.Id.randomId, FileToDataFrame())
    val graph = DirectedGraph(Set(node1, node2), Set(Edge(node1, 0, node2, 0)))
    val metadata = WorkflowMetadata(WorkflowType.Batch, apiVersion = apiVersion)
    val thirdPartyData = ThirdPartyData(JsObject(
      "gui" -> JsObject(
        "name" -> JsString(name)
      ),
      "notebooks" -> JsObject()
    ).toString)
    Workflow(metadata, graph, thirdPartyData)
  }

  def newWorkflowWithResults(
      id: Workflow.Id,
      wf: Workflow): WorkflowWithResults = {
    val executionReport = ExecutionReport(
      Map(wf.graph.nodes.head.id -> nodestate.Failed(
        DateTimeConverter.now,
        DateTimeConverter.now,
        FailureDescription(DeepSenseFailure.Id.randomId, FailureCode.NodeFailure, "title"))),
      EntitiesMap(),
      None)
    WorkflowWithResults(id, wf.metadata, wf.graph, wf.additionalData, executionReport)
  }

  def cyclicWorkflow: Workflow = {
    val node1 = Node(Node.Id.randomId, FileToDataFrame())
    val node2 = Node(Node.Id.randomId, FileToDataFrame())
    val graph = DirectedGraph(
      Set(node1, node2), Set(Edge(node1, 0, node2, 0), Edge(node2, 0, node1, 0)))
    val metadata = WorkflowMetadata(
      WorkflowType.Batch, apiVersion = BuildInfo.version)
    val thirdPartyData = ThirdPartyData("{}")
    val workflow = Workflow(metadata, graph, thirdPartyData)
    workflow
  }

  val tenantAId: String = "A"
  val tenantBId: String = "B"

  /**
   * A valid Auth Token of a user of tenant A. This user has to have roles
   * for all actions in WorkflowManager
   */
  def validAuthTokenTenantA: String = tenantAId

  /**
   * A valid Auth Token of a user of tenant B. This user has to have no roles.
   */
  def validAuthTokenTenantB: String = tenantBId

  val apiPrefix: String = "v1/workflows"
  val reportsPrefix: String = "v1/reports"

  val roleGet = "workflows:get"
  val roleUpdate = "workflows:update"
  val roleDelete = "workflows:delete"
  val roleCreate = "workflows:create"

 override val authTokens: Map[String, Set[String]] = Map(
    tenantAId -> Set(roleGet, roleUpdate, roleDelete, roleCreate),
    tenantBId -> Set()
  )

  override def createRestComponent(tokenTranslator: TokenTranslator): Route = {
    val workflowManagerProvider = mock[WorkflowManagerProvider]
    val workflowResultsStorage = mockResultsStorage()
    when(workflowManagerProvider.forContext(any(classOf[Future[UserContext]])))
      .thenAnswer(new Answer[WorkflowManager]{
      override def answer(invocation: InvocationOnMock): WorkflowManager = {
        val futureContext = invocation.getArgumentAt(0, classOf[Future[UserContext]])

        val authorizator = new UserContextAuthorizator(futureContext)
        val authorizatorProvider: AuthorizatorProvider = mock[AuthorizatorProvider]
        when(authorizatorProvider.forContext(any(classOf[Future[UserContext]])))
          .thenReturn(authorizator)

        val workflowStorage = mockStorage()
        val workflowStatesStorage = mockStatesStorage()
        when(workflowStatesStorage.get(any()))
          .thenReturn(Future.successful(Map[Node.Id, NodeState]()))
        when(workflowStatesStorage.get(workflowAId))
          .thenReturn(Future.successful(workflowAWithResults.executionReport.states))
        val notebookStorage = mockNotebookStorage()
        new WorkflowManagerImpl(
          authorizatorProvider, workflowStorage, workflowResultsStorage, workflowStatesStorage,
          notebookStorage, inferContext, futureContext, roleGet, roleUpdate, roleDelete, roleCreate)
      }
    })

    new SecureWorkflowApi(
      tokenTranslator,
      workflowManagerProvider,
      apiPrefix,
      reportsPrefix,
      graphReader).route
  }

  s"GET /workflows/:id" should {
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occurs)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "the user does not have the requested role (on NoRoleException)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
    }
    "return Not found" when {
      "asked for non existing Workflow" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
        ()
      }
    }
    "return a workflow with results" when {
      "auth token is correct, user has roles" in {
        Get(s"/$apiPrefix/$workflowAId") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)

          val returnedWorkflow = responseAs[WorkflowWithResults]
          returnedWorkflow should have(
            'id(workflowAId),
            'metadata(workflowA.metadata),
            'graph(workflowA.graph),
            'thirdPartyData(workflowA.additionalData),
            'executionReport(workflowAWithResults.executionReport)
          )
          val thirdPartyData = returnedWorkflow.thirdPartyData.data.parseJson.asJsObject
          thirdPartyData.fields.get("notebook") shouldBe None
        }
        ()
      }
    }
  }

  "GET /workflows" should {
    "list all stored workflows" in {
      Get(s"/$apiPrefix") ~>
        addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
        status should be(StatusCodes.OK)

        responseAs[JsArray].elements.size shouldBe 3
      }
      ()
    }
  }

  s"DELETE /workflows/:id" should {
    "return Not found" when {
      "workflow does not exists" in {
        Delete(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
        ()
      }
    }
    "return Ok" when {
      "workflow existed and is deleted now" in {
        Delete(s"/$apiPrefix/$workflowAId") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
        }
        ()
      }
    }
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occurs)" in {
        Delete(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "the user does not have the requested role (on NoRoleException)" in {
        Delete(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Delete(s"/$apiPrefix/${Workflow.Id.randomId}") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  "GET /workflows/:id/results-upload-time" should {
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occurs)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/results-upload-time") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "the user does not have the requested role (on NoRoleException)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/results-upload-time") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/results-upload-time") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
    }
    "return Not Found" when {
      "workflow does not exist" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/results-upload-time") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
        ()
      }
      "workflow has no execution reports uploaded yet" in {
        Get(s"/$apiPrefix/$workflowAId/results-upload-time") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
        ()
      }
    }
    "return last execution time" when {
      "at least execution report exist" in {
        Get(s"/$apiPrefix/$workflowBId/results-upload-time") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          val response = responseAs[JsObject]
          val lastExecutionKey = "resultsUploadTime"
          response.fields should contain key lastExecutionKey
          response.fields(lastExecutionKey).convertTo[DateTime]
        }
        ()
      }
    }
  }

  s"GET /workflows/:id/download" should {
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occurs)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/download?format=json") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "the user does not have the requested role (on NoRoleException)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/download?format=json") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/download?format=json") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
    }
    "return Not found" when {
      "asked for non existing Workflow" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/download?format=json") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
        ()
      }
    }
    "return an workflow" when {
      "auth token is correct, user has roles and version is current (with notebook)" in {
        Get(s"/$apiPrefix/$workflowAId/download?format=json") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          header("Content-Disposition") shouldBe Some(
            `Content-Disposition`(
              "attachment",
              Map("filename" -> "Very nice workflow__workflow.json")))

          responseAs[WorkflowWithVariables] shouldBe WorkflowWithVariables(
            workflowAId,
            workflowA.metadata,
            workflowA.graph,
            thirdPartyDataWithNotebook(workflowA.additionalData, nodeAId, notebookA),
            Variables()
          )
        }
        ()
      }
      "auth token is correct, user has roles and version is current (without notebook)" in {
        Get(s"/$apiPrefix/$workflowWithoutNotebookId/download?format=json") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          header("Content-Disposition") shouldBe Some(
            `Content-Disposition`(
              "attachment",
              Map("filename" -> "Very nice workflow__workflow.json")))

          responseAs[WorkflowWithVariables] shouldBe WorkflowWithVariables(
            workflowWithoutNotebookId,
            workflowWithoutNotebook.metadata,
            workflowWithoutNotebook.graph,
            workflowWithoutNotebook.additionalData,
            Variables()
          )
        }
        ()
      }
    }
  }

  s"GET /reports/:reportId/download" should {
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occurs)" in {
        Get(s"/$reportsPrefix/${ExecutionReportWithId.Id.randomId}/download") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "the user does not have the requested role (on NoRoleException)" in {
        Get(s"/$reportsPrefix/${ExecutionReportWithId.Id.randomId}/download") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$reportsPrefix/${ExecutionReportWithId.Id.randomId}/download") ~>
          testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
    }
    "return Not found" when {
      "asked for non existing Workflow" in {
        Get(s"/$reportsPrefix/${ExecutionReportWithId.Id.randomId}/download") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
        ()
      }
    }
    "return the report" when {
      "auth token is correct, user has roles" in {
        Get(s"/$reportsPrefix/${workflowBWithSavedResults.executionReport.id}/download") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          header("Content-Disposition") shouldBe Some(
            `Content-Disposition`("attachment", Map("filename" -> "report.json")))

          val returnedWorkflow = responseAs[WorkflowWithSavedResults]
          returnedWorkflow.executionReport.id shouldBe workflowBWithSavedResults.executionReport.id
        }
        ()
      }
      "auth token is correct, user has roles and report has no version" in {
        Get(s"/$reportsPrefix/$noVersionWorkflowResultId/download") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          header("Content-Disposition") shouldBe Some(
            `Content-Disposition`("attachment", Map("filename" -> "report.json")))
          responseAs[JsObject] shouldBe noVersionWorkflowJson
        }
        ()
      }
      "auth token is correct, user has roles and report has unsupported API version" in {
        Get(s"/$reportsPrefix/$obsoleteVersionWorkflowResultId/download") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          header("Content-Disposition") shouldBe Some(
            `Content-Disposition`("attachment", Map("filename" -> "report.json")))

          responseAs[JsObject] shouldBe obsoleteVersionWorkflowJson
        }
        ()
      }
      "auth token is correct, user has roles and report has version in incorrect format" in {
        Get(s"/$reportsPrefix/$incorrectVersionFormatWorkflowResultId/download") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          header("Content-Disposition") shouldBe Some(
            `Content-Disposition`("attachment", Map("filename" -> "report.json")))

          responseAs[JsObject] shouldBe incorrectVersionFormatWorkflowJson
        }
        ()
      }
    }
  }

  "POST /workflows" should {
    "process authorization before reading POST content" in {
      val invalidContent = JsObject()
      Post(s"/$apiPrefix", invalidContent) ~> testRoute ~> check {
        status should be(StatusCodes.Unauthorized)
      }
    }
    "return created" when {
      "inputWorkflow was send" in {
        val createdWorkflow = newWorkflow()
        Post(s"/$apiPrefix", createdWorkflow) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be (StatusCodes.Created)

          val resultJs = response.entity.asString.parseJson.asJsObject
          resultJs.fields should contain key "workflowId"
        }
        ()
      }
    }
    "return BadRequest" when {
      "inputWorkflow contains wrong API version" in {
        val createdWorkflow = newWorkflow(apiVersion = "0.0.1")
        Post(s"/$apiPrefix", createdWorkflow) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)

          assertFailureDescriptionHasVersionInfo(responseAs[FailureDescription])
        }
        ()
      }
    }
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occurs)" in {
        Post(s"/$apiPrefix", workflowA) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Post(s"/$apiPrefix", workflowA) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Post(s"/$apiPrefix", workflowA) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
    }
  }

  "POST /workflows/upload" should {

    "return BadRequest" when {
      "execution report contains wrong API version" in {
        val createdWorkflow = newWorkflow(apiVersion = "0.0.1")

        val multipartData = MultipartFormData(Map(
          "workflowFile" -> BodyPart(HttpEntity(
            ContentType(MediaTypes.`application/json`),
            workflowFormat.write(createdWorkflow).toString())
          )))

        Post(s"/$apiPrefix/upload", multipartData) ~>
          addHeaders(
            RawHeader("X-Auth-Token", validAuthTokenTenantA)) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)

          assertFailureDescriptionHasVersionInfo(responseAs[FailureDescription])
        }
        ()
      }
    }

    "return created" when {
      "workflow file is sent" in {
        val createdWorkflow = newWorkflow()

        val multipartData = MultipartFormData(Map(
          "workflowFile" -> BodyPart(HttpEntity(
            ContentType(MediaTypes.`application/json`),
            workflowFormat.write(createdWorkflow).toString())
          )))

        Post(s"/$apiPrefix/upload", multipartData) ~>
          addHeaders(
            RawHeader("X-Auth-Token", validAuthTokenTenantA)) ~> testRoute ~> check {
          status should be(StatusCodes.Created)

          val resultJs = response.entity.asString.parseJson.asJsObject
          resultJs.fields should contain key "workflowId"
        }
        ()
      }
    }

    "return created" when {
      "workflow file with execution report is sent" in {
        val createdWorkflow = workflowAWithResults

        val multipartData = MultipartFormData(Map(
          "workflowFile" -> BodyPart(HttpEntity(
            ContentType(MediaTypes.`application/json`),
            workflowWithResultsFormat.write(createdWorkflow).toString())
          )))

        Post(s"/$apiPrefix/upload", multipartData) ~>
          addHeaders(
            RawHeader("X-Auth-Token", validAuthTokenTenantA)) ~> testRoute ~> check {
          status should be (StatusCodes.Created)

          val resultJs = response.entity.asString.parseJson.asJsObject
          resultJs.fields should contain key "workflowId"
        }
        ()
      }
    }

    "return BadRequest" when {
      "workflow file with invalid execution report is sent" in {
        val createdWorkflow = workflowAWithResults

        val json = workflowWithResultsFormat.write(createdWorkflow).toString().parseJson
        val modifiedJs = JsObject(json.asJsObject.fields.map {
          case (name: String, node: JsObject) =>
            if ("executionReport" == name) {
              ("executionReport", JsObject(Map("invalid execution report" -> JsObject())))
            } else {
              (name, node)
            }
          case js => js
        })

        val multipartData = MultipartFormData(Map(
          "workflowFile" -> BodyPart(HttpEntity(
            ContentType(MediaTypes.`application/json`),
            modifiedJs.prettyPrint)
          )))

        Post(s"/$apiPrefix/upload", multipartData) ~>
          addHeaders(
            RawHeader("X-Auth-Token", validAuthTokenTenantA)) ~> testRoute ~> check {
          status should be (StatusCodes.BadRequest)

          val failureDescription = responseAs[FailureDescription]
          failureDescription.code shouldBe FailureCode.UnexpectedError
        }
        ()
      }
    }
  }

  "POST /workflows/report/upload" should {

    "return BadRequest" when {
      "execution report contains wrong API version" in {
        val workflowWithWrongAPIVersion =
          workflowAWithResults.copy(
            metadata = workflowAWithResults.metadata.copy(apiVersion = "0.0.1"))

        val multipartData = MultipartFormData(Map(
          "workflowFile" -> BodyPart(HttpEntity(
            ContentType(MediaTypes.`application/json`),
            workflowWithResultsFormat.write(workflowWithWrongAPIVersion).toString())
          )))

        Post(s"/$apiPrefix/report/upload", multipartData) ~>
          addHeaders(
            RawHeader("X-Auth-Token", validAuthTokenTenantA)) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)

          assertFailureDescriptionHasVersionInfo(responseAs[FailureDescription])
        }
        ()
      }
    }

    "return created" when {
      "execution report file is sent" in {
        val multipartData = MultipartFormData(Map(
          "workflowFile" -> BodyPart(HttpEntity(
            ContentType(MediaTypes.`application/json`),
            workflowWithResultsFormat.write(workflowAWithResults).toString())
          )))

        Post(s"/$apiPrefix/report/upload", multipartData) ~>
          addHeaders(
            RawHeader("X-Auth-Token", validAuthTokenTenantA)) ~> testRoute ~> check {
          status should be (StatusCodes.Created)

          val returnedReport = responseAs[WorkflowWithResults]
          returnedReport should have (
            'metadata (workflowAWithResults.metadata),
            'graph (workflowAWithResults.graph),
            'thirdPartyData (workflowAWithResults.thirdPartyData),
            'executionReport (workflowAWithResults.executionReport))
        }
        ()
      }
    }
  }

  s"PUT /workflows/:id" should {
    val workflow = newWorkflow()
    val updatedWorkflow = workflow.copy(
      metadata = workflow.metadata.copy(apiVersion = BuildInfo.version))
    val updatedWorkflowWithNotebook = updatedWorkflow.copy(
      additionalData = ThirdPartyData(notebookA))

    "process authorization before reading PUT content" in {
      val invalidContent = JsObject()
      Put(s"/$apiPrefix/" + Workflow.Id.randomId, invalidContent) ~> testRoute ~> check {
        status should be(StatusCodes.Unauthorized)
      }
    }
    "update the workflow and return Ok" when {
      "user updates his workflow without notebook" in {
        Put(s"/$apiPrefix/$workflowAId", updatedWorkflow) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
        }
        ()
      }
      "user updates his workflow with notebook" in {
        Put(s"/$apiPrefix/$workflowAId", updatedWorkflowWithNotebook) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
        }
        ()
      }
    }
    "return NotFound" when {
      "the workflow does not exist" in {
        val nonExistingId = Workflow.Id.randomId
        Put(s"/$apiPrefix/$nonExistingId", updatedWorkflow) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
        ()
      }
    }
    "return BadRequest" when {
      "updated workflow contains wrong API version" in {
        val wrongUpdatedWorkflow =
          workflow.copy(metadata = workflow.metadata.copy(apiVersion = "0.0.1"))
        Put(s"/$apiPrefix/$workflowAId", wrongUpdatedWorkflow) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)

          assertFailureDescriptionHasVersionInfo(responseAs[FailureDescription])
        }
        ()
      }
    }
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occurs)" in {
        Put(s"/$apiPrefix/" + workflowAId, updatedWorkflow) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Put(s"/$apiPrefix/" + workflowAId, updatedWorkflow) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Put(s"/$apiPrefix/" + workflowAId, updatedWorkflow) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  s"GET /reports/:id" should {
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occurs)" in {
        Get(s"/$reportsPrefix/${ExecutionReportWithId.Id.randomId}") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "the user does not have the requested role (on NoRoleException)" in {
        Get(s"/$reportsPrefix/${ExecutionReportWithId.Id.randomId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$reportsPrefix/${ExecutionReportWithId.Id.randomId}") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
    }
    "return Not found" when {
      "asked for non existing Workflow" in {
        Get(s"/$reportsPrefix/${ExecutionReportWithId.Id.randomId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
        ()
      }
    }
    "return a result" when {
      "auth token is correct, user has roles" in {
        Get(s"/$reportsPrefix/${workflowBWithSavedResults.executionReport.id}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)

          val returnedWorkflow = responseAs[WorkflowWithSavedResults]
          returnedWorkflow.executionReport.id shouldBe workflowBWithSavedResults.executionReport.id
        }
        ()
      }
    }
    "return a conflict" when {
      "incompatible version" in {
        Get(s"/$reportsPrefix/${obsoleteVersionWorkflowResultId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.Conflict)
        }
        ()
      }
    }
  }

  s"GET :id/report" should {
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occurs)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/report") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "the user does not have the requested role (on NoRoleException)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/report") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/report") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
        ()
      }
    }
    "return Not found" when {
      "asked for non existing Workflow" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/report") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
        ()
      }
      "workflow never executed" in {
        Get(s"/$apiPrefix/$workflowAId/report") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
        ()
      }
    }
    "return a result" when {
      "auth token is correct, user has roles" in {
        Get(s"/$apiPrefix/$workflowBId/report") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)

          val returnedWorkflow = responseAs[WorkflowWithSavedResults]
          returnedWorkflow.executionReport.id shouldBe workflowBWithSavedResults.executionReport.id
        }
        ()
      }
    }
  }

  "GET /workflows/:workflowid/notebook/:nodeid" should {
    "return notebook" when {
      "notebook exists" in {
        Get(s"/$apiPrefix/$workflowAId/notebook/$nodeAId") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)

          val returnedNotebook = responseAs[String]
          returnedNotebook shouldBe notebookA
        }
        ()
      }
    }

    "return Not found" when {
      "notebook does not exists" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/notebook/${Node.Id.randomId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
        ()
      }
    }
  }

  "POST /workflows/:workflowid/notebook/:nodeid" should {
    "create notebook" in {
      val notebook = "notebook content"
      Post(s"/$apiPrefix/${Workflow.Id.randomId}/notebook/${Node.Id.randomId}", notebook) ~>
        addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
        status should be(StatusCodes.Created)
      }
      ()
    }
  }

  def assertFailureDescriptionHasVersionInfo(fd: FailureDescription): Unit = {
    fd.code shouldBe FailureCode.IncorrectWorkflow
    fd.details should (
      contain key "workflowApiVersion" and contain key "supportedApiVersion")
  }

  def mockResultsStorage(): WorkflowResultsStorage = {
    val storage = new TestResultsStorage()
    storage.save(workflowBWithSavedResults)
    storage.saveString(noVersionWorkflowResultId, noVersionWorkflowResult)
    storage.saveString(obsoleteVersionWorkflowResultId, obsoleteVersionWorkflowResult)
    storage.saveString(incorrectVersionFormatWorkflowResultId, incorrectVersionFormatWorkflowResult)
    storage
  }

  def mockStatesStorage(): WorkflowStateStorage = {
    mock[WorkflowStateStorage]
  }

  def mockStorage(): WorkflowStorage = {
    val storage = new InMemoryWorkflowStorage()
    storage.create(workflowAId, workflowA)
    storage.create(workflowBId, workflowB)
    storage.create(workflowWithoutNotebookId, workflowWithoutNotebook)
    storage.saveExecutionResults(workflowBWithSavedResults)
    storage
  }

  def mockNotebookStorage(): NotebookStorage = {
    val storage = new TestNotebookStorage
    storage.save(workflowAId, nodeAId, notebookA)
    storage.save(workflowBId, nodeBId, notebookB)
    storage
  }

  private def thirdPartyDataWithNotebook(
      additionalData: ThirdPartyData,
      nodeId: Node.Id,
      notebook: String) = {
    val thirdPartyDataJson = additionalData.data.parseJson.asJsObject
    val notebooks = JsObject(nodeId.toString -> notebook.parseJson)
    ThirdPartyData(
      JsObject(
        thirdPartyDataJson.fields.updated("notebooks", notebooks)).toString)
  }

  private def jsonWithNotebook(
      workflowJson: JsValue,
      nodeId: Node.Id,
      notebook: String) = {
    val thirdPartyData = workflowJson.asJsObject.fields("thirdPartyData")
    val notebooks = JsObject(nodeId.toString -> notebook.parseJson)
    val thirdPartyDataWithNotebook = JsObject(
      thirdPartyData.asJsObject.fields.updated("notebooks", notebooks))
    JsObject(workflowJson.asJsObject.fields.updated("thirdPartyData", thirdPartyDataWithNotebook))
  }

  class TestResultsStorage extends WorkflowResultsStorage {
    val storage = new InMemoryWorkflowResultsStorage()
    val stringStorage: TrieMap[ExecutionReportWithId.Id, String] = TrieMap()

    override def get(
        id: ExecutionReportWithId.Id): Future[Option[Either[String, WorkflowWithSavedResults]]] = {
      storage.get(id).map { x =>
        x.orElse {
          stringStorage.get(id).map(Left(_))
        }
      }
    }

    override def save(results: WorkflowWithSavedResults): Future[Unit] = {
      storage.save(results)
    }

    def saveString(id: Id, results: String): Unit = {
      stringStorage.put(id, results)
    }
  }

  class TestNotebookStorage extends NotebookStorage {

    val notebooks: TrieMap[(Workflow.Id, Node.Id), String] = TrieMap()

    override def get(workflowId: Workflow.Id, nodeId: Node.Id): Future[Option[String]] = {
      Future.successful(notebooks.get((workflowId, nodeId)))
    }

    override def save(workflowId: Workflow.Id, nodeId: Node.Id, notebook: String): Future[Unit] = {
      Future.successful(notebooks.put((workflowId, nodeId), notebook))
    }

    override def getAll(workflowId: Workflow.Id): Future[Map[Node.Id, String]] = {
      Future.successful(notebooks.collect {
        case ((w, nodeId), notebook) if workflowId == w => (nodeId -> notebook)
      }.toMap)
    }
  }
}
