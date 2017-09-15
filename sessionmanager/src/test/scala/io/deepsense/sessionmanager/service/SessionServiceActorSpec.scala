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
import io.deepsense.sessionmanager.service.livy.Livy
import io.deepsense.sessionmanager.service.livy.responses.{Batch, BatchList, BatchState}
import io.deepsense.sessionmanager.storage.SessionStorage.SessionRow
import io.deepsense.sessionmanager.storage._
import io.deepsense.sessionmanager.storage.inmemory.InMemorySessionStorage

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
        val Some(batchId) = Some(5)
        val batchStatus = BatchState.Running
        when(livy.createSession(workflowId))
          .thenReturn(Future.successful(Batch(batchId, batchStatus)))
        "create a session" in withActor(livy) {
          actor =>
            whenReady(askCreate(actor, workflowId)) {
              _ shouldBe workflowId
            }
        }
      }
      "session exists" should {
        val row = randomRow()
        val workflowId: Id = row.workflowId
        val livy = mock[Livy]
        val batch = Batch(row.optBatchId.get, BatchState.Running)
        val expectedSession = Session(workflowId, batch)
        when(livy.getSession(row.optBatchId.get))
          .thenReturn(Future.successful(Some(batch)))
        "not create a new batch session but return existing" in
          withActor(livy, storageWithRows(row)) { actor =>
            whenReady(askCreate(actor, workflowId)) {
              sessionId =>
                sessionId shouldBe expectedSession.workflowId
                verify(livy, never()).createSession(mockito.any())
            }
          }
        "result in one batch in livy" in withActor(livy, storageWithRows(row)) { actor =>
          whenReady(askCreate(actor, workflowId)) {
            sessionId =>
              sessionId shouldBe expectedSession.workflowId
              verify(livy, never()).createSession(mockito.any())
          }
        }
      }
    }

    "requested to get a session" should {
      "return existing session" when {
        "the session row exists" in {
          val livy: Livy = mock[Livy]
          val workflowId = Id.randomId
          val optBatchId@Some(batchId) = Some(5)
          val storedSessionRow = SessionRow(workflowId, optBatchId)
          when(livy.getSession(batchId))
            .thenReturn(Future.successful(Some(Batch(batchId, BatchState.Running))))
          withActor(livy, storageWithRows(storedSessionRow)) { actor =>
            whenReady(askGet(actor, workflowId)) { session =>
              session.get.status shouldBe Status.Running
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

        "the session was running and stored but it doesn't exist in livy" in {
          val livy: Livy = mock[Livy]
          val workflowId = Id.randomId
          val optBatchId@Some(batchId) = Some(5)
          val storedSessionRow = SessionRow(workflowId, optBatchId)
          when(livy.listSessions())
            .thenReturn(Future.successful(BatchList(List(Batch(1, BatchState.Running)))))
          when(livy.getSession(batchId))
            .thenReturn(Future.successful(None))
          withActor(livy, storageWithRows(storedSessionRow)) { actor =>
            whenReady(askGet(actor, workflowId)) { session =>
              session shouldBe None
              whenReady(askGetAll(actor)) { seq =>
                seq.exists(s => s.workflowId == workflowId) shouldBe false
              }
            }
          }
        }
      }
    }

    "requested to kill a session" when {
      val existingSessions = randomRows()
      val toKill@SessionRow(_, Some(batchId), _) = existingSessions(3)
      "session exists" should {
        "kill the session" in {
          val client = mock[Livy]
          when(client.killSession(batchId)).thenReturn(Future.successful(true))
          withActor(client, storageWithRows(existingSessions: _*)) {
            actor =>
              whenReady(askKill(actor, toKill.workflowId)) { _ =>
                verify(client, times(1)).killSession(batchId)
              }
          }
        }
        "return success" in {
          val client = mock[Livy]
          when(client.killSession(mockito.any[Int]())).thenReturn(Future.successful(true))
          withActor(client, storageWithRows(existingSessions: _*)) {
            actor =>
              whenReady(askKill(actor, toKill.workflowId)) {
                _ shouldBe ()
              }
          }
        }
      }
      "session does not exist" should {
        "return success" in withActor(mock[Livy], storageWithRows(existingSessions: _*)) {
          actor =>
            val randomId: Id = Id.randomId
            whenReady(askKill(actor, randomId)) {
              _ shouldBe ()
            }
        }
        "kill no sessions" in {
          val client: Livy = mock[Livy]
          withActor(client, storageWithRows(existingSessions: _*)) {
            actor =>
              whenReady(askKill(actor, Id.randomId)) { _ =>
                verify(client, never()).killSession(mockito.any())
              }
          }
        }
      }
    }

    "requested to list sessions" should {
      val creatingRow = randomRow(withBatch = false)
      val creatingSession = Session(creatingRow.workflowId)
      val runningAndNotInLivySessionRow = randomRow()
      val runningAndInLivySessionRow = randomRow()
      val runnignAndInLivySession =
        Session(
          runningAndInLivySessionRow.workflowId,
          Batch(runningAndInLivySessionRow.optBatchId.get, BatchState.Running))

      val client = mock[Livy]
      when(client.listSessions())
        .thenReturn(Future.successful(
          BatchList(List(Batch(runningAndInLivySessionRow.optBatchId.get, BatchState.Running)))))
      when(client.getSession(runningAndNotInLivySessionRow.optBatchId.get))
        .thenReturn(Future.successful(None))
      when(client.getSession(runningAndInLivySessionRow.optBatchId.get))
        .thenReturn(Future.successful(
          Some(Batch(runningAndInLivySessionRow.optBatchId.get, BatchState.Running))))
      when(client.killSession(mockito.any[Int]()))
        .thenReturn(Future.successful(true))
      "return stored creating and running sessions" in
          withActor(client, storageWithRows(
              creatingRow, runningAndNotInLivySessionRow, runningAndInLivySessionRow)) {
            actor => whenReady(askList(actor)) {
              _ shouldBe List(runnignAndInLivySession, creatingSession)
            }
          }
      }
  }

  private def withActor[T](
    livy: Livy,
    sessionStorage: SessionStorage = new InMemorySessionStorage)
      (test: (ActorRef) => T): T = {
    val props = Props(new SessionServiceActor(livy, sessionStorage))
    test(system.actorOf(props))
  }

  private def askGet(who: ActorRef, workflowId: Id): Future[Option[Session]] = {
    (who ? SessionServiceActor.GetRequest(workflowId)).mapTo[Option[Session]]
  }

  private def askGetAll(who: ActorRef): Future[Seq[Session]] = {
    (who ? SessionServiceActor.ListRequest()).mapTo[Seq[Session]]
  }

  private def askKill(who: ActorRef, workflowId: Id): Future[Unit] = {
    (who ? SessionServiceActor.KillRequest(workflowId)).mapTo[Unit]
  }

  private def askList(who: ActorRef): Future[List[Session]] = {
    (who ? SessionServiceActor.ListRequest()).mapTo[List[Session]]
  }

  private def askCreate(who: ActorRef, workflowId: Id): Future[Id] = {
    (who ? SessionServiceActor.CreateRequest(workflowId)).mapTo[Id]
  }

  private def randomRow(withBatch: Boolean = true): SessionRow =
    SessionRow(
      Id.randomId,
      if (withBatch) Some(Random.nextInt) else None,
      Random.nextInt())

  private def randomRows(n: Int = 15): Seq[SessionRow] =
    Range(0, n).map(_ => randomRow())

  private def storageWithRows(rows: SessionRow*): SessionStorage = {
    new InMemorySessionStorage(rows)
  }
}
