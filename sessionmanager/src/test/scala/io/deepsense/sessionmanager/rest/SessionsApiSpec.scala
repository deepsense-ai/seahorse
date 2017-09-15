/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest

import scala.concurrent.{Future, Promise}

import org.mockito.Matchers._
import org.mockito.Mockito._
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport
import spray.json._
import spray.routing.Route

import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.commons.json.envelope.{Envelope, EnvelopeJsonFormat}
import io.deepsense.commons.models.Id
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.sessionmanager.rest.requests.CreateSession
import io.deepsense.sessionmanager.rest.responses.ListSessionsResponse
import io.deepsense.sessionmanager.service._

class SessionsApiSpec
  extends StandardSpec
    with UnitTestSupport
    with DefaultJsonProtocol
    with SprayJsonSupport
    with IdJsonProtocol
    with SessionsJsonProtocol {
  val apiPrefix: String = "sessions"

  def testRoute(
      service: SessionService,
      subscribed: Future[Unit] = Future.successful(())): Route = {
    new SessionsApi(apiPrefix, subscribed, service).route
  }

  val falsePromise = Promise[Unit]()
  val notReadyFuture = falsePromise.future

  implicit val envelopedSessionFormat = new EnvelopeJsonFormat[Session]("session")
  implicit val envelopedSessionIdFormat = new EnvelopeJsonFormat[Id]("sessionId")

  "GET /sessions" should {
    "list all sessions" in {
      val workflowId1 = Id.randomId
      val workflowId2 = Id.randomId
      val workflowId3 = Id.randomId
      val status1 = Status.Running
      val status2 = Status.Error
      val status3 = Status.Running
      val sessions = List(
        Session(workflowId1, status1),
        Session(workflowId2, status2),
        Session(workflowId3, status3)
      )

      val service = mock[SessionService]
      when(service.listSessions())
        .thenReturn(Future.successful(ListSessionsResponse(sessions)))

      Get(s"/$apiPrefix") ~> testRoute(service) ~> check {
        status shouldBe StatusCodes.OK
        responseAs[ListSessionsResponse]
          sessions.map(s => (s.workflowId, s.status)) should contain theSameElementsAs
          List(
            (workflowId1, status1),
            (workflowId2, status2),
            (workflowId3, status3))
      }
    }
    "return ServiceUnavailable" when {
      "not yet subscribed to Heartbeats" in {
        Get(s"/$apiPrefix") ~>
          testRoute(mock[SessionService], notReadyFuture) ~> check {
          status shouldBe StatusCodes.ServiceUnavailable
        }
      }
    }
  }

  "GET /sessions/:id" should {
    "return session" when {
      "session exists" in {
        val workflowId: Id = Id.randomId
        val sessionStatus: Status.Value = Status.Error
        val s = Session(workflowId, sessionStatus)
        val service = mock[SessionService]
        when(service.getSession(workflowId)).thenReturn(Future.successful(Some(s)))

        Get(s"/$apiPrefix/$workflowId") ~> testRoute(service) ~> check {
          status shouldBe StatusCodes.OK
          val returnedSession = responseAs[Envelope[Session]].content
          returnedSession.workflowId shouldBe workflowId
          returnedSession.status shouldBe sessionStatus
        }
      }
    }
    "return NotFound" when {
      "session does not exist" in {
        val service = mock[SessionService]
        when(service.getSession(any())).thenReturn(Future.successful(None))

        Get(s"/$apiPrefix/${Id.randomId}") ~> testRoute(service) ~> check {
          status shouldBe StatusCodes.NotFound
        }
      }
    }
    "return ServiceUnavailable" when {
      "not yet subscribed to Heartbeats" in {
        Get(s"/$apiPrefix/${Id.randomId}") ~>
          testRoute(mock[SessionService], notReadyFuture) ~> check {
          status shouldBe StatusCodes.ServiceUnavailable
        }
      }
    }
  }

  "POST /sessions" should {
    "return a session" in {
      val workflowId: Id = Id.randomId
      val s = workflowId
      val service = mock[SessionService]
      when(service.createSession(workflowId)).thenReturn(Future.successful(s))

      Post(s"/$apiPrefix", CreateSession(workflowId)) ~> testRoute(service) ~> check {
        status shouldBe StatusCodes.OK
        val returnedSessionId = responseAs[Envelope[Id]].content
        returnedSessionId shouldBe workflowId
      }
    }
    "return ServiceUnavailable" when {
      "not yet subscribed to Heartbeats" in {
        Post(s"/$apiPrefix", CreateSession(Id.randomId)) ~>
          testRoute(mock[SessionService], notReadyFuture) ~> check {
          status shouldBe StatusCodes.ServiceUnavailable
        }
      }
    }
  }

  "DELETE /sessions/:id" should {
    "pass killing request to the service" in {
      val workflowId: Id = Id.randomId
      val service = mock[SessionService]
      when(service.killSession(workflowId)).thenReturn(Future.successful(()))

      Delete(s"/$apiPrefix/$workflowId") ~> testRoute(service) ~> check {
        status shouldBe StatusCodes.OK
        verify(service, times(1)).killSession(workflowId)
      }
    }
    "return ServiceUnavailable" when {
      "not yet subscribed to Heartbeats" in {
        Delete(s"/$apiPrefix/${Id.randomId}")~>
          testRoute(mock[SessionService], notReadyFuture) ~> check {
          status shouldBe StatusCodes.ServiceUnavailable
        }
      }
    }
  }
}
