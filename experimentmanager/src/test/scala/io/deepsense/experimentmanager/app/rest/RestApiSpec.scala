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
import spray.http.{HttpRequest, StatusCodes}

import io.deepsense.experimentmanager.app.models.Experiment.Status
import io.deepsense.experimentmanager.app.models.Graph.Node
import io.deepsense.experimentmanager.app.{ExperimentManagerProvider, ExperimentManager}
import io.deepsense.experimentmanager.app.exceptions.ExperimentNotFoundException
import io.deepsense.experimentmanager.app.models.{Experiment, Graph, Id, InputExperiment}
import io.deepsense.experimentmanager.app.rest.actions.{AbortAction, LaunchAction}
import io.deepsense.experimentmanager.app.rest.json.RestJsonProtocol._
import io.deepsense.experimentmanager.auth.exceptions.{ResourceAccessDeniedException, NoRoleException}
import io.deepsense.experimentmanager.auth.usercontext.{Role, CannotGetUserException, TokenTranslator, UserContext}
import io.deepsense.experimentmanager.{StandardSpec, UnitTestSupport}

class RestApiSpec extends StandardSpec with UnitTestSupport {

  val inputExperiment = InputExperiment("test name", "test description", Graph())

  case class LaunchActionWrapper(launch: LaunchAction)
  case class AbortActionWrapper(abort: AbortAction)
  implicit val launchWrapperFormat = jsonFormat1(LaunchActionWrapper.apply)
  implicit val abortWrapperFormat = jsonFormat1(AbortActionWrapper.apply)

  val validAuthTokenTenantA = "A" // TODO: def -> with all roles
  val validAuthTokenTenantB = "B" // TODO: def -> with no roles
  val rolesForTenantA = Set(
      new Role("experiments:get"),
      new Role("experiments:list"),
      new Role("experiments:delete"),
      new Role("experiments:update"),
      new Role("experiments:create"),
      new Role("experiments:launch"),
      new Role("experiments:about"))

  val experimentOfTenantA = Experiment(
    UUID.randomUUID(),
    "A",
    "Experiment of Tenant A")

  val experimentOfTenantB = Experiment(
    UUID.randomUUID(),
    "B",
    "Experiment of Tenant B")

  protected def testRoute = {
    val tokenTranslator = mock[TokenTranslator]
    when(tokenTranslator.translate(any(classOf[String])))
      .thenAnswer(new Answer[Future[UserContext]]{
      override def answer(invocation: InvocationOnMock): Future[UserContext] = {
        val tokenFromRequest = invocation.getArgumentAt(0, classOf[String])
        if (tokenFromRequest == validAuthTokenTenantA
          || tokenFromRequest == validAuthTokenTenantB ) {
          val uc = mock[UserContext]
          when(uc.tenantId).thenReturn(tokenFromRequest)
          when(uc.roles).thenReturn(rolesForTenantA)
          Future.successful(uc)
        } else {
          Future.failed(new CannotGetUserException(tokenFromRequest))
        }
      }
    })

    val experimentManagerProvider = mock[ExperimentManagerProvider]
    when(experimentManagerProvider.forContext(any(classOf[Future[UserContext]])))
      .thenAnswer(new Answer[ExperimentManager]{
      override def answer(invocation: InvocationOnMock): ExperimentManager = {
        val futureContext = invocation.getArgumentAt(0, classOf[Future[UserContext]])
        new MockExperimentManager(futureContext)
      }
    })

    new RestApi(tokenTranslator, experimentManagerProvider).route
  }

  // TODO Test errors in Json

