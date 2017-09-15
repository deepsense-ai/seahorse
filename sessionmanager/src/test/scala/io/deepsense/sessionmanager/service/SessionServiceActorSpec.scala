/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import scala.concurrent.Future
import scala.concurrent.duration._

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.{Matchers => matchers}
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.service.EventStore.{Event, InvalidWorkflowId, SessionExists}
import io.deepsense.sessionmanager.service.actors.SessionServiceActor
import io.deepsense.sessionmanager.service.actors.SessionServiceActor.{CreateRequest, GetRequest, KillRequest, ListRequest}
import io.deepsense.sessionmanager.service.executor.SessionExecutorClients
import io.deepsense.sessionmanager.service.livy.Livy
import io.deepsense.sessionmanager.service.livy.responses.Batch
import io.deepsense.workflowexecutor.communication.message.global.Heartbeat

class SessionServiceActorSpec(_system: ActorSystem)
  extends TestKit(_system)
    with ImplicitSender
    with WordSpecLike
    with ScalaFutures
    with Matchers
    with MockitoSugar
    with BeforeAndAfterAll
    with Eventually {

  def this() = this(ActorSystem("SessionServiceActorSpec"))

  implicit val patience = PatienceConfig(timeout = 5.seconds)

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  implicit val timeout: Timeout = 5.seconds

  val existingSessionId = Id.randomId
  val notExistingSessionId = Id.randomId

  "SessionServiceActor" when {
    "received a Heartbeat" when {
      "the session exists" should {
        val eventStore = mock[EventStore]
        when(eventStore.heartbeat(existingSessionId)).thenReturn(Future.successful(Right(())))

        "forward the event to EventStore" in fixture(eventStore) { p =>
          p.sessionServiceActor ! Heartbeat(existingSessionId.toString)
          eventually {
            verify(p.eventStore, times(1)).heartbeat(existingSessionId)
          }
        }
      }
      "the session does not exist" should {
        val eventStore = mock[EventStore]
        when(eventStore.heartbeat(notExistingSessionId))
          .thenReturn(Future.successful(Left(InvalidWorkflowId())))

        "send a PoisonPill to sender" in fixture(eventStore) { p =>
          p.sessionServiceActor ! Heartbeat(notExistingSessionId.toString)
          eventually {
            verify(p.sessionExecutorClients, times(1)).sendPoisonPill(notExistingSessionId)
          }
        }
      }
    }

    "received a GetRequest" when {
      "the session exists" should {
        val eventStore = mock[EventStore]
        val event = mock[Event]
        when(eventStore.getLastEvent(existingSessionId)).thenReturn(Future.successful(Some(event)))
        val statusInferencer = mock[StatusInferencer]

        "use StatusInferencer to calculate SessionState" in
          fixture(eventStore, statusInferencer) { p =>
            val status = mock[Status.Value]
            when(statusInferencer.statusFromEvent(matchers.eq(event), any()))
              .thenReturn(status)

            val session = sendGetRequest(p, existingSessionId)
            eventually {
              verify(statusInferencer, times(1))
                .statusFromEvent(same(event), anyObject())
            }
            whenReady(session) { _ shouldBe Some(Session(existingSessionId, status)) }
        }
      }
      "the session does not exist" should {
        val eventStore = mock[EventStore]
        when(eventStore.getLastEvent(notExistingSessionId)).thenReturn(Future.successful(None))
        "return None" in fixture(eventStore) { p =>
          val session = sendGetRequest(p, notExistingSessionId)
          whenReady(session) { _ shouldBe None }
        }
      }
    }

    "received a CreateRequest" when {
      "the session exists" should {
        val eventStore = mock[EventStore]
        when(eventStore.started(existingSessionId))
          .thenReturn(Future.successful(Left(SessionExists())))
        "return it's Id" in
          fixture(eventStore) { p =>
            whenReady(sendCreateRequest(p, existingSessionId)) {
              _ shouldBe existingSessionId
            }
          }
      }
      "the session does not exist" should {
        def eventStore: EventStore = {
          val m = mock[EventStore]
          when(m.started(notExistingSessionId)).thenReturn(Future.successful(Right(())))
          m
        }

        def livy: Livy = {
          val m = mock[Livy]
          when(m.createSession(notExistingSessionId))
            .thenReturn(Future.successful(mock[Batch]))
          m
        }

        "use Livy to create a session" in fixture(eventStore, livy) { p =>
          sendCreateRequest(p, notExistingSessionId)
          eventually {
            verify(p.livy, times(1)).createSession(notExistingSessionId)
          }
        }

        "put 'Created' event to EventStore" in fixture(eventStore, livy) { p =>
          sendCreateRequest(p, notExistingSessionId)
          eventually {
            verify(p.eventStore, times(1)).started(notExistingSessionId)
          }
        }
      }
    }

    "received a KillRequest" when {
      "forward 'Killed' event to EventStore" in fixture { p =>
        val id = Id.randomId
        p.sessionServiceActor ! KillRequest(id)
        eventually {
          verify(p.eventStore, times(1)).killed(id)
        }
      }
      "send a PoisonPill to the appropriate WorkflowId" in fixture { p =>
        val id = Id.randomId
        p.sessionServiceActor ! KillRequest(id)
        eventually {
          verify(p.sessionExecutorClients, times(1)).sendPoisonPill(id)
        }
      }
    }

    "received ListRequest" should {
      val events = Map(
        Id.randomId -> mock[Event],
        Id.randomId -> mock[Event],
        Id.randomId -> mock[Event]
      )
      val eventStore = mock[EventStore]
      when(eventStore.getLastEvents).thenReturn(Future.successful(events))
      val statusInferencer = mock[StatusInferencer]
      when(statusInferencer.statusFromEvent(any(), any()))
        .thenReturn(mock[Status.Value])

      "get all last events from Event store" in fixture(eventStore) { p =>
        p.sessionServiceActor ! ListRequest()
        eventually {
          verify(p.eventStore, times(1)).getLastEvents
        }
      }
      "use StatusInferencer to calculate SessionStates" in
        fixture(eventStore, statusInferencer) { p =>
          val response = (p.sessionServiceActor ? ListRequest()).mapTo[Seq[Session]]
          whenReady(response) { sessionList =>
            events.foreach {
              case (_, event) =>
                verify(p.statusInferencer, times(1))
                  .statusFromEvent(matchers.eq(event), any())
            }
          }
      }
    }
  }

  private def sendGetRequest(p: TestParams, workflowId: Id): Future[Option[Session]] = {
    (p.sessionServiceActor ? GetRequest(workflowId))
      .mapTo[Option[Session]]
  }

  private def sendCreateRequest(p: TestParams, workflowId: Id): Future[Id] =
    (p.sessionServiceActor ? CreateRequest(workflowId)).mapTo[Id]

  private def fixture[T](eventStore: EventStore)(test: (TestParams) => T): T =
    fixture(eventStore, mock[StatusInferencer], mock[Livy])(test)

  private def fixture[T](eventStore: EventStore, livy: Livy)(test: (TestParams) => T): T =
    fixture(eventStore, mock[StatusInferencer], livy)(test)

  private def fixture[T](
      eventStore: EventStore,
      statusInferencer: StatusInferencer)(test: (TestParams) => T): T = {
    fixture(eventStore, statusInferencer, mock[Livy])(test)
  }

  private def fixture[T](test: (TestParams) => T): T =
    fixture(mock[EventStore], mock[StatusInferencer], mock[Livy])(test)

  private def fixture[T](
    eventStore: EventStore,
    statusInferencer: StatusInferencer,
    livy: Livy
  )(test: (TestParams) => T): T = {
    val sessionExecutorClients = mock[SessionExecutorClients]
    val props = Props(new SessionServiceActor(
      livy,
      eventStore,
      statusInferencer,
      sessionExecutorClients))
    val params = TestParams(
      system.actorOf(props),
      livy,
      eventStore,
      statusInferencer,
      sessionExecutorClients)
    test(params)
  }

  case class TestParams(
    sessionServiceActor: ActorRef,
    livy: Livy,
    eventStore: EventStore,
    statusInferencer: StatusInferencer,
    sessionExecutorClients: SessionExecutorClients
  )
}
