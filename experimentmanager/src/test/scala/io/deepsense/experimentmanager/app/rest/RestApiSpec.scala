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

import io.deepsense.experimentmanager.app.ExperimentManager
import io.deepsense.experimentmanager.app.exceptions.ExperimentNotFoundException
import io.deepsense.experimentmanager.app.models.{Experiment, Graph, Id, InputExperiment}
import io.deepsense.experimentmanager.app.rest.actions.{AbortAction, LaunchAction}
import io.deepsense.experimentmanager.app.rest.json.RestJsonProtocol._
import io.deepsense.experimentmanager.{StandardSpec, UnitTestSupport}

class RestApiSpec extends StandardSpec with UnitTestSupport {

  val inputExperiment = InputExperiment("test name", "test description", Graph())

  case class LaunchActionWrapper(launch: LaunchAction)
  case class AbortActionWrapper(abort: AbortAction)
  implicit val launchWrapperFormat = jsonFormat1(LaunchActionWrapper.apply)
  implicit val abortWrapperFormat = jsonFormat1(AbortActionWrapper.apply)

  var storedExperiments: List[Experiment] = List()

  protected def testRoute = {
    val mockExperimentManager = mock[ExperimentManager]
    when(mockExperimentManager.create(any[InputExperiment]))
      .thenAnswer(new Answer[Future[Experiment]] {
      override def answer(invocation: InvocationOnMock): Future[Experiment] = future {
        val paramExperiment = invocation.getArgumentAt(0, classOf[InputExperiment])
        val sampleExperiment = Experiment(
          UUID.randomUUID(),
          paramExperiment.name,
          paramExperiment.description,
          paramExperiment.graph)
        storedExperiments = storedExperiments :+ sampleExperiment
        sampleExperiment
      }})

    when(mockExperimentManager.get(any[Experiment.Id]))
      .thenAnswer(new Answer[Future[Option[Experiment]]]{
      override def answer(invocation: InvocationOnMock): Future[Option[Experiment]] = future {
        val id = invocation.getArgumentAt(0, classOf[Experiment.Id])
        storedExperiments.find(_.id == id)
      }})

    when(mockExperimentManager.delete(any[Experiment.Id]))
      .thenAnswer(new Answer[Future[Unit]]{
      override def answer(invocation: InvocationOnMock): Future[Unit] = future {
        val id = invocation.getArgumentAt(0, classOf[Experiment.Id])
        storedExperiments = storedExperiments.filterNot(_.id == id)
      }})

    when(mockExperimentManager.experiments(
      any[Option[Int]],
      any[Option[Int]],
      any[Option[Experiment.Status.Value]]))
      .thenAnswer(new Answer[Future[IndexedSeq[Experiment]]]{
      override def answer(invocation: InvocationOnMock): Future[IndexedSeq[Experiment]] = future {
        storedExperiments.toIndexedSeq
      }})

    when(mockExperimentManager.launch(
      any[Experiment.Id],
      any[List[Graph.Node.Id]]))
      .thenAnswer(new Answer[Future[Experiment]]{
      override def answer(invocation: InvocationOnMock): Future[Experiment] = future {
        val id = invocation.getArgumentAt(0, classOf[Experiment.Id])
        storedExperiments.find(_.id == id) match {
          case Some(experiment) => experiment
          case None => throw new ExperimentNotFoundException(id)
        }
      }})

    when(mockExperimentManager.abort(
      any[Experiment.Id],
      any[List[Graph.Node.Id]]))
      .thenAnswer(new Answer[Future[Experiment]]{
      override def answer(invocation: InvocationOnMock): Future[Experiment] = future {
        val id = invocation.getArgumentAt(0, classOf[Experiment.Id])
        storedExperiments.find(_.id == id) match {
          case Some(experiment) => experiment
          case None => throw new ExperimentNotFoundException(id)
        }
      }})

    new RestApi(mockExperimentManager).route
  }

