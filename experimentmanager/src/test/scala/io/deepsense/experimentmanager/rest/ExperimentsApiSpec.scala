/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest

import io.deepsense.deeplang.doperations.MathematicalOperation

import scala.concurrent._

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import spray.http.{HttpCharsets, HttpCharset, StatusCodes}
import spray.json._
import spray.routing.Route

import io.deepsense.commons.auth.exceptions.{NoRoleException, ResourceAccessDeniedException}
import io.deepsense.commons.auth.usercontext.{Role, TokenTranslator, UserContext}
import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.json.envelope.Envelope
import io.deepsense.commons.models.Id
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang.{ExecutionContext, DOperation0To1, InferContext}
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.experimentmanager.exceptions.ExperimentNotFoundException
import io.deepsense.experimentmanager.models.{Count, ExperimentsList}
import io.deepsense.experimentmanager.rest.actions.{AbortAction, LaunchAction}
import io.deepsense.experimentmanager.rest.json.{ExperimentJsonProtocol, DataFrameMetadataJsonProtocol}
import io.deepsense.experimentmanager.{ExperimentManager, ExperimentManagerProvider}
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphjson.GraphJsonProtocol.GraphReader
import io.deepsense.models.experiments.Experiment.Status
import io.deepsense.models.experiments.{Experiment, InputExperiment}