  "GET /experiments" should {
    // TODO Test pagination + filtering
    "return a list of experiments" when {
      "valid auth token was send" in {
        Get("/experiments") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          responseAs[List[Experiment]]
        }
      }
    }
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Get("/experiments") ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user has not the requested role (on NoRoleExeption)" in {
        Get("/experiments") ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Get("/experiments") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }
  "GET /experiments/:id" should {
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Get("/experiments/" + UUID.randomUUID()) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user has not the requested role (on NoRoleExeption)" in {
        Get("/experiments/" + UUID.randomUUID()) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Get("/experiments/" + UUID.randomUUID()) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
    "return Not found" when {
      "asked for Experiment from other tenant" in {
        Get("/experiments/" + experimentOfTenantB.id.toString) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "asked for non existing Experiment" in {
        Get("/experiments/" + UUID.randomUUID()) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return an experiment" when {
      "auth token is correct, user has roles and the experiment belongs to him" in {
        Get("/experiments/" + experimentOfTenantA.id.toString) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          val response = responseAs[Experiment]
          response shouldBe experimentOfTenantA
        }
      }
    }
  }

  "DELETE /experiments/:id" should {
    "return Not found" when {
      "experiment does not exists" in {
        Delete("/experiments/" + UUID.randomUUID()) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "tried to delete others' experiment" in {
        Delete("/experiments/" + experimentOfTenantB.id) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Ok" when {
      "experiment existed and is deleted now" in {
        Delete("/experiments/" + experimentOfTenantA.id) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.OK)
        }
      }
    }
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Delete("/experiments/" + UUID.randomUUID()) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user has not the requested role (on NoRoleExeption)" in {
        Delete("/experiments/" + UUID.randomUUID()) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Delete("/experiments/" + UUID.randomUUID()) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  "POST /experiments" should {
    "return created" when {
      "inputExperiment was send" in {
        Post("/experiments", inputExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be (StatusCodes.Created)
          val savedExperiment = responseAs[Experiment]
          savedExperiment should have (
            'name (inputExperiment.name),
            'description (inputExperiment.description),
            'graph (inputExperiment.graph),
            'tenantId (validAuthTokenTenantA)
          )
        }
      }
    }
    "return Unauthorized" when {
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Post("/experiments", inputExperiment) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user has not the requested role (on NoRoleExeption)" in {
        Post("/experiments", inputExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Post("/experiments", inputExperiment) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  "POST /experiments/:id/action (with LaunchAction)" should {
    "return Unauthorized" when {
      val launchAction = LaunchActionWrapper(LaunchAction(Some(List(experimentOfTenantA.id))))
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Post(s"/experiments/${experimentOfTenantA.id}/action", launchAction) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user has not the requested role (on NoRoleExeption)" in {
        Post(s"/experiments/${experimentOfTenantA.id}/action", launchAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Post(s"/experiments/${experimentOfTenantA.id}/action", launchAction) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
    "return not found" when {
      "experiment does not exist" in {
        val randomId = Id(UUID.randomUUID())
        val launchAction = LaunchActionWrapper(LaunchAction(Some(List(UUID.randomUUID()))))
        Post(s"/experiments/$randomId/action", launchAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "experiment belongs to other tenant" in {
        val launchAction = LaunchActionWrapper(LaunchAction(Some(List(UUID.randomUUID()))))
        Post(s"/experiments/${experimentOfTenantB.id}/action", launchAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Accepted" when {
      "experiments belongs to the user" in {
        val launchAction = LaunchActionWrapper(LaunchAction(Some(List(UUID.randomUUID()))))
        Post(s"/experiments/${experimentOfTenantA.id}/action", launchAction) ~>
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
        Post(s"/experiments/${experimentOfTenantA.id}/action", abortAction) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user has not the requested role (on NoRoleExeption)" in {
        Post(s"/experiments/${experimentOfTenantA.id}/action", abortAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Post(s"/experiments/${experimentOfTenantA.id}/action", abortAction) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
    "return not found" when {
      "experiment does not exist" in {
        val randomId = Id(UUID.randomUUID())
        val abortAction = AbortActionWrapper(AbortAction(Some(List(UUID.randomUUID()))))
        Post(s"/experiments/$randomId/action", abortAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "experiment belongs to other tenant" in {
        val abortAction = AbortActionWrapper(AbortAction(Some(List(UUID.randomUUID()))))
        Post(s"/experiments/${experimentOfTenantB.id}/action", abortAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Accepted" when {
      "experiments belongs to the user" in {
        val abortAction = AbortActionWrapper(AbortAction(Some(List(UUID.randomUUID()))))
        Post(s"/experiments/${experimentOfTenantA.id}/action", abortAction) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.Accepted)
          val response = responseAs[Experiment]
          response shouldBe experimentOfTenantA
        }
      }
    }
  }
  "PUT /experiments/:id" should {
    "update the experiment and return Ok" when {
      "user updates his experiment" in {
        val newExperiment = Experiment(experimentOfTenantA.id, "asd", "New Name", "New Desc", Graph())
        Put(s"/experiments/${experimentOfTenantA.id}", newExperiment) ~>
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
        val newExperiment = Experiment(UUID.randomUUID(), "asd", "New Name", "New Desc", Graph())
        Put(s"/experiments/${newExperiment.id}", newExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "the user has no right to that experiment" in {
        val newExperiment = Experiment(experimentOfTenantB.id, "asd", "New Name", "New Desc", Graph())
        Put(s"/experiments/${experimentOfTenantB.id}", newExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantA) ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
    "return Unauthorized" when {
      val newExperiment = Experiment(UUID.randomUUID(), "asd", "New Name", "New Desc", Graph())
      "invalid auth token was send (when InvalidTokenException occures)" in {
        Put("/experiments/" + newExperiment.id, newExperiment) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "the user has not the requested role (on NoRoleExeption)" in {
        Put("/experiments/" + newExperiment.id, newExperiment) ~>
          addHeader("X-Auth-Token", validAuthTokenTenantB) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
      "no auth token was send (on MissingHeaderRejection)" in {
        Put("/experiments/" + newExperiment.id, newExperiment) ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
    "return BadRequest" when {
      val newExperiment = Experiment(UUID.randomUUID(), "asd", "New Name", "New Desc", Graph())
      "Experiment's Id from Json does not match Id from request's URL" in {
        Put("/experiments/" + newExperiment.id, newExperiment) ~>
          addHeader("X-Auth-Token", "its-invalid!") ~> testRoute ~> check {
          status should be(StatusCodes.Unauthorized)
        }
      }
    }
  }

  class MockExperimentManager(userContext: Future[UserContext]) extends ExperimentManager {

    var storedExperiments: List[Experiment] = List(experimentOfTenantA, experimentOfTenantB)

    override def get(id: Experiment.Id): Future[Option[Experiment]] = {
      userContext.flatMap(uc => {
        if (uc.tenantId == validAuthTokenTenantB) throw new NoRoleException(uc, "experiments:list")
        if (uc.tenantId == validAuthTokenTenantA) {
          val experiment = storedExperiments.find(_.id == id)
          Future(experiment match {
            case Some(exp) if exp.tenantId == uc.tenantId => Some(exp)
            case Some(exp) if exp.tenantId != uc.tenantId =>
              throw new ResourceAccessDeniedException(uc, exp)
            case None => None
          })
        } else {
          Future.failed(new IllegalStateException("This should never happen in this mock"))
        }
      })
    }

    override def update(experiment: Experiment): Future[Experiment] = {
      userContext.flatMap(uc => {
        if (uc.tenantId == validAuthTokenTenantB) throw new NoRoleException(uc, "experiments:list")
        if (uc.tenantId == validAuthTokenTenantA) {
          val oldExperiment = storedExperiments.find(_.id == experiment.id)
          Future(oldExperiment match {
            case Some(oe) if oe.tenantId == uc.tenantId => Experiment(
              oe.id,
              oe.tenantId,
              experiment.name,
              experiment. description,
              experiment.graph)
            case Some(oe) if oe.tenantId != uc.tenantId =>
              throw new ResourceAccessDeniedException(uc, oe)
            case None => throw new ExperimentNotFoundException(experiment.id)
          })
        } else {
          Future.failed(new IllegalStateException("This should never happen in this mock"))
        }
      })
    }

    override def delete(id: Experiment.Id): Future[Boolean] = {
      userContext.flatMap(uc => {
        if (uc.tenantId == validAuthTokenTenantB) throw new NoRoleException(uc, "experiments:list")
        if (uc.tenantId == validAuthTokenTenantA) {
          val experiment = storedExperiments.find(_.id == id)
          Future(experiment match {
            case Some(exp) if exp.tenantId == uc.tenantId =>
              storedExperiments = storedExperiments.filterNot(_.id == id)
              true
            case Some(exp) if exp.tenantId != uc.tenantId =>
              throw new ResourceAccessDeniedException(uc, exp)
            case None => false
          })
        } else {
          Future.failed(new IllegalStateException("This should never happen in this mock"))
        }
      })
    }

    override def launch(id: Experiment.Id, targetNodes: List[Node.Id]): Future[Experiment] = {
      userContext.flatMap(uc => {
        if (uc.tenantId == validAuthTokenTenantB) {
          throw new NoRoleException(uc, "experiments:launch")
        }
        if (uc.tenantId == validAuthTokenTenantA) {
          val experiment = storedExperiments.find(_.id == id)
          Future(experiment match {
            case Some(exp) if exp.tenantId == uc.tenantId => exp
            case Some(exp) if exp.tenantId != uc.tenantId =>
              throw new ResourceAccessDeniedException(uc, exp)
            case None => throw new ExperimentNotFoundException(id)
          })
        } else {
          Future.failed(new IllegalStateException("This should never happen in this mock"))
        }
      })
    }

    override def abort(id: Experiment.Id, nodes: List[Node.Id]): Future[Experiment] = {
      userContext.flatMap(uc => {
        if (uc.tenantId == validAuthTokenTenantB) {
          throw new NoRoleException(uc, "experiments:abort")
        }
        if (uc.tenantId == validAuthTokenTenantA) {
          val experiment = storedExperiments.find(_.id == id)
          Future(experiment match {
            case Some(exp) if exp.tenantId == uc.tenantId => exp
            case Some(exp) if exp.tenantId != uc.tenantId =>
              throw new ResourceAccessDeniedException(uc, exp)
            case None => throw new ExperimentNotFoundException(id)
          })
        } else {
          Future.failed(new IllegalStateException("This should never happen in this mock"))
        }
      })
    }

    override def create(inputExperiment: InputExperiment): Future[Experiment] = {
      userContext.flatMap(uc => {
        if (uc.tenantId == validAuthTokenTenantB) throw new NoRoleException(uc, "experiments:list")
        if (uc.tenantId == validAuthTokenTenantA) {
          val experiment = Experiment(
            UUID.randomUUID(),
            uc.tenantId,
            inputExperiment.name,
            inputExperiment.description,
            inputExperiment.graph)
          Future.successful(experiment)
        } else {
          Future.failed(new IllegalStateException("This should never happen in this mock"))
        }
      })
    }

    override def experiments(
        limit: Option[Int],
        page: Option[Int],
        status: Option[Status.Value]): Future[Seq[Experiment]] = {
      userContext.flatMap(uc => {
        if (uc.tenantId == validAuthTokenTenantB) throw new NoRoleException(uc, "experiments:list")
        if (uc.tenantId == validAuthTokenTenantA) {
          Future.successful(storedExperiments.filter(_.tenantId == uc.tenantId).toSeq)
        } else {
          Future.failed(new IllegalStateException("This should never happen in this mock"))
        }
      })
    }
  }
}
