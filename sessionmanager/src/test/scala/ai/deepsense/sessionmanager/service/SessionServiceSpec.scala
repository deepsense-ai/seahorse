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

package ai.deepsense.sessionmanager.service

import java.time.Instant
import java.util.UUID

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.apache.spark.launcher.SparkAppHandle.State
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.util.Success

import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.models.workflows.Workflow
import ai.deepsense.sessionmanager.service.Status.Status
import ai.deepsense.sessionmanager.service.actors.SessionServiceActor
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.outputintercepting.OutputInterceptorHandle
import ai.deepsense.sessionmanager.service.sessionspawner.{ExecutorSession, SessionConfig, StateInferencer}

class SessionServiceSpec extends WordSpec with Matchers with MockitoSugar {
  import SessionServiceActor._
  import scala.concurrent.ExecutionContext.Implicits.global
  private val as: ActorSystem = ActorSystem.create("SessionServiceSpec")
  private val timeout: Duration = 5.seconds
  private def generateSessionServiceActor(initialSessions: Seq[CreateRequest]) =
    as.actorOf(Props[SessionServiceActorMockImpl](new SessionServiceActorMockImpl(initialSessions)))

  private val adminId = "5af6f186-521e-41d1-beab-f02a39a8fbbc"
  val clusterDetails = ClusterDetails(Option(2L), "Cluster", "yarn", "10.10.10.10", "12.12.12.12")
  val workflowIds = (1 to 4).map(_ => UUID.randomUUID())
  val userIds = (1 to 2).map (_ => UUID.randomUUID().toString)
  private val sessions = Seq(
    CreateRequest(SessionConfig(workflowIds(0), userIds(0)), clusterDetails),
    CreateRequest(SessionConfig(workflowIds(1), userIds(0)), clusterDetails),
    CreateRequest(SessionConfig(workflowIds(2), userIds(1)), clusterDetails),
    CreateRequest(SessionConfig(workflowIds(3), userIds(1)), clusterDetails)
  )

  private def test(initialSessions: Seq[CreateRequest])
    (testBody: SessionService => Unit) = {
    val sessionService = new SessionService(
      generateSessionServiceActor(initialSessions), 1000, adminId)
    testBody(sessionService)
  }


  "Session service" when {
    "asked for list of sessions" should {
      "reply with all sessions to admin" in {
        test(sessions) { sessionService =>
          val receivedSessions = Await.result(sessionService.listSessions(adminId), timeout)
          receivedSessions.map(_.workflowId.toString).toSet shouldEqual
            workflowIds.map(_.toString).toSet
        }
      }
      "reply with their sessions to common users" in {
        test(sessions) { sessionService =>
          {
            val receivedSessions = Await.result(sessionService.listSessions(userIds(0)), timeout)
            receivedSessions.map(_.workflowId.toString).toSet shouldEqual
              Set(workflowIds(0), workflowIds(1)).map(_.toString)
          }
          {
            val receivedSessions = Await.result(sessionService.listSessions(userIds(1)), timeout)
            receivedSessions.map(_.workflowId.toString).toSet shouldEqual
              Set(workflowIds(2), workflowIds(3)).map(_.toString)
          }
        }
      }
    }
    "asked for a session" should {
      "give admin someone else's session" in {
        test(sessions) { sessionService =>
          val session =
            Await.result(sessionService.getSession(adminId, workflowIds(0)).run, timeout)
          session should not be empty
        }
      }
      "give user their session" in {
        test(sessions) { sessionService =>
          val session =
            Await.result(sessionService.getSession(userIds(0), workflowIds(0)).run, timeout)
          session should not be empty
        }
      }
      "not give common user someone else's session" in {
        test(sessions) { sessionService =>
          an[UnauthorizedOperationException] shouldBe thrownBy(
            Await.result(sessionService.getSession(userIds(1), workflowIds(0)).run, timeout))
        }
      }
    }
    "asked to kill a session" should {
      "kill it if asked by admin" in {
        test(sessions) { sessionService =>
          Await.result(sessionService.killSession(adminId, workflowIds(0)).run, timeout)
          val session =
            Await.result(sessionService.getSession(adminId, workflowIds(0)).run, timeout)
          session shouldBe empty
        }
      }
      "kill it if asked by owner" in {
        test(sessions) { sessionService =>
          Await.result(sessionService.killSession(userIds(0), workflowIds(0)).run, timeout)
          val session =
            Await.result(sessionService.getSession(adminId, workflowIds(0)).run, timeout)
          session shouldBe empty
        }
      }
      "not kill it if asked by common non-owner user" in {
        test(sessions) { sessionService =>
          an[UnauthorizedOperationException] shouldBe thrownBy(
            Await.result(sessionService.killSession(userIds(1), workflowIds(0)).run, timeout))
          val session =
            Await.result(sessionService.getSession(adminId, workflowIds(0)).run, timeout)
          session should not be empty
        }
      }
    }
  }

  class SessionServiceActorMockImpl(initialSessions: Seq[CreateRequest])
    extends Actor {
    private var sessions: Map[Workflow.Id, ExecutorSession] =
      initialSessions.map(
        request => request.sessionConfig.workflowId -> sessionFromRequest(request)).toMap

    override def receive: Receive = {
      case request@CreateRequest(sessionConfig, _) =>
        val session = sessions.getOrElse(sessionConfig.workflowId, sessionFromRequest(request))
        sessions = sessions + (sessionConfig.workflowId -> session)
        sender() ! session.sessionForApi()
      case GetRequest(id) =>
        sender() ! sessions.get(id)
      case KillRequest(id) =>
        sessions = sessions - id
        sender() ! (())
      case LaunchRequest(id) =>
        sender() ! Success(())
      case ListRequest() =>
        sender() ! sessions.values.toList
    }
    def sessionFromRequest(request: CreateRequest): ExecutorSession =
      ExecutorSession(
        request.sessionConfig,
        request.clusterConfig,
        None,
        mock[StateInferencer],
        mock[OutputInterceptorHandle])
  }
}
