/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.storage.impl

import java.util.UUID

import scala.concurrent.{Await, Future}
import scala.util.{Random, Right}

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers}

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.storage.SessionStorage._

class SessionStorageImplIntegSpec
  extends StandardSpec
  with ScalaFutures
  with MockitoSugar
  with Matchers
  with BeforeAndAfter
  with SlickTestSupport
  with Logging {

  var sessionsStorage: SessionStorageImpl = _

  val (id1, id2, idCreated, idNotStored) =
    (UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())

  val session1@(_, row1) = (id1, SessionRow(id1, Some(2), Random.nextInt()))
  val session2@(_, row2) = (id2, SessionRow(id2, Some(4), Random.nextInt()))
  val sessionCreated@(_, rowCreated) = (idCreated, SessionRow(idCreated, None, Random.nextInt()))
  val sessionNotStored@(_, rowNotStored) =
    (idNotStored, SessionRow(idNotStored, Some(9), Random.nextInt()))

  val storedSessions: Map[UUID, SessionRow] =
    Map(id1 -> row1, id2 -> row2, idCreated -> rowCreated)

  before {
    sessionsStorage = new SessionStorageImpl(db, driver, "SESSIONS")
  }

  "SessionsDao" should {

    "get session by id" in withStoredSessions(storedSessions) {
      val id = id1
      val expectedOptSessionRow = Some(storedSessions(id))
      whenReady(sessionsStorage.get(id)) { optSessionRow =>
        optSessionRow shouldBe expectedOptSessionRow
      }
    }

    "return None if session doesn't exist" in withStoredSessions(storedSessions) {
      val id = idNotStored
      whenReady(sessionsStorage.get(id)) {
        _ shouldBe None
      }
    }

    "get all sessions" in withStoredSessions(storedSessions) {
      whenReady(sessionsStorage.getAll) { sessions =>
        sessions.size shouldBe storedSessions.size
        sessions.map {
          case (id, sessionRow) =>
            (id.toString, sessionRow)} shouldBe storedSessions.map {
              case (id, sessionRow) => (id.toString, sessionRow)
            }
      }
    }

    "allow to create a not stored session" in withStoredSessions(storedSessions) {
      val id = idNotStored
      whenReady(sessionsStorage.create(id)) {
        case Right(CreateSucceeded(version)) =>
          whenReady(sessionsStorage.get(id)) { getResult =>
            getResult shouldBe Some(SessionRow(id, None, version))
          }
      }
    }

    "create a session" in withStoredSessions(storedSessions) {
      val (id, sessionRow) = sessionNotStored
      val batchId = sessionRow.optBatchId.get

      whenReady(sessionsStorage.create(id)) { case Right(CreateSucceeded(createedVersion)) =>
        val expectedCreated = Some(SessionRow(id, None, createedVersion))
        whenReady(sessionsStorage.get(id)) {
          getResultAfterCreate =>
            getResultAfterCreate shouldBe expectedCreated
            whenReady(sessionsStorage.setBatchId(id, batchId, createedVersion)) {
              case Right(SetBatchIdSucceeded(newVersion)) =>
                val expectedStored = Some(SessionRow(id, Some(batchId), newVersion))
                whenReady(sessionsStorage.get(id)) {
                  getResultAfterSave =>
                    getResultAfterSave shouldBe expectedStored
                }
            }
        }
      }
    }

    "not allow to create existing session" in withStoredSessions(storedSessions) {
      val (id, existingSessionRow) = sessionCreated

      whenReady(sessionsStorage.create(id)) { createResult =>
        createResult shouldBe Left(CreateFailed())
        whenReady(sessionsStorage.get(id)) { resultAfter =>
          resultAfter shouldBe Some(existingSessionRow)
        }
      }
    }

    "not allow to setBatchId in a session not created" in withStoredSessions(storedSessions) {
      val (id, SessionRow(_, Some(batchId), version)) = sessionNotStored

      whenReady(sessionsStorage.get(id)) { resultBefore =>
        resultBefore shouldBe None
        whenReady(sessionsStorage.setBatchId(id, batchId, version)) {
          setBatchIdResult =>
            setBatchIdResult shouldBe Left(OptimisticLockFailed())
        }
      }
    }

    "not allow to setBatchId in a session with incorrect version" in
        withStoredSessions(storedSessions) {
      val (id, sessionRow@SessionRow(_, None, version)) = sessionCreated
      val incorrectVersion = version + 1
      val batchId = 8
      whenReady(sessionsStorage.get(id)) { getResultBefore =>
        getResultBefore shouldBe Some(sessionRow)
        whenReady(sessionsStorage.setBatchId(id, batchId, incorrectVersion)) {
          setBatchIdResult =>
            setBatchIdResult shouldBe Left(OptimisticLockFailed())
            whenReady(sessionsStorage.get(id)) {
              getResultAfter =>
                getResultAfter shouldBe Some(sessionRow)
            }
        }
      }
    }

    "not allow for batchId duplication by purging previous" in withStoredSessions(storedSessions) {
      val (deadId, deadSessionRow@SessionRow(_, Some(deadBatchId), _)) = session1
      val (newId, newSessionRow) = (idNotStored, deadSessionRow.copy(workflowId = idNotStored))
      whenReady(sessionsStorage.create(newId)) {
        case Right(CreateSucceeded(newVersion1)) =>
          whenReady(sessionsStorage.setBatchId(newId, deadBatchId, newVersion1)) {
            case Right(SetBatchIdSucceeded(newVersion2)) =>
              whenReady(sessionsStorage.get(deadId)) { getDeadResult =>
                getDeadResult shouldBe None
                whenReady(sessionsStorage.get(newId)) { getNewResult =>
                  getNewResult shouldBe Some(newSessionRow.copy(version = newVersion2))
                }
              }
          }
      }
    }

    "not get deleted session" in withStoredSessions(storedSessions) {
      val (id, row) = session1
      val expectedDeleteResult = Right(DeleteSucceeded())
      val expectedOptSessionRow = None
      whenReady(sessionsStorage.delete(id, row.version)) { deleteResult =>
        deleteResult shouldBe expectedDeleteResult
        whenReady(sessionsStorage.get(id)) { optSessionRow =>
          optSessionRow shouldBe expectedOptSessionRow
        }
      }
    }

    "delete a session" in withStoredSessions(storedSessions) {
      val (id, sessionRow) = session1
      val expectedGetBefore = Some(sessionRow)
      val expectedDeleteResult = Right(DeleteSucceeded())
      val expectedGetAfter = None
      whenReady(sessionsStorage.get(id)) {
        case resultBefore@Some(SessionRow(_, _, version)) =>
          resultBefore shouldBe expectedGetBefore
          whenReady(sessionsStorage.delete(id, version)) { deleteResult =>
            deleteResult shouldBe expectedDeleteResult
            whenReady(sessionsStorage.get(id)) { resultAfter =>
              resultAfter shouldBe expectedGetAfter
            }
          }
      }
    }

    "not delete a session if the version differs" in withStoredSessions(storedSessions) {
      val (id, sessionRow@SessionRow(_, _, version)) = session1
      val incorrectVersion = version + 1
      val expectedGetBefore = Some(sessionRow)
      val expectedDeleteResult = Left(OptimisticLockFailed())
      val expectedGetAfter = expectedGetBefore

      whenReady(sessionsStorage.get(id)) { resultBefore =>

        resultBefore shouldBe expectedGetBefore
        whenReady(sessionsStorage.delete(id, incorrectVersion)) { deleteResult =>
          deleteResult shouldBe expectedDeleteResult
          whenReady(sessionsStorage.get(id)) { resultAfter =>
            resultAfter shouldBe expectedGetAfter
          }
        }
      }
    }
  }

  private def withStoredSessions(storedSessions: Map[UUID, SessionRow])(testCode: => Any): Unit = {

    Await.ready(sessionsStorage.create(), operationDuration)

    val s = Future.sequence(storedSessions.map {
      case (_, sessionRow) => sessionsStorage.store(sessionRow)
    })
    Await.ready(s, operationDuration)

    try {
      testCode
    } finally {
      Await.ready(sessionsStorage.drop(), operationDuration)
    }
  }
}