  "GET /experiments" should {
    "return a list of experiments" when {
      "was not asked for an experiment" in {
        Get("/experiments") ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          responseAs[List[Experiment]]
        }
      }
    }
  }

  "GET /experiments/:id" should {
    "return an experiment" when {
      var savedExperiment: Experiment = null
      "asked for an experiment that was just created with POST" in {
        Post("/experiments", inputExperiment) ~> testRoute ~> check {
          savedExperiment = responseAs[Experiment]
        }
        Get(s"/experiments/${savedExperiment.id}") ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          val returnedExperiment = responseAs[Experiment]
          returnedExperiment should be(savedExperiment)
        }
      }
    }
    "return with not found" when {
      "asked for an non-existing experiment" in {
        val nonExistingId = UUID.randomUUID()
        Get(s"/experiments/$nonExistingId") ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
      "asked for a deleted experiment" in {
        var savedExperiment: Experiment = null
        Post("/experiments", inputExperiment) ~> testRoute ~> check {
          savedExperiment = responseAs[Experiment]
        }
        Delete(s"/experiments/${savedExperiment.id}") ~> testRoute ~> check {
        }
        Get(s"/experiments/${savedExperiment.id}") ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
  }

  "DELETE /experiments/:id" should {
    "not fail" when {
      "experiment does not exist" in {
        val nonExistingId = UUID.randomUUID()
        Delete(s"/experiments/$nonExistingId") ~> testRoute ~> check {
          status should be(StatusCodes.OK)
        }
      }
    }
    "delete an experiment" when {
      "experiment exists" in {
        var savedExperiment: Experiment = null
        Post("/experiments", inputExperiment) ~> testRoute ~> check {
          savedExperiment = responseAs[Experiment]
        }
        Delete(s"/experiments/${savedExperiment.id}") ~> testRoute ~> check {
          status should be(StatusCodes.OK)
        }
        Get(s"/experiments/${savedExperiment.id}") ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
  }

  "POST /experiments" should {
    "crate an experiment" in {
      Post("/experiments", inputExperiment) ~> testRoute ~> check {
        status should be (StatusCodes.Created)
        val savedExperiment = responseAs[Experiment]
        savedExperiment should have (
          'name (inputExperiment.name),
          'description (inputExperiment.description),
          'graph (inputExperiment.graph)
        )
      }
    }
  }

  "POST /experiments/:id/action" should {
    "launch experiment" when {
      "launch action was post and the experiment exists" in {
        val launchAction = LaunchActionWrapper(LaunchAction(Some(List(Id(UUID.randomUUID())))))
        var savedExperiment: Experiment = null
        Post("/experiments", inputExperiment) ~> testRoute ~> check {
          savedExperiment = responseAs[Experiment]
        }

        Post(s"/experiments/${savedExperiment.id}/action", launchAction) ~> testRoute ~> check {
          status should be (StatusCodes.Accepted)
          val response = responseAs[Experiment]
          response should have(
            'id (savedExperiment.id),
            'name (savedExperiment.name),
            'graph (savedExperiment.graph),
            'description (savedExperiment.description)
          )
        }
      }
    }
    "not launch experiment" when {
      "launch action was post but the experiment does not exist" in {
        val launchAction = LaunchActionWrapper(LaunchAction(Some(List(Id(UUID.randomUUID())))))
        val nonExistingId = UUID.randomUUID()

        Post(s"/experiments/$nonExistingId/action", launchAction) ~> testRoute ~> check {
          status should be (StatusCodes.NotFound)
          // TODO response should say why it was rejected
        }
      }
    }
    "abort experiment" when {
      "abort action was post and the experiment exists" in {
        val abortAction = AbortActionWrapper(AbortAction(Some(List(Id(UUID.randomUUID())))))
        var savedExperiment: Experiment = null
        Post("/experiments", inputExperiment) ~> testRoute ~> check {
          savedExperiment = responseAs[Experiment]
        }

        Post(s"/experiments/${savedExperiment.id}/action", abortAction) ~> testRoute ~> check {
          status should be (StatusCodes.Accepted)
          val response = responseAs[Experiment]
          response should have(
            'id (savedExperiment.id),
            'name (savedExperiment.name),
            'graph (savedExperiment.graph),
            'description (savedExperiment.description)
          )
        }
      }
    }
    "not abort experiment" when {
      "abort action was post but the experiment does not exist" in {
        val abortAction = AbortActionWrapper(AbortAction(Some(List(Id(UUID.randomUUID())))))
        val nonExistingId = UUID.randomUUID()

        Post(s"/experiments/$nonExistingId/action", abortAction) ~> testRoute ~> check {
          status should be (StatusCodes.NotFound)
          // TODO response should say why it was rejected
        }
      }
    }
  }
  // TODO handle errors
}
