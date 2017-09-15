/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.actors

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
import io.deepsense.sessionmanager.rest.requests.ClusterDetails
import io.deepsense.sessionmanager.service.EventStore.{Event, InvalidWorkflowId, SessionExists}
import io.deepsense.sessionmanager.service.{_}
import io.deepsense.sessionmanager.service.actors.SessionServiceActor.{CreateRequest, GetRequest, KillRequest, ListRequest}
import io.deepsense.sessionmanager.service.executor.SessionExecutorClients
import io.deepsense.sessionmanager.service.sessionspawner.SessionSpawner
import io.deepsense.workflowexecutor.communication.message.global.Heartbeat
import io.deepsense.sessionmanager.rest.requests.ClusterDetails

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

  val cluster = ClusterDetails("yarn", "localhost", Some(1), Some(1), Some(1), Some(1), Some(""))


  // TODO: Currently sessionId == workflowId
  val existingSessionId, existingWorkflowId = Id.randomId
  // TODO: Currently sessionId == workflowId
  val notExistingSessionId, notExistingWorkflowId = Id.randomId

  "SessionServiceActor" when {
    "received a Heartbeat" when {
      "the session exists" should {
        val eventStore = mock[EventStore]
        when(eventStore.heartbeat(existingWorkflowId)).thenReturn(Future.successful(Right(())))

        "forward the event to EventStore" in fixture(eventStore) { p =>
          p.sessionServiceActor ! Heartbeat(existingWorkflowId.toString)
          eventually {
            verify(p.eventStore, times(1)).heartbeat(existingWorkflowId)
          }
        }
      }
      "the session does not exist" should {
        val eventStore = mock[EventStore]
        when(eventStore.heartbeat(notExistingWorkflowId))
          .thenReturn(Future.successful(Left(InvalidWorkflowId())))

        "send a PoisonPill to sender" in fixture(eventStore) { p =>
          p.sessionServiceActor ! Heartbeat(notExistingWorkflowId.toString)
          eventually {
            verify(p.sessionExecutorClients, times(1)).sendPoisonPill(notExistingWorkflowId)
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
            whenReady(session) { s =>
              s should matchPattern {
                case Some(Session(existingSessionId, status, _)) =>
              }
            }
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
        when(eventStore.started(existingSessionId, cluster))
          .thenReturn(Future.successful(Left(SessionExists())))
        "return it's Id" in
          fixture(eventStore) { p =>
            whenReady(sendCreateRequest(p, existingSessionId, Id.randomId.toString, cluster)) {
              _ shouldBe existingSessionId
            }
          }
      }
      "the session does not exist" should {

        def eventStore: EventStore = {
          val m = mock[EventStore]
          when(m.started(notExistingSessionId, cluster)).thenReturn(Future.successful(Right(())))
          m
        }

        def sessionSpawner: SessionSpawner = {
          val m = mock[SessionSpawner]
          when(m.createSession(matchers.eq(notExistingSessionId), any(), any()))
            .thenReturn(Future.successful(()))
          m
        }

        "use session spawner to create a session" in fixture(eventStore, sessionSpawner) { p =>
          val userId = Id.randomId.toString
          sendCreateRequest(p, notExistingSessionId, userId, cluster)
          eventually {
            verify(p.sessionSpawner, times(1)).createSession(notExistingSessionId, userId, cluster)
          }
        }

        "put 'Created' event to EventStore" in fixture(eventStore, sessionSpawner) { p =>
          sendCreateRequest(p, notExistingSessionId, Id.randomId.toString, cluster)
          eventually {
            verify(p.eventStore, times(1)).started(notExistingSessionId, cluster)
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


  private def sendCreateRequest(
      p: TestParams,
      workflowId: Id,
      userId: String,
      cluster: ClusterDetails): Future[Id] =
    (p.sessionServiceActor ? CreateRequest(workflowId, userId, cluster)).mapTo[Id]


  private def fixture[T](eventStore: EventStore)
    (test: (TestParams) => T): T =
    fixture(eventStore, mock[StatusInferencer], mock[SessionSpawner])(test)

  private def fixture[T](
      eventStore: EventStore,
      sessionSpawner: SessionSpawner)(test: (TestParams) => T): T =
    fixture(eventStore, mock[StatusInferencer], sessionSpawner)(test)

  private def fixture[T](
      eventStore: EventStore,
      statusInferencer: StatusInferencer)(test: (TestParams) => T): T = {
    fixture(eventStore, statusInferencer, mock[SessionSpawner])(test)
  }

  private def fixture[T](test: (TestParams) => T): T =
    fixture(mock[EventStore], mock[StatusInferencer], mock[SessionSpawner])(test)

  private def fixture[T](
    eventStore: EventStore,
    statusInferencer: StatusInferencer,
    sessionSpawner: SessionSpawner
  )(test: (TestParams) => T): T = {
    val sessionExecutorClients = mock[SessionExecutorClients]
    val props = Props(new SessionServiceActor(
      sessionSpawner,
      eventStore,
      statusInferencer,
      sessionExecutorClients))
    val params = TestParams(
      system.actorOf(props),
      sessionSpawner,
      eventStore,
      statusInferencer,
      sessionExecutorClients)
    test(params)
  }

  case class TestParams(
    sessionServiceActor: ActorRef,
    sessionSpawner: SessionSpawner,
    eventStore: EventStore,
    statusInferencer: StatusInferencer,
    sessionExecutorClients: SessionExecutorClients
  )
}
