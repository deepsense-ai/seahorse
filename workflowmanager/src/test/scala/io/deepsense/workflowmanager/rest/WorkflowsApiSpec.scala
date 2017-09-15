/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest

import scala.concurrent._

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import spray.http.StatusCodes
import spray.json._
import spray.routing.Route

import io.deepsense.commons.auth.exceptions.{NoRoleException, ResourceAccessDeniedException}
import io.deepsense.commons.auth.usercontext.{Role, TokenTranslator, UserContext}
import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.json.envelope.Envelope
import io.deepsense.commons.models.Id
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang.DOperationCategories
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.doperations.{FileToDataFrame, MathematicalOperation}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.graph.{Edge, Graph, Node}
import io.deepsense.model.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.model.json.workflow.WorkflowJsonProtocol
import io.deepsense.models.actions.{Action, AbortAction, LaunchAction}
import io.deepsense.models.workflows.Workflow.Status
import io.deepsense.models.workflows._
import io.deepsense.workflowmanager.conversion.FileConverter
import io.deepsense.workflowmanager.exceptions.WorkflowNotFoundException
import io.deepsense.workflowmanager.{WorkflowManager, WorkflowManagerProvider}

class WorkflowsApiSpec
  extends StandardSpec
  with UnitTestSupport
  with ApiSpecSupport
  with WorkflowJsonProtocol {

  val created = DateTimeConverter.now
  val updated = created.plusHours(1)
  val catalog = DOperationsCatalog()
  catalog.registerDOperation[FileToDataFrame](
    DOperationCategories.IO,
    "Converts a file to a DataFrame"
  )

  val dOperableCatalog = new DOperableCatalog
  override val inferContext: InferContext = new InferContext(dOperableCatalog, true)
  override val graphReader: GraphReader = new GraphReader(catalog)
  case class LaunchActionWrapper(launch: LaunchAction)
  case class AbortActionWrapper(abort: AbortAction)
  implicit val launchWrapperFormat = jsonFormat1(LaunchActionWrapper.apply)
  implicit val abortWrapperFormat = jsonFormat1(AbortActionWrapper.apply)

  /**
   * Returns an InputExperiment. Used for testing Experiment creation.
   */
  def inputWorkflow: InputWorkflow = InputWorkflow("test name", "test description", Graph())

  def envelopedInputWorkflow: Envelope[InputWorkflow] = Envelope(inputWorkflow)

  def cyclicWorkflow: InputWorkflow = {
    val node1 = Node(Node.Id.randomId, FileToDataFrame())
    val node2 = Node(Node.Id.randomId, FileToDataFrame())
    val graph = Graph(Set(node1, node2), Set(Edge(node1, 0, node2, 0), Edge(node2, 0, node1, 0)))
    InputWorkflow("cycle", "cyclic graph", graph)
  }

  val tenantAId: String = "A"
  val tenantBId: String = "B"

  /**
   * A valid Auth Token of a user of tenant A. This user has to have roles
   * for all actions in ExperimentManager
   */
  def validAuthTokenTenantA: String = tenantAId

  /**
   * A valid Auth Token of a user of tenant B. This user has to have no roles.
   */
  def validAuthTokenTenantB: String = tenantBId

  val workflowAId = Workflow.Id.randomId
  val workflowAWithNodeId = Workflow.Id.randomId
  val workflowA2Id = Workflow.Id.randomId
  val workflowBId = Workflow.Id.randomId

  protected def workflowOfTenantA = Workflow(
    workflowAId,
    tenantAId,
    "Workflow of Tenant A",
    Graph(),
    created,
    updated)

  val nodeUUID = Node.Id.randomId

  protected def workflowOfTenantAWithNode = Workflow(
    workflowAWithNodeId,
    tenantAId,
    "Workflow of Tenant A with node",
    Graph(Set(Node(nodeUUID, MathematicalOperation("2")))),
    created,
    updated)

  protected def workflowOfTenantA2 = Workflow(
    workflowA2Id,
    tenantAId,
    "Second workflow of Tenant A",
    Graph(),
    created,
    updated)

  protected def workflowOfTenantB = Workflow(
    workflowBId,
    tenantBId,
    "Workflow of Tenant B",
    Graph(),
    created,
    updated)

  val apiPrefix: String = "v1/experiments"

  override val authTokens: Map[String, Set[String]] = Map(
    tenantAId -> Set("experiments:get", "experiments:update", "experiments:delete",
      "experiments:launch", "experiments:abort", "experiments:create", "experiments:list"),
    tenantBId -> Set()
  )

  override def createRestComponent(tokenTranslator: TokenTranslator): Route = {
    val experimentManagerProvider = mock[WorkflowManagerProvider]
    val fileConverter = mock[FileConverter]
    when(experimentManagerProvider.forContext(any(classOf[Future[UserContext]])))
      .thenAnswer(new Answer[WorkflowManager]{
      override def answer(invocation: InvocationOnMock): WorkflowManager = {
        val futureContext = invocation.getArgumentAt(0, classOf[Future[UserContext]])
        new MockWorkflowManager(futureContext)
      }
    })

    new WorkflowApi(tokenTranslator, experimentManagerProvider, fileConverter,
      apiPrefix, graphReader, inferContext).route
  }

  // TODO Test errors in Json
  "GET /experiments" should {
    // TODO Test pagination + filtering
    "return a list of experiments" when {
      "valid auth token was send" in {
        Get(s"/$apiPrefix") ~>
        addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          responseAs[WorkflowsList]
        }
      }
    }
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Get(s"/$apiPrefix") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Get(s"/$apiPrefix") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }
  s"GET /experiments/:id" should {
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
    "return Not found" when {
      "asked for Experiment from other tenant" in {
        Get(s"/$apiPrefix/${workflowOfTenantB.id.toString}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "asked for non existing Experiment" in {
        Get(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return an experiment" when {
      "auth token is correct, user has roles and the experiment belongs to him" in {
        Get(s"/$apiPrefix/${workflowOfTenantA.id}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          val response = responseAs[Envelope[Workflow]].content
          response shouldBe workflowOfTenantA
        }
      }
    }
  }

  s"DELETE /experiments/:id" should {
    "return Not found" when {
      "experiment does not exists" in {
        Delete(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "tried to delete others' experiment" in {
        Delete(s"/$apiPrefix/${workflowOfTenantB.id}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Ok" when {
      "experiment existed and is deleted now" in {
        Delete(s"/$apiPrefix/${workflowOfTenantA.id}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
        }
      }
    }
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Delete(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Delete(s"/$apiPrefix/${Workflow.Id.randomId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Delete(s"/$apiPrefix/${Workflow.Id.randomId}") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  "POST /experiments" should {
    "process authorization before reading POST content" in {
      val invalidContent = JsObject()
      Post(s"/$apiPrefix", invalidContent) ~> testRoute ~> check {
        status should be(StatusCodes.Unauthorized)
      }
    }
    "return created" when {
      "inputExperiment was send" in {
        Post(s"/$apiPrefix", envelopedInputWorkflow) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be (StatusCodes.Created)
          val savedExperiment = responseAs[Envelope[Workflow]].content
          savedExperiment should have (
            'name (inputWorkflow.name),
            'description (inputWorkflow.description),
            'graph (inputWorkflow.graph),
            'tenantId (tenantAId)
          )
        }
      }
    }
    "return InternalServerError" when {
      "inputExperiment contains cyclic graph" in {
        Post(s"/$apiPrefix", Envelope(cyclicWorkflow)) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be (StatusCodes.InternalServerError)
        }
      }
    }
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Post(s"/$apiPrefix", envelopedInputWorkflow) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Post(s"/$apiPrefix", envelopedInputWorkflow) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Post(s"/$apiPrefix", envelopedInputWorkflow) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  s"POST /experiments/:id/action" should {
    "process authorization before reading POST content" in {
      val invalidContent = JsObject()
      Post(s"/$apiPrefix/${workflowOfTenantA.id}/action", invalidContent) ~> testRoute ~> check {
        status should be(StatusCodes.Unauthorized)
      }
    }
  }

  s"POST /experiments/:id/action (with LaunchAction)" should {
    "return Unauthorized" when {
      def launchAction: LaunchActionWrapper = LaunchActionWrapper(
        LaunchAction(
          Some(
            List(
              Node.Id(workflowOfTenantA.id.value)))))
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Post(s"/$apiPrefix/${workflowOfTenantA.id}/action", launchAction) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Post(s"/$apiPrefix/${workflowOfTenantA.id}/action", launchAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Post(s"/$apiPrefix/${workflowOfTenantA.id}/action", launchAction) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
    "return not found" when {
      "experiment does not exist" in {
        val randomId = Workflow.Id.randomId
        val launchAction = LaunchActionWrapper(LaunchAction(Some(List(Node.Id.randomId))))
        Post(s"/$apiPrefix/$randomId/action", launchAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "experiment belongs to other tenant" in {
        val launchAction = LaunchActionWrapper(LaunchAction(Some(List(Node.Id.randomId))))
        Post(s"/$apiPrefix/${workflowOfTenantB.id}/action", launchAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Accepted" when {
      "experiments belongs to the user" ignore {
        val launchAction = LaunchActionWrapper(LaunchAction(Some(List(Node.Id.randomId))))
        Post(s"/$apiPrefix/${workflowOfTenantA.id}/action", launchAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.Accepted)
          val response = responseAs[Envelope[Workflow]].content
          response shouldBe workflowOfTenantA
        }
      }
    }
  }

  "POST /experiments/:id/action (with AbortAction)" should {
    "return Unauthorized" when {
      val abortAction = AbortActionWrapper(AbortAction(Some(List(Node.Id.randomId))))
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Post(s"/$apiPrefix/${workflowOfTenantA.id}/action", abortAction) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Post(s"/$apiPrefix/${workflowOfTenantA.id}/action", abortAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Post(s"/$apiPrefix/${workflowOfTenantA.id}/action", abortAction) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
    "return not found" when {
      "experiment does not exist" in {
        val randomId = Workflow.Id.randomId
        val abortAction = AbortActionWrapper(AbortAction(Some(List(Node.Id.randomId))))
        Post(s"/$apiPrefix/$randomId/action", abortAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~>
          testRoute ~>
          check {
            status shouldBe StatusCodes.NotFound
        }
      }
      "experiment belongs to other tenant" in {
        val abortAction = AbortActionWrapper(AbortAction(Some(List(Node.Id.randomId))))
        Post(s"/$apiPrefix/${workflowOfTenantB.id}/action", abortAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Accepted" when {
      "experiments belongs to the user" in {
        val abortAction = AbortActionWrapper(AbortAction(Some(List(Node.Id.randomId))))
        Post(s"/$apiPrefix/${workflowOfTenantA.id}/action", abortAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.Accepted)
          val response = responseAs[Envelope[Workflow]].content
          response shouldBe workflowOfTenantA
        }
      }
    }
  }

  s"PUT /experiments/:id" should {
    val newExperiment = InputWorkflow("New Name", "New Desc", Graph())
    val envlopedNewExperiment = Envelope(newExperiment)

    "process authorization before reading PUT content" in {
      val invalidContent = JsObject()
      Put(s"/$apiPrefix/" + Workflow.Id.randomId, invalidContent) ~> testRoute ~> check {
        status should be(StatusCodes.Unauthorized)
      }
    }
    "update the experiment and return Ok" when {
      "user updates his experiment" in {
        Put(s"/$apiPrefix/${workflowOfTenantA.id}", envlopedNewExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          val response = responseAs[Envelope[Workflow]].content
          response should have(
            'id (workflowOfTenantA.id),
            'tenantId (workflowOfTenantA.tenantId),
            'description (newExperiment.description),
            'graph (newExperiment.graph),
            'name (newExperiment.name))
        }
      }
    }
    "return NotFound" when {
      "the experiment does not exist" in {
        val nonExistingId = Workflow.Id.randomId

        Put(s"/$apiPrefix/$nonExistingId", envlopedNewExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "the user has no right to that experiment" in {

        Put(s"/$apiPrefix/${workflowOfTenantB.id}", envlopedNewExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Put(s"/$apiPrefix/" + workflowOfTenantA.id, envlopedNewExperiment) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Put(s"/$apiPrefix/" + workflowOfTenantA.id, envlopedNewExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Put(s"/$apiPrefix/" + workflowOfTenantA.id, envlopedNewExperiment) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  val metadataParams = Map("nodeId" -> nodeUUID.toString, "portIndex" -> "0")
  val metadataParamsString = getParamString(metadataParams)

  s"GET /experiments/:id/metadata" should {
    "return Metadata" when {
      "auth token is correct, user has roles and the experiment belongs to him" in {
        Get(s"/$apiPrefix/${workflowOfTenantAWithNode.id}/metadata" + metadataParamsString) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)

          val expectedJson = JsObject(
            "metadata" -> JsArray(),
            "warnings" -> JsArray(),
            "errors" -> JsArray()
          )
          responseAs[JsObject] shouldBe expectedJson
        }
      }
    }
    "return NotFound" when {
      "the experiment does not exist" in {
        val nonExistingId = Workflow.Id.randomId
        Get(s"/$apiPrefix/${nonExistingId}/metadata" + metadataParamsString) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "the user has no right to that experiment" in {
        Get(s"/$apiPrefix/${workflowOfTenantB.id}/metadata" + metadataParamsString) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Get(s"/$apiPrefix/${workflowOfTenantA.id}/metadata" + metadataParamsString) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Get(s"/$apiPrefix/${workflowOfTenantA.id}/metadata" + metadataParamsString) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/${workflowOfTenantA.id}/metadata" +
            metadataParamsString) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  private def getParamString(keyValue: Map[String, Any]): String = {
    "?" + (keyValue.toList.foldLeft("")
      ((str: String, kv: (String, Any)) => str + kv._1 + "=" + kv._2 + "&"))
      .dropRight(1)
  }

  class MockWorkflowManager(userContext: Future[UserContext]) extends WorkflowManager {

    var storedExperiments = Seq(
      workflowOfTenantA, workflowOfTenantAWithNode, workflowOfTenantA2, workflowOfTenantB)

    var storedExperimentsWithoutNodes = Seq(
      workflowOfTenantA, workflowOfTenantA2, workflowOfTenantB)

    override def get(id: Id): Future[Option[Workflow]] = {
      val wantedRole = "experiments:get"
      userContext.flatMap(uc => {
        if (!uc.roles.contains(Role(wantedRole))) {
          throw new NoRoleException(uc, wantedRole)
        } else {
          val experiment = storedExperiments.find(_.id == id)
          Future(experiment match {
            case Some(exp) if exp.tenantId == uc.tenantId => Some(exp)
            case Some(exp) if exp.tenantId != uc.tenantId =>
              throw new ResourceAccessDeniedException(uc, exp)
            case None => None
          })
        }
      })
    }

    override def update(experimentId: Id, experiment: InputWorkflow): Future[Workflow] = {
      val wantedRole = "experiments:update"
      userContext.flatMap(uc => {
        if (!uc.roles.contains(Role(wantedRole))) {
          throw new NoRoleException(uc, wantedRole)
        } else {
          val oldExperiment = storedExperiments.find(_.id == experimentId)
          Future(oldExperiment match {
            case Some(oe) if oe.tenantId == uc.tenantId => Workflow(
              oe.id,
              oe.tenantId,
              experiment.name,
              experiment.graph,
              created,
              updated,
              experiment.description)
            case Some(oe) if oe.tenantId != uc.tenantId =>
              throw new ResourceAccessDeniedException(uc, oe)
            case None => throw new WorkflowNotFoundException(experimentId)
          })
        }
      })
    }

    override def delete(id: Id): Future[Boolean] = {
      val wantedRole = "experiments:delete"
      userContext.flatMap(uc => {
        if (!uc.roles.contains(Role(wantedRole))) {
          throw new NoRoleException(uc, wantedRole)
        } else {
          val experiment = storedExperiments.find(_.id == id)
          Future(experiment match {
            case Some(exp) if exp.tenantId == uc.tenantId =>
              storedExperiments = storedExperiments.filterNot(_.id == id)
              true
            case Some(exp) if exp.tenantId != uc.tenantId =>
              throw new ResourceAccessDeniedException(uc, exp)
            case None => false
          })
        }
      })
    }

    override def launch(id: Id, targetNodes: Seq[Node.Id]): Future[Workflow] = {
      val wantedRole = "experiments:launch"
      userContext.flatMap(uc => {
        if (!uc.roles.contains(Role(wantedRole))) {
          throw new NoRoleException(uc, wantedRole)
        } else {
          val experiment = storedExperiments.find(_.id == id)
          Future(experiment match {
            case Some(exp) if exp.tenantId == uc.tenantId => exp
            case Some(exp) if exp.tenantId != uc.tenantId =>
              throw new ResourceAccessDeniedException(uc, exp)
            case None => throw new WorkflowNotFoundException(id)
          })
        }
      })
    }

    override def abort(id: Id, nodes: Seq[Node.Id]): Future[Workflow] = {
      val wantedRole = "experiments:abort"
      userContext.flatMap(uc => {
        if (!uc.roles.contains(Role(wantedRole))) {
          throw new NoRoleException(uc, "experiments:abort")
        } else {
          val experiment = storedExperiments.find(_.id == id)
          Future(experiment match {
            case Some(exp) if exp.tenantId == uc.tenantId => exp
            case Some(exp) if exp.tenantId != uc.tenantId =>
              throw new ResourceAccessDeniedException(uc, exp)
            case None => throw new WorkflowNotFoundException(id)
          })
        }
      })
    }

    override def create(inputExperiment: InputWorkflow): Future[Workflow] = {
      val wantedRole = "experiments:create"
      userContext.flatMap(uc => {
        if (!uc.roles.contains(Role(wantedRole))) {
          throw new NoRoleException(uc, wantedRole)
        } else {
          val experiment = Workflow(
            Workflow.Id.randomId,
            uc.tenantId,
            inputExperiment.name,
            inputExperiment.graph,
            created,
            updated,
            inputExperiment.description)
          Future.successful(experiment)
        }
      })
    }

    override def experiments(
        limit: Option[Int],
        page: Option[Int],
        status: Option[Status.Value]): Future[WorkflowsList] = {
      val wantedRole = "experiments:list"
      userContext.flatMap(uc => {
        if (!uc.roles.contains(Role(wantedRole))) {
          throw new NoRoleException(uc, wantedRole)
        } else {
          val filteredExperiments = storedExperimentsWithoutNodes.filter(_.tenantId == uc.tenantId)
          Future.successful(
            WorkflowsList(
              Count(filteredExperiments.size, filteredExperiments.size),
              filteredExperiments))
        }
      })
    }

    override def runAction(id: Id, action: Action): Future[Workflow] = action match {
      case AbortAction(nodes) => abort(id, nodes.getOrElse(List()))
      case LaunchAction(nodes) => launch(id, nodes.getOrElse(List()))
    }
  }
}
