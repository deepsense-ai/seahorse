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
import io.deepsense.experimentmanager.app.models.{Graph, Experiment}
import io.deepsense.experimentmanager.app.rest.json.RestJsonProtocol._
import io.deepsense.experimentmanager.{StandardSpec, UnitTestSupport}

class RestApiSpec extends StandardSpec with UnitTestSupport {

  val unknownId = Experiment.Id(UUID.randomUUID())
  val experiments = List(
    Experiment(UUID.randomUUID(), "experiment 1", "", Graph()),
    Experiment(UUID.randomUUID(), "experiment 2", "Description", Graph()))

  val experiment0 = experiments(0)

  protected def testRoute = {
    val mockExperimentManager = mock[ExperimentManager]
    when(mockExperimentManager.get(any[Experiment.Id]))
      .thenAnswer(new Answer[Future[Option[Experiment]]] {
      override def answer(invocation: InvocationOnMock): Future[Option[Experiment]] = future {
        val id = invocation.getArgumentAt(0, classOf[Experiment.Id])
        experiments.find(_.id.equals(id))
      }
    })

    new RestApi(mockExperimentManager).route
  }

  s"GET /experiments/${experiments(0).id}" should {
    "respond with success" when {
      "the specified experiment exists" in {
        Get(s"/experiments/${experiments(0).id}") ~> testRoute ~> check {
          status should be(StatusCodes.OK)
          val r = responseAs[Experiment]
          r should have (
            'id (experiment0.id),
            'name (experiment0.name),
            'description (experiment0.description)
          )
        }
      }
    }
    "respond with not-found" when {
      "the specified experiment does not exist" in {
        Get(s"/experiments/$unknownId") ~> testRoute ~> check {
          status should be(StatusCodes.NotFound)
        }
      }
    }
  }
}
