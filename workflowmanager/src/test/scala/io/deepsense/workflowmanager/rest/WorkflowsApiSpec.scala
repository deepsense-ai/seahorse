/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest

import scala.collection.concurrent.TrieMap
import scala.concurrent._

import com.google.common.base.Charsets
import org.joda.time
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
import io.deepsense.commons.{StandardSpec, UnitTestSupport, models}
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
  with WorkflowWithKnowledgeJsonProtocol
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
  val workflowAName = "Very nice workflow&*workflow"
  val (workflowA, knowledgeA) = newWorkflowAndKnowledge()
  val workflowAId = Workflow.Id.randomId
  val workflowAWithResults = newWorkflowWithResults(workflowAId, workflowA)
  val (workflowB, knowledgeB) = newWorkflowAndKnowledge()
  val workflowBId = Workflow.Id.randomId
  val workflowBWithSavedResults = WorkflowWithSavedResults(
    ExecutionReportWithId.Id.randomId,
    newWorkflowWithResults(workflowBId, workflowB)._1)

  val workflowWithoutNotebookId = Workflow.Id.randomId
  val (workflowWithoutNotebook, _) = newWorkflowAndKnowledge()

  val noVersionWorkflowId = Workflow.Id.randomId
  val obsoleteVersionWorkflowId = Workflow.Id.randomId
  val obsoleteVersionWorkflowWithNotebookId = Workflow.Id.randomId
  val incorrectVersionFormatWorkflowId = Workflow.Id.randomId
  val noVersionWorkflowResultId = Workflow.Id.randomId
  val obsoleteVersionWorkflowResultId = Workflow.Id.randomId
  val incorrectVersionFormatWorkflowResultId = Workflow.Id.randomId

  val noVersionWorkflowJson = JsObject(
    "foo" -> JsString("bar"),
    "thirdPartyData" -> JsObject())
  val obsoleteVersionWorkflowJson =
    JsObject(
      "metadata" -> JsObject("apiVersion" -> JsString("0.0.0")),
      "thirdPartyData" -> JsObject())
  val obsoleteVersionWorkflowWithNotebookJson = obsoleteVersionWorkflowJson.copy()
  val incorrectVersionFormatWorkflowJson =
    JsObject(
      "metadata" -> JsObject("apiVersion" -> JsString("foobar")),
      "thirdPartyData" -> JsObject())

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

  def newWorkflowAndKnowledge(apiVersion: String = BuildInfo.version, name: String = workflowAName)
      : (Workflow, GraphKnowledge) = {
    val node1 = Node(Node.Id.randomId, FileToDataFrame())
    val node2 = Node(Node.Id.randomId, FileToDataFrame())
    val graph = StatefulGraph(Set(node1, node2), Set(Edge(node1, 0, node2, 0)))
    val metadata = WorkflowMetadata(WorkflowType.Batch, apiVersion = apiVersion)
    val thirdPartyData = ThirdPartyData(JsObject(
      "gui" -> JsObject(
        "name" -> JsString(name)
      )
    ).toString)
    val knowledge = graph.inferKnowledge(inferContext)
    val workflow = Workflow(metadata, graph, thirdPartyData)
    (workflow, knowledge)
  }

  def newWorkflowWithResults(
      id: Workflow.Id,
      wf: Workflow): (WorkflowWithResults, GraphKnowledge) = {
    val executionReport = ExecutionReport(
      graphstate.Failed(FailureDescription(
        DeepSenseFailure.Id.randomId, FailureCode.NodeFailure, "title")),
      DateTimeConverter.now,
      DateTimeConverter.now,
      Map(Node.Id.randomId -> nodestate.Failed(
        DateTimeConverter.now,
        DateTimeConverter.now,
        FailureDescription(DeepSenseFailure.Id.randomId, FailureCode.NodeFailure, "title"))),
      EntitiesMap())

    val knowledge = wf.graph.inferKnowledge(inferContext)
    val workflow =
      WorkflowWithResults(id, wf.metadata, wf.graph, wf.additionalData, executionReport)
    (workflow, knowledge)
  }

  def cyclicWorkflow: Workflow = {
    val node1 = Node(Node.Id.randomId, FileToDataFrame())
    val node2 = Node(Node.Id.randomId, FileToDataFrame())
    val graph = StatefulGraph(
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
        val notebookStorage = mockNotebookStorage()
        new WorkflowManagerImpl(
          authorizatorProvider, workflowStorage, workflowResultsStorage, notebookStorage,
          inferContext, futureContext, roleGet, roleUpdate, roleDelete, roleCreate)
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
    "return a workflow" when {
      "auth token is correct, user has roles" in {
        Get(s"/$apiPrefix/$workflowAId") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)

          // Checking if WorkflowWithKnowledge response is correct
          // This should be done better, but JsonReader is not available for WorkflowWithKnowledge
          val returnedWorkflow = responseAs[Workflow]
          returnedWorkflow should have(
            'metadata(workflowA.metadata),
            'graph(workflowA.graph),
            'additionalData(workflowA.additionalData)
          )
          val thirdPartyData = returnedWorkflow.additionalData.data.parseJson.asJsObject
          thirdPartyData.fields.get("notebook") shouldBe None

          val resultJs = response.entity.asString.parseJson.asJsObject
          resultJs.fields("knowledge") shouldBe knowledgeA.results.toJson
          resultJs.fields("id") shouldBe workflowAId.toJson
        }
        ()
      }
    }
    "return a conflict" when {
      "incompatible version" in {
        Get(s"/$apiPrefix/${obsoleteVersionWorkflowId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.Conflict)
        }
        ()
      }
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
            thirdPartyDataWithNotebook(workflowA.additionalData, notebookA),
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
      "auth token is correct, user has roles and version is unknown" in {
        Get(s"/$apiPrefix/$noVersionWorkflowId/download?format=json") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          header("Content-Disposition") shouldBe Some(
            `Content-Disposition`(
              "attachment",
              Map("filename" -> "workflow.json")))

          responseAs[JsObject] shouldBe noVersionWorkflowJson
        }
        ()
      }
      "auth token is correct, user has roles and version is in incorrect format" in {
        Get(s"/$apiPrefix/$incorrectVersionFormatWorkflowId/download?format=json") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          header("Content-Disposition") shouldBe Some(
            `Content-Disposition`(
              "attachment",
              Map("filename" -> "workflow.json")))

          responseAs[JsObject] shouldBe incorrectVersionFormatWorkflowJson
        }
        ()
      }
      "auth token is correct, user has roles and version is obsolete" in {
        Get(s"/$apiPrefix/$obsoleteVersionWorkflowId/download?format=json") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          header("Content-Disposition") shouldBe Some(
            `Content-Disposition`(
              "attachment",
              Map("filename" -> "workflow.json")))

          responseAs[JsObject] shouldBe obsoleteVersionWorkflowJson
        }
        ()
      }
      "auth token is correct, user has roles, version is obsolete and notebook is present" in {
        Get(s"/$apiPrefix/$obsoleteVersionWorkflowWithNotebookId/download?format=json") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          header("Content-Disposition") shouldBe Some(
            `Content-Disposition`(
              "attachment",
              Map("filename" -> "workflow.json")))

          val expectedJson = jsonWithNotebook(
            obsoleteVersionWorkflowWithNotebookJson, obsoleteNotebook)
          responseAs[JsObject] shouldBe expectedJson
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
        val (createdWorkflow, knowledge) = newWorkflowAndKnowledge()
        Post(s"/$apiPrefix", createdWorkflow) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be (StatusCodes.Created)

          // Checking if WorkflowWithKnowledge response is correct
          // This should be done better, but JsonReader is not available for WorkflowWithKnowledge
          val savedWorkflow = responseAs[Workflow]
          savedWorkflow should have (
            'metadata (createdWorkflow.metadata),
            'graph (createdWorkflow.graph),
            'additionalData (createdWorkflow.additionalData)
          )
          val resultJs = response.entity.asString.parseJson.asJsObject
          resultJs.fields("knowledge") shouldBe knowledge.results.toJson
          resultJs.fields should contain key "id"
        }
        ()
      }
    }
    "return BadRequest" when {
      "inputWorkflow contains cyclic graph" in {
        Post(s"/$apiPrefix", cyclicWorkflow) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be (StatusCodes.BadRequest)

          val failureDescription = responseAs[FailureDescription]
          failureDescription.code shouldBe FailureCode.IllegalArgumentException
        }
        ()
      }
      "inputWorkflow contains wrong API version" in {
        val (createdWorkflow, knowledge) = newWorkflowAndKnowledge(apiVersion = "0.0.1")
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
        val (createdWorkflow, knowledge) =
          newWorkflowAndKnowledge(apiVersion = "0.0.1")

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
        val (createdWorkflow, knowledge) = newWorkflowAndKnowledge()

        val multipartData = MultipartFormData(Map(
          "workflowFile" -> BodyPart(HttpEntity(
            ContentType(MediaTypes.`application/json`),
            workflowFormat.write(createdWorkflow).toString())
          )))

        Post(s"/$apiPrefix/upload", multipartData) ~>
          addHeaders(
            RawHeader("X-Auth-Token", validAuthTokenTenantA)) ~> testRoute ~> check {
          status should be(StatusCodes.Created)

          // Checking if WorkflowWithKnowledge response is correct
          // This should be done better, but JsonReader is not available for WorkflowWithKnowledge
          val savedWorkflow = responseAs[Workflow]
          savedWorkflow should have(
            'metadata(createdWorkflow.metadata),
            'graph(createdWorkflow.graph),
            'additionalData(createdWorkflow.additionalData))

          val resultJs = response.entity.asString.parseJson.asJsObject
          resultJs.fields("knowledge") shouldBe knowledge.results.toJson
          resultJs.fields should contain key "id"
        }
        ()
      }
    }

    "return created" when {
      "workflow file with execution report is sent" in {
        val (createdWorkflow, knowledge) = workflowAWithResults

        val multipartData = MultipartFormData(Map(
          "workflowFile" -> BodyPart(HttpEntity(
            ContentType(MediaTypes.`application/json`),
            workflowWithResultsFormat.write(createdWorkflow).toString())
          )))

        Post(s"/$apiPrefix/upload", multipartData) ~>
          addHeaders(
            RawHeader("X-Auth-Token", validAuthTokenTenantA)) ~> testRoute ~> check {
          status should be (StatusCodes.Created)

          val savedWorkflow = responseAs[Workflow]
          savedWorkflow should have (
            'metadata (createdWorkflow.metadata),
            'graph (createdWorkflow.graph))

          val resultJs = response.entity.asString.parseJson.asJsObject
          // NOTE: workflowWithKnowledge is returned instead of workflowAWithResults
          resultJs.fields("knowledge") shouldBe knowledge.results.toJson
          resultJs.fields should contain key "id"
        }
        ()
      }
    }

    "return BadRequest" when {
      "workflow file with invalid execution report is sent" in {
        val (createdWorkflow, knowledge) = workflowAWithResults

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

    "return correct encoding" when {
      "workflow with UTF-8 characters is sent" in {

        // scalastyle:off
        val workflowName = "zażółć gęślą jaźń"
        // scalastyle:on

        val (createdWorkflow, knowledge) = newWorkflowAndKnowledge(name = workflowName)

        val multipartData = MultipartFormData(Map(
          "workflowFile" -> BodyPart(HttpEntity(
            ContentType(MediaTypes.`application/json`),
            workflowFormat.write(createdWorkflow).toString.getBytes(Charsets.UTF_8))
          )))

        Post(s"/$apiPrefix/upload", multipartData) ~>
          addHeaders(
            RawHeader("X-Auth-Token", validAuthTokenTenantA)) ~> testRoute ~> check {
          status should be (StatusCodes.Created)

          val savedWorkflow = responseAs[Workflow]
          val savedName = savedWorkflow.additionalData.data.parseJson.asJsObject
            .fields("gui").asJsObject
            .fields("name").asInstanceOf[JsString].value
          savedName shouldBe workflowName
        }
        ()
      }
    }
  }

  "POST /workflows/report/upload" should {

    "return BadRequest" when {
      "execution report contains wrong API version" in {
        val workflowWithWrongAPIVersion =
          workflowAWithResults._1.copy(
            metadata = workflowAWithResults._1.metadata.copy(apiVersion = "0.0.1"))

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
            workflowWithResultsFormat.write(workflowAWithResults._1).toString())
          )))

        Post(s"/$apiPrefix/report/upload", multipartData) ~>
          addHeaders(
            RawHeader("X-Auth-Token", validAuthTokenTenantA)) ~> testRoute ~> check {
          status should be (StatusCodes.Created)

          val returnedReport = responseAs[WorkflowWithResults]
          returnedReport should have (
            'metadata (workflowAWithResults._1.metadata),
            'graph (workflowAWithResults._1.graph),
            'thirdPartyData (workflowAWithResults._1.thirdPartyData),
            'executionReport (workflowAWithResults._1.executionReport))
        }
        ()
      }
    }
  }

  s"PUT /workflows/:id" should {
    val (workflow, knowledge) = newWorkflowAndKnowledge()
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

          // Checking if WorkflowWithKnowledge response is correct
          // This should be done better, but JsonReader is not available for WorkflowWithKnowledge
          val savedWorkflow = responseAs[Workflow]
          savedWorkflow should have(
            'graph (updatedWorkflow.graph),
            'metadata (updatedWorkflow.metadata),
            'additionalData (updatedWorkflow.additionalData)
          )
          val resultJs = response.entity.asString.parseJson.asJsObject
          resultJs.fields("knowledge") shouldBe knowledge.results.toJson
          resultJs.fields("id") shouldBe workflowAId.toJson
        }
        ()
      }
      "user updates his workflow with notebook" in {
        Put(s"/$apiPrefix/$workflowAId", updatedWorkflowWithNotebook) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)

          val savedWorkflow = responseAs[Workflow]
          savedWorkflow should have(
            'graph (updatedWorkflowWithNotebook.graph),
            'metadata (updatedWorkflowWithNotebook.metadata),
            'additionalData (updatedWorkflowWithNotebook.additionalData)
          )

          val resultJs = response.entity.asString.parseJson.asJsObject
          resultJs.fields("knowledge") shouldBe knowledge.results.toJson
          resultJs.fields("id") shouldBe workflowAId.toJson
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
    "return a conflict" when {
      "incompatible version" in {
        Get(s"/$apiPrefix/${obsoleteVersionWorkflowId}/report") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.Conflict)
        }
        ()
      }
    }
  }

  "GET /workflows/:id/notebook" should {
    "return notebook" when {
      "notebook exists" in {
        Get(s"/$apiPrefix/$workflowAId/notebook") ~>
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
        Get(s"/$apiPrefix/${Id.randomId}/notebook") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
        ()
      }
    }
  }

  "POST /workflows/:id/notebook" should {
    "create notebook" in {
      val notebook = "notebook content"
      Post(s"/$apiPrefix/${Id.randomId}/notebook", notebook) ~>
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

  def mockStorage(): WorkflowStorage = {
    val storage = new TestWorkflowStorage()
    storage.save(workflowAId, workflowA)
    storage.save(workflowBId, workflowB)
    storage.save(workflowWithoutNotebookId, workflowWithoutNotebook)
    storage.saveExecutionResults(workflowBWithSavedResults)

    storage.saveString(noVersionWorkflowId, noVersionWorkflow)
    storage.saveString(obsoleteVersionWorkflowId, obsoleteVersionWorkflow)
    storage.saveString(obsoleteVersionWorkflowWithNotebookId, obsoleteVersionWorkflowWithNotebook)
    storage.saveString(incorrectVersionFormatWorkflowId, incorrectVersionFormatWorkflow)
    storage
  }

  def mockNotebookStorage(): NotebookStorage = {
    val storage = new TestNotebookStorage
    storage.save(workflowAId, notebookA)
    storage.save(workflowBId, notebookB)
    storage.save(obsoleteVersionWorkflowWithNotebookId, obsoleteNotebook)
    storage
  }

  private def thirdPartyDataWithNotebook(additionalData: ThirdPartyData, notebook: String) = {
    val thirdPartyDataJson = additionalData.data.parseJson.asJsObject
    ThirdPartyData(
      JsObject(
        thirdPartyDataJson.fields.updated("notebook", notebook.parseJson)).toString)
  }

  private def jsonWithNotebook(workflowJson: JsValue, notebook: String) = {
    val thirdPartyData = workflowJson.asJsObject.fields("thirdPartyData")
    val thirdPartyDataWithNotebook = JsObject(
      thirdPartyData.asJsObject.fields.updated("notebook", notebook.parseJson))
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

  class TestWorkflowStorage extends WorkflowStorage {
    private val storage = new InMemoryWorkflowStorage()
    private val stringStorage: TrieMap[models.Id, String] = TrieMap()

    override def get(id: Id): Future[Option[Either[String, Workflow]]] = {
      storage.get(id).map { _.orElse {
          stringStorage.get(id).map(Left(_))
        }
      }
    }

    override def getLatestExecutionResults(
        workflowId: Id): Future[Option[Either[String, WorkflowWithSavedResults]]] = {
      storage.getLatestExecutionResults(workflowId).map { _.orElse {
          stringStorage.get(workflowId).map(Left(_))
        }
      }
    }

    override def delete(id: Id): Future[Unit] = storage.delete(id)

    override def saveExecutionResults(results: WorkflowWithSavedResults): Future[Unit] =
      storage.saveExecutionResults(results)

    override def getResultsUploadTime(workflowId: Id): Future[Option[time.DateTime]] =
      storage.getResultsUploadTime(workflowId)

    override def save(id: Id, workflow: Workflow): Future[Unit] = storage.save(id, workflow)

    def saveString(id: Id, stringWorkflow: String): Future[Unit] = {
      Future.successful(stringStorage.put(id, stringWorkflow))
    }
  }

  class TestNotebookStorage extends NotebookStorage {

    val notebooks: TrieMap[Id, String] = TrieMap()

    override def get(id: Id): Future[Option[String]] = {
      Future.successful(notebooks.get(id))
    }

    override def save(id: Id, notebook: String): Future[Unit] = {
      Future.successful(notebooks.put(id, notebook))
    }
  }
}
