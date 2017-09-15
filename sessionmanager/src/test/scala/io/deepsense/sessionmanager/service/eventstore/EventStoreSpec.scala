/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.eventstore

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import io.deepsense.commons.models.Id
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.commons.models.ClusterDetails
import io.deepsense.sessionmanager.service.{TestData, EventStore}
import io.deepsense.sessionmanager.service.EventStore._

abstract class EventStoreSpec extends StandardSpec with UnitTestSupport {

  "EventStore" when {

    "recorded no events" should {
      "return no events" in {
        whenReady(eventStoreNoEvents().getLastEvents) {
          _ shouldBe empty
        }
      }
    }

    "recorded a Heartbeat event" when {
      val store = eventStore()
      val (lastHeartbeat, lastStarted) = lastEvents(store)
      "there was a Started event" should {
        val fixture = recordHeartbeatAndGetLastEvent(store, lastStarted, startedId) _

        "record the event" in {
          testHeartbeatRecorded(fixture)
        }
        "return no event" in {
          testReturnNoError(fixture)
        }
      }
      "there was a Heartbeat event" should {
        val fixture = recordHeartbeatAndGetLastEvent(store, lastHeartbeat, heartbeatId) _
        "record the event" in {
          testHeartbeatRecorded(fixture)
        }
        "return no event" in {
          testReturnNoError(fixture)
        }
      }
      "there was no event" should {
        val fixture = recordHeartbeatAndGetLastEvent(
          eventStoreNoEvents(),
          Future.successful(None),
          Id.randomId) _
        "not record the event" in {
          testNoEventRecorded(fixture)
        }
        "return InvalidWorkflowId" in {
          fixture {
            case (_, result, _) =>
              result shouldBe Left(InvalidWorkflowId())
          }
        }
      }
    }

    "recorded a Started event" when {
      "there was a Started event" should {
        def fixture: Fixture[Either[SessionExists, Unit]] = {
          val store = eventStore()
          val cluster = TestData.someClusterDetails
          val (_, lastStarted) = lastEvents(store)
          recordStartedAndGetLastEvent(store, lastStarted, startedId, cluster) _
        }

        "not record the event" in {
          testNoEventRecorded(fixture)
        }
        "return that the session exists" in {
          testReturnExists(fixture)
        }
      }

      "there was a Heartbeat event" should {
        def fixture: Fixture[Either[SessionExists, Unit]] = {
          val store = eventStore()
          val cluster = TestData.someClusterDetails
          val (lastHeartbeat, _) = lastEvents(store)
          recordStartedAndGetLastEvent(store, lastHeartbeat, heartbeatId, cluster) _
        }
        "record the event" in {
          testHeartbeatRecorded(fixture)
        }
        "return that the session exists" in {
          testReturnExists(fixture)
        }
      }

      "there was no event" should {
        val someId = Id.randomId
        val cluster = TestData.someClusterDetails
        def fixture: Fixture[Either[SessionExists, Unit]] =
          recordStartedAndGetLastEvent(
            eventStoreNoEvents(),
            Future.successful(None),
            someId,
            cluster) _
        "record the event" in {
          fixture {
            case (_, _, updatedEvent) =>
              updatedEvent shouldBe a[Some[_]]
              updatedEvent.get shouldBe a[Started]
              updatedEvent.get.workflowId shouldBe someId
          }
        }
        "return no event" in {
          testReturnNoError(fixture)
        }
      }
    }

    "recorded Killed event" when {
      "there was a Started event" should {
        "delete the Started event" in {
          val store = eventStore()
          val (_, lastStarted) = lastEvents(store)
          val fixture = recordKilledAndGetLastEvent(store, lastStarted, startedId) _
          testKilledRecorded(fixture)
        }
      }
      "there was a Heartbeat event" should {
        "delete the Heartbeat event" in {
          val store = eventStore()
          val (lastHeartbeat, _) = lastEvents(store)
          val fixture = recordKilledAndGetLastEvent(store, lastHeartbeat, heartbeatId) _
          testKilledRecorded(fixture)
        }
      }
    }
  }

  protected lazy val heartbeatId = Id.randomId
  protected lazy val startedId = Id.randomId

