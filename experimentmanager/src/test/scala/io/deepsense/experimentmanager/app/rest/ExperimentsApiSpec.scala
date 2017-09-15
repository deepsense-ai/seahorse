/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.rest

import java.util.UUID

import scala.concurrent._

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import spray.http.StatusCodes
import spray.routing.Route

import io.deepsense.deeplang.InferContext
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.experimentmanager.app.exceptions.ExperimentNotFoundException
import io.deepsense.experimentmanager.app.models.Experiment.Status
import io.deepsense.experimentmanager.app.models.{Experiment, Id, InputExperiment}
import io.deepsense.experimentmanager.app.rest.actions.{AbortAction, LaunchAction}
import io.deepsense.experimentmanager.app.rest.json.{ExperimentJsonProtocol}
import io.deepsense.experimentmanager.app.{ExperimentManager, ExperimentManagerProvider}
import io.deepsense.experimentmanager.auth.exceptions.{NoRoleException, ResourceAccessDeniedException}
import io.deepsense.experimentmanager.auth.usercontext.{Role, CannotGetUserException, TokenTranslator, UserContext}
import io.deepsense.experimentmanager.{StandardSpec, UnitTestSupport}
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphjson.GraphJsonProtocol.GraphReader

class ExperimentsApiSpec
  extends StandardSpec
  with UnitTestSupport
  with ApiSpecSupport
  with ExperimentJsonProtocol {

  val catalog = mock[DOperationsCatalog]
  val dOperableCatalog = mock[DOperableCatalog]
  override val inferContext: InferContext = mock[InferContext]
  when(inferContext.dOperableCatalog).thenReturn(dOperableCatalog)
  override val graphReader: GraphReader = new GraphReader(catalog)
  case class LaunchActionWrapper(launch: LaunchAction)
  case class AbortActionWrapper(abort: AbortAction)
  implicit val launchWrapperFormat = jsonFormat1(LaunchActionWrapper.apply)
  implicit val abortWrapperFormat = jsonFormat1(AbortActionWrapper.apply)

  /**
   * Returns an InputExperiment. Used for testing Experiment creation.
   */
  def inputExperiment: InputExperiment = InputExperiment("test name", "test description", Graph())

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

  val experimentAId = UUID.randomUUID()
  val experimentBId = UUID.randomUUID()

  def experimentOfTenantA = Experiment(
    experimentAId,
    tenantAId,
    "Experiment of Tenant A",
    Graph())

  def experimentOfTenantB = Experiment(
    experimentBId,
    tenantBId,
    "Experiment of Tenant B",
    Graph())

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
          responseAs[List[Experiment]]
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
        Get(s"/$apiPrefix/${UUID.randomUUID()}") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Get(s"/$apiPrefix/${UUID.randomUUID()}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Get(s"/$apiPrefix/${UUID.randomUUID()}") ~> testRoute ~> check {
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
        Get(s"/$apiPrefix/${UUID.randomUUID()}") ~>
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
          val response = responseAs[Experiment]
          response shouldBe experimentOfTenantA
        }
      }
    }
  }

  s"DELETE /experiments/:id" should {
    "return Not found" when {
      "experiment does not exists" in {
        Delete(s"/$apiPrefix/${UUID.randomUUID()}") ~>
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
        Delete(s"/$apiPrefix/${UUID.randomUUID()}") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Delete(s"/$apiPrefix/${UUID.randomUUID()}") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Delete(s"/$apiPrefix/${UUID.randomUUID()}") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  "POST /experiments" should {
    "return created" when {
      "inputExperiment was send" in {
        Post(s"/$apiPrefix", inputExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be (StatusCodes.Created)
          val savedExperiment = responseAs[Experiment]
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
        Post(s"/$apiPrefix", inputExperiment) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Post(s"/$apiPrefix", inputExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Post(s"/$apiPrefix", inputExperiment) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  s"POST /experiments/:id/action (with LaunchAction)" should {
    "return Unauthorized" when {
      def launchAction = LaunchActionWrapper(
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
        val randomId = Id(UUID.randomUUID())
        val launchAction = LaunchActionWrapper(LaunchAction(Some(List(UUID.randomUUID()))))
        Post(s"/$apiPrefix/$randomId/action", launchAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "experiment belongs to other tenant" in {
        val launchAction = LaunchActionWrapper(LaunchAction(Some(List(UUID.randomUUID()))))
        Post(s"/$apiPrefix/${experimentOfTenantB.id}/action", launchAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Accepted" when {
      "experiments belongs to the user" in {
        val launchAction = LaunchActionWrapper(LaunchAction(Some(List(UUID.randomUUID()))))
        Post(s"/$apiPrefix/${experimentOfTenantA.id}/action", launchAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.Accepted)
          val response = responseAs[Experiment]
          response shouldBe experimentOfTenantA
        }
      }
    }
  }

  "POST /experiments/:id/action (with AbortAction)" should {
    "return Unauthorized" when {
      val abortAction = AbortActionWrapper(AbortAction(Some(List(UUID.randomUUID()))))
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
        val randomId = Id(UUID.randomUUID())
        val abortAction = AbortActionWrapper(AbortAction(Some(List(UUID.randomUUID()))))
        Post(s"/$apiPrefix/$randomId/action", abortAction) ~> addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "experiment belongs to other tenant" in {
        val abortAction = AbortActionWrapper(AbortAction(Some(List(UUID.randomUUID()))))
        Post(s"/$apiPrefix/${experimentOfTenantB.id}/action", abortAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Accepted" when {
      "experiments belongs to the user" in {
        val abortAction = AbortActionWrapper(AbortAction(Some(List(UUID.randomUUID()))))
        Post(s"/$apiPrefix/${experimentOfTenantA.id}/action", abortAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.Accepted)
          val response = responseAs[Experiment]
          response shouldBe experimentOfTenantA
        }
      }
    }
  }

  s"PUT /experiments/:id" should {
    "update the experiment and return Ok" when {
      "user updates his experiment" in {
        val newExperiment = Experiment(
          experimentOfTenantA.id,
          tenantAId,
          "New Name",
          Graph(),
          "New Desc")

        Put(s"/$apiPrefix/${experimentOfTenantA.id}", newExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          val response = responseAs[Experiment]
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
        val newExperiment = Experiment(
          UUID.randomUUID(),
          tenantAId,
          "New Name",
          Graph(),
          "New Desc")

        Put(s"/$apiPrefix/${newExperiment.id}", newExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "the user has no right to that experiment" in {

        val newExperiment = Experiment(
          experimentOfTenantB.id,
          tenantBId,
          "New Name",
          Graph(),
          "New Desc")

        Put(s"/$apiPrefix/${experimentOfTenantB.id}", newExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Unauthorized" when {
      val newExperiment = Experiment(UUID.randomUUID(), "asd", "New Name", Graph(), "New Desc")
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Put(s"/$apiPrefix/" + newExperiment.id, newExperiment) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user does not have the requested role (on NoRoleExeption)" in {
        Put(s"/$apiPrefix/" + newExperiment.id, newExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Put(s"/$apiPrefix/" + newExperiment.id, newExperiment) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
    "return BadRequest" when {
      val newExperiment = Experiment(UUID.randomUUID(), "asd", "New Name", Graph(), "New Desc")
      "Experiment's Id from Json does not match Id from request's URL" in {
        Put(s"/$apiPrefix/" + newExperiment.id, newExperiment) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  class MockExperimentManager(userContext: Future[UserContext]) extends ExperimentManager {

    var storedExperiments: List[Experiment] = List(experimentOfTenantA, experimentOfTenantB)

    override def get(id: Experiment.Id): Future[Option[Experiment]] = {
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

    override def update(experiment: Experiment): Future[Experiment] = {
      val wantedRole = "experiments:update"
      userContext.flatMap(uc => {
        if (!uc.roles.contains(Role(wantedRole))) {
          throw new NoRoleException(uc, wantedRole)
        } else {
          val oldExperiment = storedExperiments.find(_.id == experiment.id)
          Future(oldExperiment match {
            case Some(oe) if oe.tenantId == uc.tenantId => Experiment(
              oe.id,
              oe.tenantId,
              experiment.name,
              experiment.graph,
              experiment.description)
            case Some(oe) if oe.tenantId != uc.tenantId =>
              throw new ResourceAccessDeniedException(uc, oe)
            case None => throw new ExperimentNotFoundException(experiment.id)
          })
        }
      })
    }

    override def delete(id: Experiment.Id): Future[Boolean] = {
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

    override def launch(id: Experiment.Id, targetNodes: List[Node.Id]): Future[Experiment] = {
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

    override def abort(id: Experiment.Id, nodes: List[Node.Id]): Future[Experiment] = {
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
            UUID.randomUUID(),
            uc.tenantId,
            inputExperiment.name,
            inputExperiment.graph,
            inputExperiment.description)
          Future.successful(experiment)
        }
      })
    }

    override def experiments(
        limit: Option[Int],
        page: Option[Int],
        status: Option[Status.Value]): Future[Seq[Experiment]] = {
      val wantedRole = "experiments:list"
      userContext.flatMap(uc => {
        if (!uc.roles.contains(Role(wantedRole))) {
          throw new NoRoleException(uc, wantedRole)
        } else {
          Future.successful(storedExperiments.filter(_.tenantId == uc.tenantId).toSeq)
        }
      })
    }
  }
}
