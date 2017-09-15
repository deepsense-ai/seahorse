/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest

import scala.concurrent.Future

import org.mockito.Matchers._
import org.mockito.Mockito._
import spray.http.{HttpRequest, StatusCodes}
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
    with IdJsonProtocol {
  val apiPrefix: String = "sessions"

  def testRoute(service: SessionService): Route = {
    new SessionsApi(apiPrefix, service).route
  }

  implicit val rawSessionFormat = new RootJsonFormat[Session] with IdJsonProtocol {
    val sessionWriter: RootJsonWriter[Session] =
      SessionsJsonProtocol.sessionFormat

    override def read(json: JsValue): Session = {
      val obj = json.asJsObject
      Session(
        LivySessionHandle(
          obj.fields("workflowId").convertTo[Id], 0),
          Status.withName(obj.fields("status").convertTo[String]))
    }

    override def write(obj: Session): JsValue = {
      obj.toJson(sessionWriter)
    }
  }

  implicit val envelopedSessionFormat = new EnvelopeJsonFormat[Session]("session")
  implicit val envelopedSessionIdFormat = new EnvelopeJsonFormat[Id]("sessionId")

  implicit val listSessionsResponseFormat = new RootJsonFormat[ListSessionsResponse] {
    val protocolFormat = SessionsJsonProtocol.listSessionsResponseFormat

    override def read(json: JsValue): ListSessionsResponse = {
      val sessions = json.asJsObject.fields("sessions")
        .asInstanceOf[JsArray]
        .elements.map(_.convertTo[Session](rawSessionFormat)).toList
      ListSessionsResponse(sessions)
    }

    override def write(obj: ListSessionsResponse): JsValue = {
      obj.toJson(protocolFormat)
    }
  }

  implicit val createSessionFormat = SessionsJsonProtocol.createSessionFormat

  "GET /sessions" should {
    "list all sessions" in {
      val userId: String = Id.randomId.toString
      val workflowId1 = Id.randomId
      val workflowId2 = Id.randomId
      val workflowId3 = Id.randomId
      val status1 = Status.Running
      val status2 = Status.Error
      val status3 = Status.Running
      val sessions = List(
        session(workflowId1, status1),
        session(workflowId2, status2),
        session(workflowId3, status3)
      )

      val service = mock[SessionService]
      when(service.listSessions())
        .thenReturn(Future.successful(ListSessionsResponse(sessions)))

      Get(s"/$apiPrefix").withUserId(userId) ~> testRoute(service) ~> check {
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
  }

  "GET /sessions/:id" should {
    "return session" when {
      "session exists" in {
        val workflowId: Id = Id.randomId
        val userId: String = Id.randomId.toString
        val sessionStatus: Status.Value = Status.Error
        val s = session(workflowId, sessionStatus)
        val service = mock[SessionService]
        when(service.getSession(workflowId)).thenReturn(Future.successful(Some(s)))

        Get(s"/$apiPrefix/$workflowId").withUserId(userId) ~> testRoute(service) ~> check {
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
        val userId: String = Id.randomId.toString
        when(service.getSession(any())).thenReturn(Future.successful(None))

        Get(s"/$apiPrefix/${Id.randomId}").withUserId(userId) ~> testRoute(service) ~> check {
          status shouldBe StatusCodes.NotFound
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
      val workflowId: Id = Id.randomId
      val userId: String = Id.randomId.toString
      val s = workflowId
      val service = mock[SessionService]
      when(service.createSession(workflowId, userId)).thenReturn(Future.successful(s))

      Post(s"/$apiPrefix", CreateSession(workflowId))
        .withUserId(userId) ~> testRoute(service) ~> check {

        status shouldBe StatusCodes.OK
        val returnedSessionId = responseAs[Envelope[Id]].content
        returnedSessionId shouldBe workflowId
      }
    }
    "return 400 Bad Request when X-Seahorse-UserId is not present" in {
      Post(s"/$apiPrefix", CreateSession(Id.randomId)) ~> testRoute(mock[SessionService]) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }
  }

  "DELETE /sessions/:id" should {
    "pass killing request to the service" in {
      val workflowId: Id = Id.randomId
      val userId: String = Id.randomId.toString
      val service = mock[SessionService]
      when(service.killSession(workflowId)).thenReturn(Future.successful(()))

      Delete(s"/$apiPrefix/$workflowId").withUserId(userId)  ~> testRoute(service) ~> check {
        status shouldBe StatusCodes.OK
        verify(service, times(1)).killSession(workflowId)
      }
    }
  }

  private def session(workflowId: Id, status: Status.Value): Session = {
    Session(LivySessionHandle(workflowId, 0), status)
  }

  private implicit class UserIdAddition(httpRequest: HttpRequest) {
    def withUserId(userId: String): HttpRequest =
      addHeader("X-Seahorse-UserId", userId)(httpRequest)
  }
}
