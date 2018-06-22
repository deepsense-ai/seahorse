/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.sessionmanager.service.actors

import scala.concurrent.{ExecutionContext, Future}
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

import ai.deepsense.commons.models.Id
import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.sessionmanager.service._
import ai.deepsense.sessionmanager.service.actors.SessionServiceActor.{CreateRequest, GetRequest, KillRequest, ListRequest}
import ai.deepsense.sessionmanager.service.executor.SessionExecutorClients
import ai.deepsense.sessionmanager.service.sessionspawner.{ExecutorSession, SessionConfig, SessionSpawner}
import ai.deepsense.workflowexecutor.communication.message.global.Heartbeat
import ai.deepsense.workflowexecutor.rabbitmq.{ChannelSetupResult, MQCommunicationFactory}

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

  implicit val ec: ExecutionContext = _system.dispatcher

  implicit val patience = PatienceConfig(timeout = 5.seconds)

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  implicit val timeout: Timeout = 5.seconds

  "SessionServiceActor" when {
    "received a Heartbeat" when {
      "the session does not exist" should {
        "send a PoisonPill to sender" in fixture { p =>
          p.sessionServiceActor ! Heartbeat(notExistingWorkflowId.toString, None)
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
    val communicationFactory = mock[MQCommunicationFactory]
    when(communicationFactory.registerSubscriber(anyString(), anyObject()))
      .thenReturn(Future(ChannelSetupResult("", null)))
    val props = Props(new SessionServiceActor(
      sessionSpawner,
      sessionExecutorClients,
      communicationFactory
    ))
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
