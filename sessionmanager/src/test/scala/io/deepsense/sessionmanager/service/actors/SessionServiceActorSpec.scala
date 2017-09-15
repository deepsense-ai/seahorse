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
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import io.deepsense.commons.models.Id
import io.deepsense.commons.models.ClusterDetails
import io.deepsense.sessionmanager.service._
import io.deepsense.sessionmanager.service.actors.SessionServiceActor.{CreateRequest, GetRequest, KillRequest, ListRequest}
import io.deepsense.sessionmanager.service.executor.SessionExecutorClients
import io.deepsense.sessionmanager.service.sessionspawner.{SessionConfig, SessionSpawner}
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

  import TestData._

  def this() = this(ActorSystem("SessionServiceActorSpec"))

  implicit val patience = PatienceConfig(timeout = 5.seconds)

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  implicit val timeout: Timeout = 5.seconds

  "SessionServiceActor" when {
    "received a Heartbeat" when {
      "the session does not exist" should {
        "send a PoisonPill to sender" in fixture { p =>
          p.sessionServiceActor ! Heartbeat(notExistingWorkflowId.toString)
          eventually {
            verify(p.sessionExecutorClients, times(1)).sendPoisonPill(notExistingWorkflowId)
          }
        }
      }
    }

    "received a CreateRequest" when {
      "the session does not exist" should {

        "use session spawner to create a session" in fixture { p =>
          val userId = Id.randomId.toString
          sendCreateRequest(p, notExistingWorkflowId, userId)
          eventually {
            verify(p.sessionSpawner, times(1))
              .createSession(notExistingWorkflowSessionConfig, someClusterDetails)
          }
        }

      }
    }

  }

  private def sendGetRequest(p: TestParams, workflowId: Id): Future[Option[Session]] = {
    (p.sessionServiceActor ? GetRequest(workflowId))
      .mapTo[Option[Session]]
  }

  private def sendCreateRequest(p: TestParams, workflowId: Id, userId: String): Future[Id] = {
    val sessionConfig = SessionConfig(workflowId, someUserId)
    (p.sessionServiceActor ? CreateRequest(sessionConfig, someClusterDetails)).mapTo[Id]
  }

  private lazy val notExistingWorkflowSessionConfig = SessionConfig(
    notExistingWorkflowId, someUserId
  )
  private lazy val someUserId = Id.randomId.toString()

  private lazy val existingWorkflowId = Id.randomId
  private lazy val notExistingWorkflowId = Id.randomId

  private def fixture[T](test: (TestParams) => T): T = {
    val sessionExecutorClients = mock[SessionExecutorClients]
    val sessionSpawner = mock[SessionSpawner]
    val props = Props(new SessionServiceActor(
      sessionSpawner,
      sessionExecutorClients))
    val params = TestParams(
      system.actorOf(props),
      sessionSpawner,
      sessionExecutorClients)
    test(params)
  }

  case class TestParams(
    sessionServiceActor: ActorRef,
    sessionSpawner: SessionSpawner,
    sessionExecutorClients: SessionExecutorClients
  )
}