  protected def eventStoreNoEvents(): EventStore
  private def eventStore(): EventStore = {
    val store = eventStoreNoEvents()
    val cluster = TestData.someClusterDetails
    val eventuallySavedEvents = for {
      _ <- store.started(startedId, cluster)
      _ <- store.started(heartbeatId, cluster)
      _ <- store.heartbeat(heartbeatId)
    } yield store

    Await.result(eventuallySavedEvents, 2.seconds)
  }

  private def lastEvents(
      store: EventStore): (Future[Option[HeartbeatReceived]], Future[Option[Started]]) = {
    def getLastEvent[T](workflowId: Id): Future[Option[T]] = {
      store.getLastEvent(workflowId).map(_.map(_.asInstanceOf[T]))
    }
    val heartbeat = getLastEvent[HeartbeatReceived](heartbeatId)
    val started = getLastEvent[Started](startedId)
    (heartbeat, started)
  }

  private type RecordStartedAndGet =
  ((Option[Event], Either[InvalidWorkflowId, Unit], Option[Event]) => Any) => Unit

  private def recordStartedAndGetLastEvent(
    store: EventStore,
    previousEvent: Future[Option[Event]],
    workflowId: Id,
    clusterDetails: ClusterDetails)(
    tests: (Option[Event], Either[SessionExists, Unit], Option[Event]) => Any): Unit = {
    whenReady(previousEvent) { previousEvent =>
      whenReady(store.started(workflowId, clusterDetails)) { startedResult =>
        whenReady(store.getLastEvent(workflowId)) {
          updatedEvent =>
            tests(previousEvent, startedResult, updatedEvent)
        }
      }
    }
  }

  private type RecordHeartbeatAndGet =
  ((Option[Event], Either[InvalidWorkflowId, Unit], Option[Event]) => Any) => Unit

  private def recordHeartbeatAndGetLastEvent(
      store: EventStore,
      previousEvent: Future[Option[Event]],
      workflowId: Id)(
    tests: (Option[Event], Either[InvalidWorkflowId, Unit], Option[Event]) => Any): Unit = {
    whenReady(previousEvent) { previousEvent =>
      whenReady(store.heartbeat(workflowId)) { heartbeatResult =>
        whenReady(store.getLastEvent(workflowId)) {
          updatedEvent =>
            tests(previousEvent, heartbeatResult, updatedEvent)
        }
      }
    }
  }

  private type Fixture[T] =
  ((Option[Event], T, Option[Event]) => Any) => Unit

  private def testHeartbeatRecorded[T](fixture: Fixture[T]): Unit = {
    fixture {
      case (previousEvent, _, currentEvent) =>
        currentEvent shouldBe a[Some[_]]
        val event = currentEvent.get
        event shouldBe a[HeartbeatReceived]
        previousEvent.foreach {
          _.happenedAt.isBefore(event.happenedAt)
        }
    }
  }

  private def testNoEventRecorded[T](fixture: Fixture[T]): Unit = {
    fixture {
      case (previousEvent, _, updatedEvent) =>
        updatedEvent shouldBe previousEvent
    }
  }

  private def testReturnNoError[T](fixture: Fixture[T]): Unit = {
    fixture {
      case (_, result, _) =>
        result shouldBe Right(())
    }
  }

  private def testReturnExists[T](fixture: Fixture[T]): Unit = {
    fixture {
      case (previousEvent, result, _) =>
        result shouldBe Left(SessionExists())
    }
  }

  private def recordKilledAndGetLastEvent(
    store: EventStore,
    previousEvent: Future[Option[Event]],
    workflowId: Id)(
    tests: (Option[Event], Unit, Option[Event]) => Any): Unit = {
    whenReady(previousEvent) { previousEvent =>
      whenReady(store.killed(workflowId)) { killedResult =>
        whenReady(store.getLastEvent(workflowId)) {
          updatedEvent =>
            tests(previousEvent, killedResult, updatedEvent)
        }
      }
    }
  }

  private def testKilledRecorded[T](fixture: Fixture[T]): Unit = {
    fixture {
      case (_, _, currentEvent) => currentEvent shouldBe None
    }
  }
}
