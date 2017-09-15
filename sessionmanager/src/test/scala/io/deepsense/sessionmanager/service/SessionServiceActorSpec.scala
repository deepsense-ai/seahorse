/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.mockito.Mockito._
import org.mockito.{Matchers => mockito}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.service.SessionServiceActor.KillResponse
import io.deepsense.sessionmanager.service.livy.Livy
import io.deepsense.sessionmanager.service.livy.responses.{BatchStatus, Batch, BatchList}

class SessionServiceActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with ScalaFutures with Matchers with MockitoSugar with BeforeAndAfterAll {

  def this() = this(ActorSystem("SessionServiceActorSpec"))

  implicit val patience = PatienceConfig(timeout = 5.seconds)

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  implicit val timeout: Timeout = 5.seconds

  "SessionServiceActor" when {
    "requested to create a session" when {
      "session does not exist" should {
        val livy = mock[Livy]
        val workflowId = Id.randomId
        val batchId = 5
        val batchStatus = BatchStatus.Running
        val expectedStatus = Status.fromBatchStatus(batchStatus)
        when(livy.createSession(workflowId))
          .thenReturn(Future.successful(Batch(batchId, batchStatus)))
        "create a session" in withActor(livy) {
          actor =>
            val session = askCreate(actor, workflowId)
            whenReady(session) {
              s =>
                s.handle.workflowId shouldBe workflowId
                s.handle.batchId shouldBe batchId
                s.status shouldBe expectedStatus
            }
        }
      }
      "session exists" should {
        val handle = randomHandle
        val workflowId: Id = handle.workflowId
        val livy = mock[Livy]
        when(livy.getSession(handle.batchId))
          .thenReturn(Future.successful(Some(Batch(handle.batchId, BatchStatus.Running))))
        "not create new session" in withActor(livy, Seq(handle)) { actor =>
          whenReady(askCreate(actor, workflowId)) {
            session =>
              session.handle shouldBe handle
              verify(livy, never()).createSession(mockito.any())
          }
        }
      }
    }

    "requested to get a session" should {
      "return existing session" when {
        "the session handle exists" in {
          val livy: Livy = mock[Livy]
          val workflowId = Id.randomId
          val batchId = 5
          val existingSessionHandle = LivySessionHandle(workflowId, batchId)
          when(livy.getSession(batchId))
            .thenReturn(Future.successful(Some(Batch(batchId, BatchStatus.Running))))
          withActor(livy, handles = Seq(existingSessionHandle)) { actor =>
            whenReady(askGet(actor, workflowId)) { session =>
              session.get.status shouldBe Status.Running
            }
          }
        }

        "the session is creating" in {
          val livy: Livy = mock[Livy]
          val workflowId = Id.randomId
          val expectedSession = mock[Session]
          val creatingSession = Future.successful(expectedSession)

          withActor(livy, creating = Map(workflowId -> creatingSession)) { actor =>
            whenReady(askGet(actor, workflowId)) {
              session =>
                session shouldBe Some(expectedSession)
                verifyZeroInteractions(livy)
            }
          }
        }
      }
      "return None" when {
        "the session does not exist" in {
          val livy: Livy = mock[Livy]
          withActor(livy) { actor =>
            whenReady(askGet(actor, Id.randomId)) {
              session =>
                session shouldBe None
                verifyZeroInteractions(livy)
            }
          }
        }
      }
    }

    "requested to kill a session" when {
      val existingSessions = randomSessions()
      val toKill = existingSessions(3)
      "session exists" should {
        "kill the session" in {
          val client = mock[Livy]
          when(client.killSession(toKill.batchId)).thenReturn(Future.successful(true))
          withActor(client, existingSessions) {
            actor =>
              whenReady(askKill(actor, toKill.workflowId)) { _ =>
                verify(client, times(1)).killSession(toKill.batchId)
              }
          }
        }
        "return the killed session" in {
          val client = mock[Livy]
          when(client.killSession(mockito.any())).thenReturn(Future.successful(true))
          withActor(client, existingSessions) {
            actor =>
              whenReady(askKill(actor, toKill.workflowId)) {
                _ shouldBe KillResponse(toKill.workflowId, killed = true)
              }
          }
        }
      }
      "session does not exist" should {
        "return no session" in withActor(mock[Livy], existingSessions) {
          actor =>
            val randomId: Id = Id.randomId
            whenReady(askKill(actor, randomId)) {
              _ shouldBe KillResponse(randomId, killed = false)
            }
        }
        "kill no sessions" in {
          val client: Livy = mock[Livy]
          withActor(client, existingSessions) {
            actor =>
              whenReady(askKill(actor, Id.randomId)) { _ =>
                verify(client, never()).killSession(mockito.any())
              }
          }
        }
      }
    }

    "requested to list sessions" should {
      val sessions = randomSessions()
      val remoteAndLocalSession = sessions(2)
      val remoteSessions = Seq(remoteAndLocalSession, randomHandle)
      val batches = remoteSessions.zipWithIndex.map {
        case (handle, idx) =>
          Batch(handle.batchId, if (idx % 2 == 0) BatchStatus.Running else BatchStatus.Error)
      }

      val client = mock[Livy]
      when(client.listSessions()).thenReturn(Future.successful(BatchList(batches.toList)))
      "returns all intersection of own and remote sessions" in withActor(client, sessions) {
        actor => whenReady(askList(actor)) {
          _ shouldBe List(Session(remoteAndLocalSession, Status.Running))
        }
      }
    }
  }

  private def withActor[T](
    livy: Livy,
    handles: Seq[LivySessionHandle] = Seq.empty,
    creating: Map[Id, Future[Session]] = Map.empty)
      (test: (ActorRef) => T): T = {
    val props = Props(new SessionServiceActor(livy, handles, creating))
    test(system.actorOf(props))
  }

  private def askGet(who: ActorRef, workflowId: Id): Future[Option[Session]] = {
    (who ? SessionServiceActor.GetRequest(workflowId)).mapTo[Option[Session]]
  }

  private def askKill(who: ActorRef, workflowId: Id): Future[KillResponse] = {
    (who ? SessionServiceActor.KillRequest(workflowId)).mapTo[KillResponse]
  }

  private def askList(who: ActorRef): Future[List[Session]] = {
    (who ? SessionServiceActor.ListRequest()).mapTo[List[Session]]
  }

  private def askCreate(who: ActorRef, workflowId: Id): Future[Session] = {
    (who ? SessionServiceActor.CreateRequest(workflowId)).mapTo[Session]
  }

  private def randomHandle: LivySessionHandle =
    LivySessionHandle(Id.randomId, Random.nextInt())

  private def randomSessions(n: Int = 15): Seq[LivySessionHandle] = Seq.fill(n)(randomHandle)
}
