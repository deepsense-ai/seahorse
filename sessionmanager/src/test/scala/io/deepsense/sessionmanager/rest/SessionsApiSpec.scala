/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest

import scala.concurrent.{Future, Promise}

import org.mockito.Matchers._
import org.mockito.Mockito._
import spray.http.{HttpRequest, StatusCodes}
import spray.httpx.SprayJsonSupport
import spray.json._
import spray.routing.Route

import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.commons.json.envelope.{Envelope, EnvelopeJsonFormat}
import io.deepsense.commons.models.{ClusterDetails, Id}
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.sessionmanager.rest.requests.CreateSession
import io.deepsense.sessionmanager.rest.responses.ListSessionsResponse
import io.deepsense.sessionmanager.service._
import io.deepsense.sessionmanager.service.sessionspawner.SessionConfig

class SessionsApiSpec
  extends StandardSpec
    with UnitTestSupport
    with DefaultJsonProtocol
    with SprayJsonSupport
    with IdJsonProtocol
    with SessionsJsonProtocol {
  import TestData._

  val apiPrefix: String = "sessions"
  val cluster = ClusterDetails(
    name = "name", id = Some(1), clusterType = "yarn", uri = "localhost", userIP = "127.0.0.1"
  )

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
        Session(workflowId1, status1, cluster),
        Session(workflowId2, status2, cluster),
        Session(workflowId3, status3, cluster)
      )

      val service = mock[SessionService]
      when(service.listSessions())
        .thenReturn(Future.successful(ListSessionsResponse(sessions)))

      Get(s"/$apiPrefix").withUserId(someUserId) ~> testRoute(service) ~> check {
        status shouldBe StatusCodes.OK
        responseAs[ListSessionsResponse]
          sessions.map(s => (s.workflowId, s.status)) should contain theSameElementsAs
          List(
            (workflowId1, status1),
            (workflowId2, status2),
            (workflowId3, status3))
      }
    }
    "return 400 Bad Request when X-Seahorse-UserId is not present" in {
      Get(s"/$apiPrefix") ~> testRoute(mock[SessionService]) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }
    "return ServiceUnavailable" when {
      "not yet subscribed to Heartbeats" in {
        Get(s"/$apiPrefix").withUserId(someUserId) ~>
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
        val s = Session(workflowId, sessionStatus, cluster)
        val service = mock[SessionService]
        when(service.getSession(workflowId)).thenReturn(Future.successful(Some(s)))

        Get(s"/$apiPrefix/$workflowId").withUserId(someUserId) ~> testRoute(service) ~> check {
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

        Get(s"/$apiPrefix/${Id.randomId}").withUserId(someUserId) ~> testRoute(service) ~> check {
          status shouldBe StatusCodes.NotFound
        }
      }
    }
    "return ServiceUnavailable" when {
      "not yet subscribed to Heartbeats" in {
        Get(s"/$apiPrefix/${Id.randomId}").withUserId(someUserId) ~>
          testRoute(mock[SessionService], notReadyFuture) ~> check {
          status shouldBe StatusCodes.ServiceUnavailable
        }
      }
    }
    "return 400 Bad Request when X-Seahorse-UserId is not present" in {
      Get(s"/$apiPrefix/${Id.randomId}") ~> testRoute(mock[SessionService]) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }
  }

  "POST /sessions" should {
    "return a session" in {
      val service = mock[SessionService]

      when(service.createSession(
        someSessionConfig, someClusterDetails
      )).thenReturn(Future.successful(someSession))

      Post(s"/$apiPrefix", CreateSession(someWorkflowId, someClusterDetails))
        .withUserId(someUserId) ~> testRoute(service) ~> check {

        status shouldBe StatusCodes.OK
        val returnedSession = responseAs[Envelope[Session]].content
        returnedSession shouldBe someSession
      }
    }
    "return ServiceUnavailable" when {
      "not yet subscribed to Heartbeats" in {
        Post(s"/$apiPrefix", CreateSession(Id.randomId, someClusterDetails))
          .withUserId(someUserId) ~>
          testRoute(mock[SessionService], notReadyFuture) ~> check {
          status shouldBe StatusCodes.ServiceUnavailable
        }
      }
    }
    "return 400 Bad Request" when {
      "X-Seahorse-UserId is not present" in {
        Post(s"/$apiPrefix", CreateSession(Id.randomId, someClusterDetails)) ~>
          testRoute(mock[SessionService]) ~> check {
          status shouldBe StatusCodes.BadRequest
        }
      }
    }
  }

  "DELETE /sessions/:id" should {
    "pass killing request to the service" in {
      val workflowId: Id = Id.randomId
      val service = mock[SessionService]
      when(service.killSession(workflowId)).thenReturn(Future.successful(()))

      Delete(s"/$apiPrefix/$workflowId").withUserId(someUserId)  ~> testRoute(service) ~> check {
        status shouldBe StatusCodes.OK
        verify(service, times(1)).killSession(workflowId)
      }
    }
    "return ServiceUnavailable" when {
      "not yet subscribed to Heartbeats" in {
        Delete(s"/$apiPrefix/${Id.randomId}").withUserId(someUserId) ~>
          testRoute(mock[SessionService], notReadyFuture) ~> check {
          status shouldBe StatusCodes.ServiceUnavailable
        }
      }
    }
  }


  private lazy val someSessionConfig = SessionConfig(
    workflowId = someWorkflowId,
    userId = someUserId
  )
  private lazy val someUserId = Id.randomId.toString
  private lazy val someWorkflowId = Id.randomId
  private lazy val someSession = Session(someWorkflowId, Status.Running, someClusterDetails)

  private implicit class RichHttpRequest(httpRequest: HttpRequest) {
    def withUserId(userId: String): HttpRequest =
      addHeader("X-Seahorse-UserId", userId)(httpRequest)
  }
}
