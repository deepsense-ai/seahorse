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

import scala.collection.concurrent.TrieMap
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.runtime.universe.TypeTag

import akka.testkit._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import spray.http.HttpHeaders.{RawHeader, `Content-Disposition`}
import spray.http._
import spray.json._
import spray.routing.{HttpServiceBase, Route}
import ai.deepsense.commons.auth.usercontext.{TokenTranslator, UserContext}
import ai.deepsense.commons.auth.{AuthorizatorProvider, UserContextAuthorizator}
import ai.deepsense.commons.buildinfo.BuildInfo
import ai.deepsense.commons.datetime.DateTimeConverter
import ai.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import ai.deepsense.commons.{StandardSpec, UnitTestSupport}
import ai.deepsense.deeplang
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.catalogs.SortPriority
import ai.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import ai.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import ai.deepsense.deeplang.doperations.FilterColumns
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.{DOperable, DOperation1To1, DOperationCategories}
import ai.deepsense.graph._
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.json.workflow._
import ai.deepsense.models.workflows._
import ai.deepsense.workflowmanager.storage._
import ai.deepsense.workflowmanager.storage.inmemory.InMemoryWorkflowStorage
import ai.deepsense.workflowmanager.{PresetService, WorkflowManager, WorkflowManagerImpl, WorkflowManagerProvider}

