/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import scala.concurrent.duration._
import scala.util.Success

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit._
import org.apache.hadoop.yarn.api.records.ApplicationId
import org.apache.hadoop.yarn.client.api.YarnClient
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest._
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Seconds, Span}

import io.deepsense.graphexecutor.clusterspawner.ClusterSpawner
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.messages.{Abort, ExecutorReady, Launch, Update}

class GraphExecutorClientSpec(actorSystem: ActorSystem)
  extends TestKit(actorSystem)
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with MockitoSugar
  with Eventually {

  def this() = this(ActorSystem("GraphExecutorClientSpec"))

  val experimentId = Experiment.Id.randomId
  val mockedExperiment = mock[Experiment]
  when(mockedExperiment.id).thenReturn(experimentId)
  when(mockedExperiment.state).thenReturn(Experiment.State.draft)

  "GECA" when {
    "received Launch" should {
      "spawn GE" when {
        "GE was not spawned yet" in {
          val spawner = succeedingSpawner()
          val gecActor = createTestGEC(spawner)
          val runningExperiments = TestProbe()
          launchAndWaitForSpawn(mockedExperiment, spawner, gecActor, runningExperiments)
        }
      }
    }
    "received Abort" should {
      "reply with an aborted experiment" when {
        "GE was requested to spawn but not spawned yet" in {
          val abortedExperiment = mock[Experiment]
          when(mockedExperiment.markAborted).thenReturn(abortedExperiment)
          val watcher = TestProbe()
          val yarnClient = mock[YarnClient]
          val spawner = slowSpawner(yarnClient)
          val gecActor = createTestGEC(spawner)
          watcher.watch(gecActor)
          val runningExperiments = TestProbe()
          launchAndWaitForSpawn(mockedExperiment, spawner, gecActor, runningExperiments)
          runningExperiments.send(gecActor, Abort(mockedExperiment.id))
          runningExperiments.expectMsg(Update(abortedExperiment))
          eventually(Timeout(Span(8, Seconds))) {
            verify(yarnClient, times(1)).close()
            watcher.expectTerminated(gecActor, 1.second)
          }
        }
        "GE was spawned but is not ready yet" in {
          val abortedExperiment = mock[Experiment]
          when(mockedExperiment.markAborted).thenReturn(abortedExperiment)
          val watcher = TestProbe()
          val yarnClient = mock[YarnClient]
          val spawner = succeedingSpawner(yarnClient)
          val gecActor = createTestGEC(spawner)
          watcher.watch(gecActor)
          val runningExperiments = TestProbe()
          launchAndWaitForSpawn(mockedExperiment, spawner, gecActor, runningExperiments)
          runningExperiments.send(gecActor, Abort(mockedExperiment.id))
          runningExperiments.expectMsg(Update(abortedExperiment))
          val ge = TestProbe()
          ge.send(gecActor, ExecutorReady(mockedExperiment.id))
          ge.expectNoMsg()
          eventually(Timeout(Span(8, Seconds))) {
            verify(yarnClient, times(1)).close()
            watcher.expectTerminated(gecActor, 1.second)
          }
        }
      }
      "abort GE and wait for the last update" when {
        "GE is running" in {
          val abortedExperiment = mockExperimentWithId(mockedExperiment.id)
          val spawner = succeedingSpawner()
          val gecActor = createTestGEC(spawner)
          val runningExperiments = TestProbe()
          val ge = TestProbe()
          launchAndWaitForSpawn(mockedExperiment, spawner, gecActor, runningExperiments)
          signalExecutorReady(gecActor, ge)
          runningExperiments.send(gecActor, Abort(mockedExperiment.id))
          ge.expectMsg(Abort(mockedExperiment.id))
          ge.reply(Update(abortedExperiment))
          runningExperiments.expectMsg(Update(abortedExperiment))
        }
      }
    }
    "spawn of GE succeeded" should {
      "await ExecutorReady message and send Launch afterwards" in {
        val spawner = succeedingSpawner()
        val gecActor = createTestGEC(spawner)
        val runningExperiments = TestProbe()
        launchAndWaitForSpawn(mockedExperiment, spawner, gecActor, runningExperiments)
        val ge = TestProbe()
        signalExecutorReady(gecActor, ge)
      }
    }
    "spawn of GE failed" should {
      "return with a failed experiment" in {
        val gec = createTestGEC(failingSpawner)
        val failedExperiment = mock[Experiment]
        val runningExperiments = TestProbe()
        when(mockedExperiment.markFailed(any())).thenReturn(failedExperiment)
        runningExperiments.send(gec, Launch(mockedExperiment))
        runningExperiments.expectMsg(Update(failedExperiment))
      }
    }
    "received Update from GE" should {
      "forward it to REA" in {
        val spawner = succeedingSpawner()
        val gecActor = createTestGEC(spawner)
        val runningExperiments = TestProbe()
        val ge = TestProbe()

        launchAndWaitForSpawn(mockedExperiment, spawner, gecActor, runningExperiments)
        signalExecutorReady(gecActor, ge)
        val updates = List.fill(3)(Update(mockExperimentWithId(mockedExperiment.id)))

        updates.foreach(u => ge.send(gecActor, u))
        runningExperiments.expectMsgAllOf(updates: _*)
      }
    }
  }

  def signalExecutorReady(gecActor: ActorRef, ge: TestProbe): Unit = {
    eventually {
      ge.send(gecActor, ExecutorReady(mockedExperiment.id))
      ge.expectMsg(1.millisecond, Launch(mockedExperiment))
    }
  }

  def launchAndWaitForSpawn(
      experiment: Experiment,
      spawner: ClusterSpawner,
      gecActor: ActorRef,
      runningExperiments: TestProbe): Registration = {
    runningExperiments.send(gecActor, Launch(experiment))
    verifySpawnerCalled(spawner, experiment)
  }

  def verifySpawnerCalled(spawner: ClusterSpawner, experiment: Experiment): Registration = {
    eventually {
      verify(spawner, times(1))
        .spawnOnCluster(org.mockito.Matchers.eq(experiment.id), any(), any())
    }
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  def mockExperimentWithId(id: Experiment.Id): Experiment = {
    val experiment = mock[Experiment]
    when(experiment.id).thenReturn(id)
    experiment
  }

  def createTestGEC(spawner: ClusterSpawner): ActorRef = {
    actorSystem.actorOf(
      Props(classOf[GraphExecutorClientActor], "not-used-in-mock", spawner))
  }

  def failingSpawner: ClusterSpawner = {
    val spawner = mock[ClusterSpawner]
    when(spawner.spawnOnCluster(any(), any(), any()))
      .thenThrow(new IllegalStateException("This mock always fails when spawning GE"))
    spawner
  }

  def succeedingSpawner(yarnClient: YarnClient = mock[YarnClient]): ClusterSpawner = {
    val spawner = mock[ClusterSpawner]
    when(spawner.spawnOnCluster(any(), any(), any()))
      .thenReturn(Success(yarnClient, mock[ApplicationId]))
    spawner
  }

  def slowSpawner(yarnClient: YarnClient = mock[YarnClient]): ClusterSpawner = {
    val spawner = mock[ClusterSpawner]
    when(spawner.spawnOnCluster(any(), any(), any()))
      .thenAnswer(new Answer[Success[(YarnClient, ApplicationId)]]{
      override def answer(invocation: InvocationOnMock): Success[(YarnClient, ApplicationId)] = {
        Thread.sleep(4000)
        Success(yarnClient, mock[ApplicationId])
      }
    })
    spawner
  }
}
