/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import scala.concurrent.Future

import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import spray.http.HttpHeaders.RawHeader
import spray.http.{BasicHttpCredentials, HttpChallenge, HttpHeaders, StatusCodes}
import spray.json._
import spray.routing._

import ai.deepsense.commons.auth.usercontext.{TokenTranslator, UserContext}
import ai.deepsense.commons.auth.{AuthorizatorProvider, UserContextAuthorizator}
import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.commons.rest.ClusterDetailsJsonProtocol
import ai.deepsense.commons.{StandardSpec, UnitTestSupport}
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.json.workflow.{InferredStateJsonProtocol, WorkflowJsonProtocol, WorkflowWithResultsJsonProtocol, WorkflowWithVariablesJsonProtocol}
import ai.deepsense.models.workflows.Workflow
import ai.deepsense.workflowmanager.model.WorkflowPreset
import ai.deepsense.workflowmanager.model.WorkflowPresetJsonProtocol._
import ai.deepsense.workflowmanager.storage.{NotebookStorage, WorkflowStateStorage, WorkflowStorage}
import ai.deepsense.workflowmanager.{PresetService, WorkflowManager, WorkflowManagerImpl, WorkflowManagerProvider}


class PresetApiSpec
  extends StandardSpec
    with UnitTestSupport
    with ApiSpecSupport
    with HttpServiceBase
    with WorkflowJsonProtocol
    with InferredStateJsonProtocol
    with WorkflowWithVariablesJsonProtocol
    with WorkflowWithResultsJsonProtocol
    with ClusterDetailsJsonProtocol {

  val presetPrefix: String = "v1/presets"
  val apiPrefix: String = "v1/workflows"
  val reportsPrefix: String = "v1/reports"

  override val graphReader: GraphReader = mock[GraphReader]

  val authUser = "authUser"
  val authPass = "authPass"
  val credentials = BasicHttpCredentials(authUser, authPass)
  val invalidCredentials = BasicHttpCredentials("invalid", "credentials")

  val workflowAId = Workflow.Id.randomId
  val workflowBId = Workflow.Id.randomId

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

  val fakeDatasourcesServerAddress = "http://mockedHttpAddress/"
  val roleGet = "workflows:get"
  val roleUpdate = "workflows:update"
  val roleDelete = "workflows:delete"
  val roleCreate = "workflows:create"

  override val authTokens: Map[String, Set[String]] = Map(
    tenantAId -> Set(roleGet, roleUpdate, roleDelete, roleCreate),
    tenantBId -> Set()
  )


  var presetsServiceMock = mock[PresetService]
  override def createRestComponent(tokenTranslator: TokenTranslator): Route = {
    val workflowManagerProvider = mock[WorkflowManagerProvider]
    when(workflowManagerProvider.forContext(any(classOf[Future[UserContext]])))
      .thenAnswer(new Answer[WorkflowManager] {
        override def answer(invocation: InvocationOnMock): WorkflowManager = {
          val futureContext = invocation.getArgumentAt(0, classOf[Future[UserContext]])

          val authorizator = new UserContextAuthorizator(futureContext)
          val authorizatorProvider: AuthorizatorProvider = mock[AuthorizatorProvider]
          when(authorizatorProvider.forContext(any(classOf[Future[UserContext]])))
            .thenReturn(authorizator)

          val workflowStorage = mock[WorkflowStorage]
          val workflowStatesStorage = mock[WorkflowStateStorage]
          val notebookStorage = mock[NotebookStorage]
          new WorkflowManagerImpl(
            authorizatorProvider, workflowStorage, workflowStatesStorage,
            notebookStorage, futureContext, fakeDatasourcesServerAddress,
            roleGet, roleUpdate, roleDelete, roleCreate)
        }
      })


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

  val clusterDetails =
    new ClusterDetails(Option(2L), "Cluster", "yarn", "10.10.10.10", "12.12.12.12")

  s"GET /presets/:id" should {
    "return Unauthorized when no auth headers were sent" in {
      Get(s"/$presetPrefix/1") ~>
        sealRoute(testRoute) ~> check {
        status should be(StatusCodes.Unauthorized)
      }
    }
    "return Not Found when preset not found" in {
      presetsServiceMock = mock[PresetService]

      when(presetsServiceMock.getPreset(2)).thenReturn(Future.successful(None))
      Get(s"/$presetPrefix/2") ~>
        addCredentials(credentials) ~> testRoute ~> check {
        status should be(StatusCodes.NotFound)
      }
    }

    "return OK and preset id when is present" in {
      presetsServiceMock = mock[PresetService]
      when(presetsServiceMock.getPreset(2)).thenReturn(Future.successful(Some(clusterDetails)))
      Get(s"/$presetPrefix/2") ~>
        addCredentials(credentials) ~> testRoute ~> check {
        status should be(StatusCodes.OK)
        val returnedClusterDetails = responseAs[ClusterDetails]
        returnedClusterDetails shouldBe clusterDetails
      }
    }
  }

  s"POST /presets/:id" should {
    "process authorization before reading POST content" in {
      val invalidContent = JsObject()
      Post(s"/$presetPrefix/2", invalidContent) ~>
        addCredentials(credentials) ~> testRoute ~> check {
        status should be(StatusCodes.BadRequest)
      }
    }



    "return OK and preset when is present" in {
      presetsServiceMock = mock[PresetService]

      when(presetsServiceMock.updatePreset(2L, clusterDetails)).thenReturn(Future.successful(2L))
      Post(s"/$presetPrefix/2", clusterDetails) ~>
        addCredentials(credentials) ~> testRoute ~> check {
        status should be(StatusCodes.OK)
      }
    }

    "return Internal Server Error when preset is not present" in {
      presetsServiceMock = mock[PresetService]
      when(presetsServiceMock.updatePreset(2L, clusterDetails)).thenReturn(
        Future.failed(new Exception("The call failed!!")))
      Post(s"/$presetPrefix/2", clusterDetails) ~>
        addCredentials(credentials) ~> testRoute ~> check {
        // TODO check if we want to response code to be 500
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  s"DELETE /presets/:id" should {
    "return Unauthorized when no auth headers were sent" in {
      Delete(s"/$presetPrefix/1") ~>
        sealRoute(testRoute) ~> check {
        status should be(StatusCodes.Unauthorized)
      }
    }

    "return OK" in {
      presetsServiceMock = mock[PresetService]
      when(presetsServiceMock.removePreset(2)).thenReturn(Future.successful(()))
      Delete(s"/$presetPrefix/2") ~>
        addCredentials(credentials) ~> testRoute ~> check {
        status should be(StatusCodes.OK)
      }
    }
  }

  s"GET /presets" should {
    "return Unauthorized when no auth headers were sent" in {
      Get(s"/$presetPrefix") ~>
        sealRoute(testRoute) ~> check {
        status should be(StatusCodes.Unauthorized)
      }
    }

    "return OK and presets when is present" in {
      presetsServiceMock = mock[PresetService]
      val seq : Seq[ClusterDetails] = List(clusterDetails, clusterDetails)
      when(presetsServiceMock.listPresets()).thenReturn(Future.successful(seq))
      Get(s"/$presetPrefix") ~>
        addCredentials(credentials) ~> testRoute ~> check {
        status should be(StatusCodes.OK)
        val returnedClusterDetails = responseAs[Seq[ClusterDetails]]
        returnedClusterDetails.size shouldBe 2
      }
    }
  }

  s"POST /presets" should {
    "return Unauthorized when no auth headers were sent" in {
      val invalidContent = JsObject()
      Post(s"/$presetPrefix", invalidContent) ~> sealRoute(testRoute) ~> check {
        status should be(StatusCodes.Unauthorized)
      }
    }

    "process authorization before reading POST content" in {
        val invalidContent = JsObject()
        Post(s"/$presetPrefix", invalidContent) ~>
          addCredentials(credentials) ~> testRoute ~> check {
          status should be(StatusCodes.BadRequest)
        }
    }
    "add preset" in {
      when(presetsServiceMock.createPreset(clusterDetails)).thenReturn(Future.successful(12L))
      Post(s"/$presetPrefix", clusterDetails) ~>
        addCredentials(credentials) ~> testRoute ~> check {
        status should be(StatusCodes.Created)
        val loc = header("Location")
        loc.isDefined should be(true)
        loc.get.value should be("12")
      }
    }
  }

  s"GET preset to workflows" should {
    "return ClusterDetails for workflow" in {
      presetsServiceMock = mock[PresetService]
      when(presetsServiceMock.getWorkflowsPreset(workflowAId)).
        thenReturn(Future.successful(Some(clusterDetails)))
      Get(s"/$apiPrefix/$workflowAId/preset") ~>
        addCredentials(credentials) ~>
        addHeaders(validHeaders()) ~> testRoute ~> check {
        status should be(StatusCodes.OK)
        val returnedClusterDetails = responseAs[ClusterDetails]
        returnedClusterDetails.id shouldBe(Some(2L))
        returnedClusterDetails.name shouldBe("Cluster")
      }
    }

    "return Unauthorized when no auth headers were sent" in {
      Get(s"/$apiPrefix/$workflowAId/preset") ~>
        sealRoute(testRoute) ~> check {
        status should be(StatusCodes.Unauthorized)
      }
    }
    "return Not Found when preset not found" in {
      presetsServiceMock = mock[PresetService]

      when(presetsServiceMock.getWorkflowsPreset(workflowAId)).thenReturn(Future.successful(None))
      Get(s"/$apiPrefix/$workflowAId/preset") ~>
        addCredentials(credentials) ~> addHeaders(validHeaders()) ~> testRoute ~> check {
        status should be(StatusCodes.NotFound)
      }
    }

    "return Bad Request when wrong Headers" in {
      presetsServiceMock = mock[PresetService]

      when(presetsServiceMock.getWorkflowsPreset(workflowAId)).thenReturn(Future.successful(None))
      Get(s"/$apiPrefix/$workflowAId/preset") ~>
        addCredentials(credentials)  ~> testRoute ~> check {
        status should be(StatusCodes.BadRequest)
      }
    }
  }

  s"POST preset to workflows" should {
    "add workflow preset" in {
      presetsServiceMock = mock[PresetService]
      val workflowPreset = WorkflowPreset(workflowAId, 2L)
      when(presetsServiceMock.saveWorkflowsPreset(
        any(), Matchers.eq(workflowAId), Matchers.eq(workflowPreset))).
        thenReturn(Future.successful(()))
      Post(s"/$apiPrefix/$workflowAId/preset", workflowPreset) ~>
        addCredentials(credentials) ~>
        addHeaders(validHeaders()) ~> testRoute ~> check {
        status should be(StatusCodes.OK)
      }
    }

    "return BadRequest when workflowId in preset doesn't match id in POST request" in {
      presetsServiceMock = mock[PresetService]
      val workflowPreset = WorkflowPreset(workflowBId, 2L)
      Post(s"/$apiPrefix/$workflowAId/preset", workflowPreset) ~>
        addCredentials(credentials) ~>
        addHeaders(validHeaders()) ~> testRoute ~> check {
        status should be(StatusCodes.BadRequest)
      }
    }
    "return Unauthorized when no auth headers were sent" in {
      val workflowPreset = WorkflowPreset(workflowAId, 2L)
      Post(s"/$apiPrefix/$workflowAId/preset", workflowPreset) ~>
        sealRoute(testRoute) ~> check {
        status should be(StatusCodes.Unauthorized)
      }
    }

    "return BadRequest when no valid header is passed" in {
      presetsServiceMock = mock[PresetService]
      val workflowPreset = WorkflowPreset(workflowAId, 2L)
      when(presetsServiceMock.saveWorkflowsPreset(
        any(), Matchers.eq(workflowAId), Matchers.eq(workflowPreset))).
        thenReturn(Future.successful(()))
      Post(s"/$apiPrefix/$workflowAId/preset", workflowPreset) ~>
        addCredentials(credentials) ~> testRoute ~> check {
        status should be(StatusCodes.BadRequest)
      }
    }

    "return Unauthorized when invalid credentials are passed" in {
      presetsServiceMock = mock[PresetService]
      val workflowPreset = WorkflowPreset(workflowAId, 2L)
      when(presetsServiceMock.saveWorkflowsPreset(
        any(), Matchers.eq(workflowAId), Matchers.eq(workflowPreset))).
        thenReturn(Future.successful(()))
      Post(s"/$apiPrefix/$workflowAId/preset", workflowPreset) ~>
        addCredentials(invalidCredentials) ~>
        addHeaders(validHeaders()) ~> sealRoute(testRoute) ~> check {
        status should be(StatusCodes.Unauthorized)
        header[HttpHeaders.`WWW-Authenticate`].get.challenges.head shouldBe a[HttpChallenge]
      }
    }


  }

  val ownerId = "ownerid"
  val ownerName = "ownername"
  def validHeaders(): List[RawHeader] = {
    List(
      RawHeader("X-Seahorse-UserId", ownerId),
      RawHeader("X-Seahorse-UserName", ownerName)
    )
  }

}