class WorkflowsApiSpec
  extends StandardSpec
  with UnitTestSupport
  with ApiSpecSupport
  with HttpServiceBase
  with WorkflowJsonProtocol
  with InferredStateJsonProtocol
  with WorkflowWithVariablesJsonProtocol
  with WorkflowWithResultsJsonProtocol { self =>

  import WorkflowsApiSpec.MockOperation

  override implicit val routeTestTimeout: RouteTestTimeout = RouteTestTimeout((5 seconds) dilated)

  val catalog = DOperationsCatalog()
  catalog.registerDOperation(DOperationCategories.Transformation, () => new MockOperation(), SortPriority.coreDefault)

  val dOperableCatalog = new DOperableCatalog
  override val graphReader: GraphReader = new GraphReader(catalog)

  val authUser = "authUser"
  val authPass = "authPass"
  val credentials = BasicHttpCredentials(authUser, authPass)
  val invalidCredentials = BasicHttpCredentials("invalid", "credentials")

  val ownerId = "ownerid"
  val ownerName = "ownername"

  val nodeAId = Node.Id.randomId
  val workflowAId = Workflow.Id.randomId
  val workflowAName = "Very nice workflow&*workflow"
  val workflowA: Workflow = newWorkflow()
  val workflowAWithResults = newWorkflowWithResults(workflowAId, workflowA)

  val workflowB = newWorkflow()
  val workflowBId = Workflow.Id.randomId
  val nodeBId = Node.Id.randomId

  val workflowWithoutNotebookId = Workflow.Id.randomId
  val workflowWithoutNotebook = newWorkflow()

  val noVersionWorkflowId = Workflow.Id.randomId
  val incorrectVersionFormatWorkflowId = Workflow.Id.randomId
  val noVersionWorkflowResultId = Workflow.Id.randomId

  val noVersionWorkflowJson = JsObject(
    "foo" -> JsString("bar"),
    "thirdPartyData" -> JsObject("notebooks" -> JsObject(), "datasources" -> JsArray()))
  val noVersionWorkflow = noVersionWorkflowJson.prettyPrint
  val noVersionWorkflowResult = noVersionWorkflow

  val notebookA = JsObject("notebook A content" -> JsObject())
  val notebookB = "{ \"notebook B content\": {} }"
  val obsoleteNotebook = "{ \"obsolete notebook content\": {} }"

  val obsoleteVersion = "0.0.0"
  val obsoleteVersionWorkflowId = Workflow.Id.randomId
  val obsoleteVersionWorkflow =
    workflowA.copy(
      metadata = workflowA.metadata.copy(apiVersion = obsoleteVersion))
  val obsoleteVersionWorkflowWithResults =
    newWorkflowWithResults(obsoleteVersionWorkflowId, obsoleteVersionWorkflow)

  def newWorkflow(
      apiVersion: String = BuildInfo.version,
      name: String = workflowAName): Workflow = {
    val node1 = Node(nodeAId, MockOperation())
    val node2 = Node(Node.Id.randomId, MockOperation())
    val graph = DeeplangGraph(Set(node1, node2), Set(Edge(node1, 0, node2, 0)))
    val metadata = WorkflowMetadata(WorkflowType.Batch, apiVersion = apiVersion)
    val thirdPartyData = JsObject(
      "gui" -> JsObject(
        "name" -> JsString(name)
      ),
      "notebooks" -> JsObject(),
      "datasources" -> JsArray()
    )
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
    WorkflowWithResults(id, wf.metadata, wf.graph, wf.additionalData,
      executionReport, WorkflowInfo.forId(id))
  }

  def cyclicWorkflow: Workflow = {
    val node1 = Node(Node.Id.randomId, new FilterColumns)
    val node2 = Node(Node.Id.randomId, new FilterColumns)
    val graph = DeeplangGraph(
      Set(node1, node2), Set(Edge(node1, 0, node2, 0), Edge(node2, 0, node1, 0)))
    val metadata = WorkflowMetadata(
      WorkflowType.Batch, apiVersion = BuildInfo.version)
    val thirdPartyData = JsObject()
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

  val fakeDatasourcesServerAddress = "http://mockedHttpAddress/"
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
          authorizatorProvider, workflowStorage, workflowStatesStorage,
          notebookStorage, futureContext, fakeDatasourcesServerAddress, roleGet, roleUpdate, roleDelete, roleCreate)
      }
    })

    val presetsServiceMock = mock[PresetService]

    new InsecureWorkflowApi(
      tokenTranslator,
      workflowManagerProvider,
      apiPrefix,
      reportsPrefix,
      authUser,
      authPass,
      presetsServiceMock,
      graphReader).route
  }

  s"GET /workflows/:id" should {
    "return BadRequest" when {
      "no auth headers were sent (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addCredentials(credentials) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)
        }
      }
    }
    "return Not found" when {
      "asked for non existing Workflow" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return a workflow with results" when {
      "auth token is correct, user has roles" in {
        Get(s"/$apiPrefix/$workflowAId") ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.OK)

          val returnedWorkflow = responseAs[WorkflowWithResults]
          returnedWorkflow should have(
            'id(workflowAId),
            'metadata(workflowA.metadata),
            'graph(workflowA.graph),
            'thirdPartyData(workflowA.additionalData),
            'executionReport(workflowAWithResults.executionReport)
          )
          val thirdPartyData = returnedWorkflow.thirdPartyData
          thirdPartyData.fields.get("notebook") shouldBe None
        }
      }
    }
    "return BadRequest" when {
      "workflow's API version is not compatible with current build" in {
        Get(s"/$apiPrefix/$obsoleteVersionWorkflowId") ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)

          assertFailureDescriptionHasVersionInfo(responseAs[FailureDescription])
        }
      }
    }
  }

  "GET /workflows" should {
    "list all stored workflows" in {
      Get(s"/$apiPrefix") ~>
        addCredentials(credentials) ~>
        addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
        status should be(StatusCodes.OK)

        responseAs[JsArray].elements.size shouldBe 4
      }
    }

    "return Unauthorized" when {
      "no credentials were sent" in {
        Get(s"/$apiPrefix") ~>
          addHeaders(validHeadersIdOnly()) ~> sealRoute(testRoute) ~> check {
          status should be(StatusCodes.Unauthorized)
          header[HttpHeaders.`WWW-Authenticate`].get.challenges.head shouldBe a[HttpChallenge]
        }
      }
      "invalid credentials were sent" in {
        Get(s"/$apiPrefix") ~>
          addCredentials(invalidCredentials) ~>
          addHeaders(validHeadersIdOnly()) ~> sealRoute(testRoute) ~> check {
          status should be(StatusCodes.Unauthorized)
          header[HttpHeaders.`WWW-Authenticate`].get.challenges.head shouldBe a[HttpChallenge]
        }
      }
    }
  }

  s"DELETE /workflows/:id" should {
    "return Not found" when {
      "workflow does not exists" in {
        Delete(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Ok" when {
      "workflow existed and is deleted now" in {
        Delete(s"/$apiPrefix/$workflowAId") ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
        }
      }
    }
    "return BadRequest" when {
      "no auth headers were sent (on MissingHeaderRejection)" in {
        Delete(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addCredentials(credentials) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)
        }
      }
    }
    "return Unauthorized" when {
      "the owner of the workflow is different" in {
        Delete(s"/$apiPrefix/$workflowAId") ~>
          addCredentials(credentials) ~>
          addHeaders(differentOwnerHeaders()) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  s"GET /workflows/:id/download" should {
    "return BadRequest" when {
      "no auth headers were sent (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/download?format=json&export-datasources=true") ~>
          addCredentials(credentials) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)
        }
      }
    }
    "return Not found" when {
      "asked for non existing Workflow" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/download?format=json&export-datasources=true") ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return an workflow" when {
      "auth token is correct, user has roles and version is current (with notebook)" in {
        Get(s"/$apiPrefix/$workflowAId/download?format=json&export-datasources=true") ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          header("Content-Disposition") shouldBe Some(
            `Content-Disposition`(
              "attachment",
              Map("filename" -> "Very_nice_workflow__workflow.json")))

          responseAs[WorkflowWithVariables] shouldBe WorkflowWithVariables(
            workflowAId,
            workflowA.metadata,
            workflowA.graph,
            thirdPartyDataWithNotebook(workflowA.additionalData, nodeAId, notebookA),
            Variables()
          )
        }
      }
      "auth token is correct, user has roles and version is current (without notebook)" in {
        Get(s"/$apiPrefix/$workflowWithoutNotebookId/download?format=json&export-datasources=true") ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          header("Content-Disposition") shouldBe Some(
            `Content-Disposition`(
              "attachment",
              Map("filename" -> "Very_nice_workflow__workflow.json")))

          responseAs[WorkflowWithVariables] shouldBe WorkflowWithVariables(
            workflowWithoutNotebookId,
            workflowWithoutNotebook.metadata,
            workflowWithoutNotebook.graph,
            workflowWithoutNotebook.additionalData,
            Variables()
          )
        }
      }
    }
  }

  "POST /workflows" should {
    "process authorization before reading POST content" in {
      val invalidContent = JsObject()
      Post(s"/$apiPrefix", invalidContent) ~>
        addCredentials(credentials) ~> testRoute ~> check {
        status should be(StatusCodes.BadRequest)
      }
    }
    "return created" when {
      "inputWorkflow was send" in {
        val createdWorkflow = newWorkflow()
        Post(s"/$apiPrefix", createdWorkflow) ~>
          addCredentials(credentials) ~>
          addHeaders(validHeaders()) ~> testRoute ~> check {
          status should be (StatusCodes.Created)

          val resultJs = response.entity.asString.parseJson.asJsObject
          resultJs.fields should contain key "workflowId"
        }
      }
    }
    "return BadRequest" when {
      "inputWorkflow contains wrong API version" in {
        val createdWorkflow = newWorkflow(apiVersion = "0.0.1")
        Post(s"/$apiPrefix", createdWorkflow) ~>
          addCredentials(credentials) ~>
          addHeaders(validHeaders()) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)

          assertFailureDescriptionHasVersionInfo(responseAs[FailureDescription])
        }
      }
      "no auth headers were sent (on MissingHeaderRejection)" in {
        Post(s"/$apiPrefix", workflowA) ~>
          addCredentials(credentials) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)
        }
      }
      "only id auth header was sent (on MissingHeaderRejection)" in {
        Post(s"/$apiPrefix", workflowA) ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)
        }
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
          addCredentials(credentials) ~>
          addHeaders(validHeaders()) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)

          assertFailureDescriptionHasVersionInfo(responseAs[FailureDescription])
        }
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
          addCredentials(credentials) ~>
          addHeaders(validHeaders()) ~> testRoute ~> check {
          status should be(StatusCodes.Created)

          val resultJs = response.entity.asString.parseJson.asJsObject
          resultJs.fields should contain key "workflowId"
        }
      }
    }
  }

  "POST /workflows/:id/clone" should {
    val description = JsObject(
      "name" -> JsString("Linear Regression"),
      "description" -> JsString("Use case 100")
    )

    "return NotFound" when {
      "workflow with specified id does not exist" in {
        Post(s"/$apiPrefix/${Workflow.Id.randomId}/clone", description) ~>
          addCredentials(credentials) ~>
          addHeaders(validHeaders()) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }

    "return Created" when {
      "workflow with specified id exists" in {
        Post(s"/$apiPrefix/$workflowAId/clone", description) ~>
          addCredentials(credentials) ~>
          addHeaders(validHeaders()) ~> testRoute ~> check {
          status should be(StatusCodes.Created)

          val resultJs = response.entity.asString.parseJson.asJsObject
          resultJs.fields should contain key "workflowId"
          val JsString(clonedWorkflowId) = resultJs.fields("workflowId")
          clonedWorkflowId should not equal workflowAId
        }
      }
    }
  }

  s"PUT /workflows/:id" should {
    val workflowWithResults = newWorkflowWithResults(workflowAId, newWorkflow())
    val updatedWorkflowWithResults = workflowWithResults.copy(
      metadata = workflowWithResults.metadata.copy(apiVersion = BuildInfo.version))
    val updatedWorkflowWithResultsWithNotebook = workflowWithResults.copy(
      thirdPartyData = notebookA)

    "process authorization before reading PUT content" in {
      val invalidContent = JsObject()
      Put(s"/$apiPrefix/" + Workflow.Id.randomId, invalidContent) ~>
        addCredentials(credentials) ~> testRoute ~> check {
        status should be(StatusCodes.BadRequest)
      }
    }
    "update the workflow and return Ok" when {
      "user updates his workflow without notebook" in {
        Put(s"/$apiPrefix/$workflowAId", updatedWorkflowWithResults) ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
        }
      }
      "return BadRequest" when {
        "workflow's API version is not compatible with current build" in {
          Put(s"/$apiPrefix/$workflowAId", obsoleteVersionWorkflowWithResults) ~>
            addCredentials(credentials) ~>
            addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
            status should be(StatusCodes.BadRequest)

            assertFailureDescriptionHasVersionInfo(responseAs[FailureDescription])
          }
        }
      }
      "user updates his workflow with notebook" in {
        Put(s"/$apiPrefix/$workflowAId", updatedWorkflowWithResultsWithNotebook) ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
        }
      }
    }
    "return NotFound" when {
      "the workflow does not exist" in {
        val nonExistingId = Workflow.Id.randomId
        Put(s"/$apiPrefix/$nonExistingId", updatedWorkflowWithResults) ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return BadRequest" when {
      "updated workflow contains wrong API version" in {
        val wrongUpdatedWorkflow = workflowWithResults
          .copy(metadata = workflowWithResults.metadata.copy(apiVersion = "0.0.1"))
        Put(s"/$apiPrefix/$workflowAId", wrongUpdatedWorkflow) ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)

          assertFailureDescriptionHasVersionInfo(responseAs[FailureDescription])
        }
      }
      "no auth headers were sent (on MissingHeaderRejection)" in {
        Put(s"/$apiPrefix/" + workflowAId, updatedWorkflowWithResults) ~>
          addCredentials(credentials) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)
        }
      }
    }
    "return Unauthorized" when {
      "the owner of the workflow is different" in {
        Put(s"/$apiPrefix/" + workflowAId, updatedWorkflowWithResults) ~>
          addCredentials(credentials) ~>
          addHeaders(differentOwnerHeaders()) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  "GET /workflows/:workflowid/notebook/:nodeid" should {
    "return notebook" when {
      "notebook exists" in {
        Get(s"/$apiPrefix/$workflowAId/notebook/$nodeAId") ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.OK)

          val returnedNotebook = responseAs[String]
          returnedNotebook shouldBe notebookA.compactPrint
        }
      }
    }

    "return Not found" when {
      "notebook does not exists" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}/notebook/${Node.Id.randomId}") ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
  }

  "POST /workflows/:workflowid/notebook/:nodeid" should {
    "create notebook" in {
      val notebook = "notebook content"
      Post(s"/$apiPrefix/${Workflow.Id.randomId}/notebook/${Node.Id.randomId}", notebook) ~>
        addCredentials(credentials) ~>
        addHeaders(validHeaders()) ~> testRoute ~> check {
        status should be(StatusCodes.Created)
      }
    }
  }

  "POST /workflows/:workflowid/notebook/:sourcenodeid/copy/:destinationnodeid" should {
    "copy notebook" in {
      val notebook = "notebook content"
      Post(s"/$apiPrefix/${Workflow.Id.randomId}/" +
        s"notebook/${Node.Id.randomId}/copy/${Node.Id.randomId}", notebook) ~>
        addCredentials(credentials) ~>
        addHeaders(validHeaders()) ~> testRoute ~> check {
        status should be(StatusCodes.Created)
      }
    }
  }

  s"PUT /reports/:id" should {
    val executionReport = workflowAWithResults.executionReport
    "return BadRequest" when {
      "no auth headers were sent (on MissingHeaderRejection)" in {
        Put(s"/$reportsPrefix/${Workflow.Id.randomId}", executionReport) ~>
          addCredentials(credentials) ~>
          testRoute ~> check {
          status should be(StatusCodes.BadRequest)
        }
      }
    }
    "save execution report" when {
      "auth token is correct, user has roles" in {
        Put(s"/$reportsPrefix/${workflowAWithResults.id}", executionReport) ~>
          addCredentials(credentials) ~>
          addHeaders(validHeadersIdOnly()) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
        }
      }
    }
  }

  def validHeaders(): List[RawHeader] = {
    List(
      RawHeader("X-Seahorse-UserId", ownerId),
      RawHeader("X-Seahorse-UserName", ownerName)
    )
  }

  def validHeadersIdOnly(): List[RawHeader] =
    List(RawHeader("X-Seahorse-UserId", ownerId))

  def differentOwnerHeaders(): List[RawHeader] = {
    List(
      RawHeader("X-Seahorse-UserId", s"not $ownerId"),
      RawHeader("X-Seahorse-UserName", s"not $ownerName")
    )
  }

  def assertFailureDescriptionHasVersionInfo(fd: FailureDescription): Unit = {
    fd.code shouldBe FailureCode.IncorrectWorkflow
    fd.details should (
      contain key "workflowApiVersion" and contain key "supportedApiVersion")
  }

  def mockStatesStorage(): WorkflowStateStorage = {
    val workflowStatesStorage = mock[WorkflowStateStorage]
    when(workflowStatesStorage.save(any(), any()))
      .thenReturn(Future.successful[Unit](Unit))
    workflowStatesStorage
  }

  def mockStorage(): WorkflowStorage = {
    val storage = new InMemoryWorkflowStorage() {
      override val graphReader: GraphReader = self.graphReader
    }
    storage.create(workflowAId, workflowA, ownerId, ownerName)
    storage.create(workflowBId, workflowB, ownerId, ownerName)
    storage.create(workflowWithoutNotebookId, workflowWithoutNotebook, ownerId, ownerName)
    storage.create(obsoleteVersionWorkflowId, obsoleteVersionWorkflow, ownerId, ownerName)
    storage
  }

  def mockNotebookStorage(): NotebookStorage = {
    val storage = new TestNotebookStorage
    storage.save(workflowAId, nodeAId, notebookA.compactPrint)
    storage.save(workflowBId, nodeBId, notebookB)
    storage
  }

  private def thirdPartyDataWithNotebook(
      additionalData: JsObject,
      nodeId: Node.Id,
      notebook: JsObject) = {
    val thirdPartyDataJson = additionalData
    val notebooks = JsObject(nodeId.toString -> notebook)
    JsObject(thirdPartyDataJson.fields.updated("notebooks", notebooks))
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
        case ((w, nodeId), notebook) if workflowId == w => nodeId -> notebook
      }.toMap)
    }
  }
}

object WorkflowsApiSpec {
  case class MockOperation() extends DOperation1To1[DOperable, DOperable] {
    override val id: Id = "7814b1ae-a24d-11e5-bf7f-feff819cdc9f"
    override val name: String = "mock operation"
    override val description: String = "mock operation desc"
    override val specificParams = Array[Param[_]]()

    @transient
    override lazy val tTagTI_0: TypeTag[DOperable] = typeTag[DOperable]

    @transient
    override lazy val tTagTO_0: TypeTag[DOperable] = typeTag[DOperable]

    override protected def execute(t0: DOperable)(ctx: deeplang.ExecutionContext): DOperable = ???
  }
}