class ExperimentsApiSpec
  extends StandardSpec
  with UnitTestSupport
  with ApiSpecSupport
  with ExperimentJsonProtocol {

  val created = DateTimeConverter.now
  val updated = created.plusHours(1)
  val catalog = DOperationsCatalog()
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
  def inputExperiment: InputExperiment = InputExperiment("test name", "test description", Graph())

  def envelopedInputExperiment: Envelope[InputExperiment] = Envelope(inputExperiment)

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

  val experimentAId = Experiment.Id.randomId
  val experimentAWithNodeId = Experiment.Id.randomId
  val experimentA2Id = Experiment.Id.randomId
  val experimentBId = Experiment.Id.randomId

  protected def experimentOfTenantA = Experiment(
    experimentAId,
    tenantAId,
    "Experiment of Tenant A",
    Graph(),
    created,
    updated)

  val nodeUUID = Node.Id.randomId

  protected def experimentOfTenantAWithNode = Experiment(
    experimentAWithNodeId,
    tenantAId,
    "Experiment of Tenant A with node",
    Graph(Set(Node(nodeUUID, MathematicalOperation("2")))),
    created,
    updated)

  protected def experimentOfTenantA2 = Experiment(
    experimentA2Id,
    tenantAId,
    "Second experiment of Tenant A",
    Graph(),
    created,
    updated)

  protected def experimentOfTenantB = Experiment(
    experimentBId,
    tenantBId,
    "Experiment of Tenant B",
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
    val experimentManagerProvider = mock[ExperimentManagerProvider]
    when(experimentManagerProvider.forContext(any(classOf[Future[UserContext]])))
      .thenAnswer(new Answer[ExperimentManager]{
      override def answer(invocation: InvocationOnMock): ExperimentManager = {
        val futureContext = invocation.getArgumentAt(0, classOf[Future[UserContext]])
        new MockExperimentManager(futureContext)
      }
    })

    new ExperimentsApi(
      tokenTranslator, experimentManagerProvider, apiPrefix, graphReader, inferContext).route
  }

  // TODO Test errors in Json
  "GET /experiments" should {
    // TODO Test pagination + filtering
    "return a list of experiments" when {
      "valid auth token was send" in {
        Get(s"/$apiPrefix") ~>
        addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          responseAs[ExperimentsList]
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
        Get(s"/$apiPrefix/${Experiment.Id.randomId}") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Get(s"/$apiPrefix/${Experiment.Id.randomId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/${Experiment.Id.randomId}") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
    "return Not found" when {
      "asked for Experiment from other tenant" in {
        Get(s"/$apiPrefix/${experimentOfTenantB.id.toString}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "asked for non existing Experiment" in {
        Get(s"/$apiPrefix/${Experiment.Id.randomId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return an experiment" when {
      "auth token is correct, user has roles and the experiment belongs to him" in {
        Get(s"/$apiPrefix/${experimentOfTenantA.id}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          val response = responseAs[Envelope[Experiment]].content
          response shouldBe experimentOfTenantA
        }
      }
    }
  }

  s"DELETE /experiments/:id" should {
    "return Not found" when {
      "experiment does not exists" in {
        Delete(s"/$apiPrefix/${Experiment.Id.randomId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "tried to delete others' experiment" in {
        Delete(s"/$apiPrefix/${experimentOfTenantB.id}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Ok" when {
      "experiment existed and is deleted now" in {
        Delete(s"/$apiPrefix/${experimentOfTenantA.id}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
        }
      }
    }
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Delete(s"/$apiPrefix/${Experiment.Id.randomId}") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Delete(s"/$apiPrefix/${Experiment.Id.randomId}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Delete(s"/$apiPrefix/${Experiment.Id.randomId}") ~> testRoute ~> check {
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
        Post(s"/$apiPrefix", envelopedInputExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be (StatusCodes.Created)
          val savedExperiment = responseAs[Envelope[Experiment]].content
          savedExperiment should have (
            'name (inputExperiment.name),
            'description (inputExperiment.description),
            'graph (inputExperiment.graph),
            'tenantId (tenantAId)
          )
        }
      }
    }
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Post(s"/$apiPrefix", envelopedInputExperiment) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Post(s"/$apiPrefix", envelopedInputExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Post(s"/$apiPrefix", envelopedInputExperiment) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  s"POST /experiments/:id/action" should {
    "process authorization before reading POST content" in {
      val invalidContent = JsObject()
      Post(s"/$apiPrefix/${experimentOfTenantA.id}/action", invalidContent) ~> testRoute ~> check {
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
              Node.Id(experimentOfTenantA.id.value)))))
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Post(s"/$apiPrefix/${experimentOfTenantA.id}/action", launchAction) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Post(s"/$apiPrefix/${experimentOfTenantA.id}/action", launchAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Post(s"/$apiPrefix/${experimentOfTenantA.id}/action", launchAction) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
    "return not found" when {
      "experiment does not exist" in {
        val randomId = Experiment.Id.randomId
        val launchAction = LaunchActionWrapper(LaunchAction(Some(List(Node.Id.randomId))))
        Post(s"/$apiPrefix/$randomId/action", launchAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "experiment belongs to other tenant" in {
        val launchAction = LaunchActionWrapper(LaunchAction(Some(List(Node.Id.randomId))))
        Post(s"/$apiPrefix/${experimentOfTenantB.id}/action", launchAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Accepted" when {
      "experiments belongs to the user" ignore {
        val launchAction = LaunchActionWrapper(LaunchAction(Some(List(Node.Id.randomId))))
        Post(s"/$apiPrefix/${experimentOfTenantA.id}/action", launchAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.Accepted)
          val response = responseAs[Envelope[Experiment]].content
          response shouldBe experimentOfTenantA
        }
      }
    }
  }

  "POST /experiments/:id/action (with AbortAction)" should {
    "return Unauthorized" when {
      val abortAction = AbortActionWrapper(AbortAction(Some(List(Node.Id.randomId))))
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Post(s"/$apiPrefix/${experimentOfTenantA.id}/action", abortAction) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Post(s"/$apiPrefix/${experimentOfTenantA.id}/action", abortAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Post(s"/$apiPrefix/${experimentOfTenantA.id}/action", abortAction) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
    "return not found" when {
      "experiment does not exist" in {
        val randomId = Experiment.Id.randomId
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
        Post(s"/$apiPrefix/${experimentOfTenantB.id}/action", abortAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Accepted" when {
      "experiments belongs to the user" in {
        val abortAction = AbortActionWrapper(AbortAction(Some(List(Node.Id.randomId))))
        Post(s"/$apiPrefix/${experimentOfTenantA.id}/action", abortAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.Accepted)
          val response = responseAs[Envelope[Experiment]].content
          response shouldBe experimentOfTenantA
        }
      }
    }
  }

  s"PUT /experiments/:id" should {
    val newExperiment = InputExperiment("New Name", "New Desc", Graph())
    val envlopedNewExperiment = Envelope(newExperiment)

    "process authorization before reading PUT content" in {
      val invalidContent = JsObject()
      Put(s"/$apiPrefix/" + Experiment.Id.randomId, invalidContent) ~> testRoute ~> check {
        status should be(StatusCodes.Unauthorized)
      }
    }
    "update the experiment and return Ok" when {
      "user updates his experiment" in {
        Put(s"/$apiPrefix/${experimentOfTenantA.id}", envlopedNewExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          val response = responseAs[Envelope[Experiment]].content
          response should have(
            'id (experimentOfTenantA.id),
            'tenantId (experimentOfTenantA.tenantId),
            'description (newExperiment.description),
            'graph (newExperiment.graph),
            'name (newExperiment.name))
        }
      }
    }
    "return NotFound" when {
      "the experiment does not exist" in {
        val nonExistingId = Experiment.Id.randomId

        Put(s"/$apiPrefix/$nonExistingId", envlopedNewExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "the user has no right to that experiment" in {

        Put(s"/$apiPrefix/${experimentOfTenantB.id}", envlopedNewExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Put(s"/$apiPrefix/" + experimentOfTenantA.id, envlopedNewExperiment) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Put(s"/$apiPrefix/" + experimentOfTenantA.id, envlopedNewExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Put(s"/$apiPrefix/" + experimentOfTenantA.id, envlopedNewExperiment) ~> testRoute ~> check {
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
        Get(s"/$apiPrefix/${experimentOfTenantAWithNode.id}/metadata" + metadataParamsString) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)

          val expectedJson = JsObject(
            "metadata" -> JsArray()
          )
          response.entity.asString(HttpCharsets.`UTF-8`).parseJson shouldBe expectedJson
        }
      }
    }
    "return NotFound" when {
      "the experiment does not exist" in {
        val nonExistingId = Experiment.Id.randomId
        Get(s"/$apiPrefix/${nonExistingId}/metadata" + metadataParamsString) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "the user has no right to that experiment" in {
        Get(s"/$apiPrefix/${experimentOfTenantB.id}/metadata" + metadataParamsString) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Get(s"/$apiPrefix/${experimentOfTenantA.id}/metadata" + metadataParamsString) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Get(s"/$apiPrefix/${experimentOfTenantA.id}/metadata" + metadataParamsString) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/${experimentOfTenantA.id}/metadata" + metadataParamsString) ~> testRoute ~> check {
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

  class MockExperimentManager(userContext: Future[UserContext]) extends ExperimentManager {

    var storedExperiments = Seq(
      experimentOfTenantA, experimentOfTenantAWithNode, experimentOfTenantA2, experimentOfTenantB)

    var storedExperimentsWithoutNodes = Seq(
      experimentOfTenantA, experimentOfTenantA2, experimentOfTenantB)

    override def get(id: Id): Future[Option[Experiment]] = {
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

    override def update(experimentId: Id, experiment: InputExperiment): Future[Experiment] = {
      val wantedRole = "experiments:update"
      userContext.flatMap(uc => {
        if (!uc.roles.contains(Role(wantedRole))) {
          throw new NoRoleException(uc, wantedRole)
        } else {
          val oldExperiment = storedExperiments.find(_.id == experimentId)
          Future(oldExperiment match {
            case Some(oe) if oe.tenantId == uc.tenantId => Experiment(
              oe.id,
              oe.tenantId,
              experiment.name,
              experiment.graph,
              created,
              updated,
              experiment.description)
            case Some(oe) if oe.tenantId != uc.tenantId =>
              throw new ResourceAccessDeniedException(uc, oe)
            case None => throw new ExperimentNotFoundException(experimentId)
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

    override def launch(id: Id, targetNodes: Seq[Node.Id]): Future[Experiment] = {
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
            case None => throw new ExperimentNotFoundException(id)
          })
        }
      })
    }

    override def abort(id: Id, nodes: Seq[Node.Id]): Future[Experiment] = {
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
            case None => throw new ExperimentNotFoundException(id)
          })
        }
      })
    }

    override def create(inputExperiment: InputExperiment): Future[Experiment] = {
      val wantedRole = "experiments:create"
      userContext.flatMap(uc => {
        if (!uc.roles.contains(Role(wantedRole))) {
          throw new NoRoleException(uc, wantedRole)
        } else {
          val experiment = Experiment(
            Experiment.Id.randomId,
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
        status: Option[Status.Value]): Future[ExperimentsList] = {
      val wantedRole = "experiments:list"
      userContext.flatMap(uc => {
        if (!uc.roles.contains(Role(wantedRole))) {
          throw new NoRoleException(uc, wantedRole)
        } else {
          val filteredExperiments = storedExperimentsWithoutNodes.filter(_.tenantId == uc.tenantId)
          Future.successful(
            ExperimentsList(
              Count(filteredExperiments.size, filteredExperiments.size),
              filteredExperiments))
        }
      })
    }
  }
}
