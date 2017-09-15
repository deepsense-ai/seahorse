/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.execution

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Success, Try, Random}

import akka.actor.{ActorRef, ActorRefFactory, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Seconds, Span}

import io.deepsense.commons.exception.FailureCode.NodeFailure
import io.deepsense.commons.exception.FailureDescription
import io.deepsense.deeplang.doperations.LoadDataFrame
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphexecutor.clusterspawner.DefaultClusterSpawner
import io.deepsense.graphexecutor.{GraphExecutorClientActor, HdfsIntegTestSupport, SimpleGraphExecutionIntegSuiteEntities}
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.messages.{Update, Abort, Get, Launch}

class EMtoGESpec
  extends HdfsIntegTestSupport
  with MockitoSugar
  with ScalaFutures
  with Eventually
  with IntegrationPatience {

  implicit var system: ActorSystem = _
  var runningExperimentsActorRef: TestActorRef[RunningExperimentsActor] = _
  var testProbe: TestProbe = _

  // Used by eventually block
  implicit override val patienceConfig =
    PatienceConfig(timeout = scaled(Span(60, Seconds)), interval = scaled(Span(2, Seconds)))

  "ExperimentManager" should {
    "launch experiment and be told about COMPLETED status of experiment once all nodes COMPLETED" in {
      val experiment = oneNodeExperiment()

      testProbe.send(runningExperimentsActorRef, Launch(experiment))

      eventually {
        experimentById(experiment.id) shouldBe 'Running
        waitTillExperimentFinishes(experiment)
        experimentById(experiment.id) shouldBe 'Completed
      }
    }

    "launch experiment and be told about FAILED status of experiment after some nodes FAILED" in {
      val experiment = experimentWithFailingGraph()

      testProbe.send(runningExperimentsActorRef, Launch(experiment))

      eventually {
        waitTillExperimentFinishes(experiment)
        val state = stateOfExperiment(experiment)
        experimentById(experiment.id) shouldBe 'Failed
        val failureDescription: FailureDescription = state.error.get
        failureDescription.code shouldBe NodeFailure
        failureDescription.title shouldBe Experiment.failureMessage(experiment.id)
        failureDescription.message shouldBe None
        failureDescription.details shouldBe Map()
      }
    }

    "get experiment by id" in {
      val experiment = oneNodeExperiment()

      testProbe.send(runningExperimentsActorRef, Launch(experiment))
      eventually {
        experimentById(experiment.id) shouldBe 'Running
      }
      testProbe.send(runningExperimentsActorRef, Get(experiment.id))
      val Some(exp) = testProbe.expectMsgType[Option[Experiment]]
      exp.isRunning shouldBe true

      // FIXME Is it really needed? Can't we leave the experiment to die itself?
      eventually {
        experimentById(experiment.id) shouldBe 'Completed
      }
    }

    "abort running experiment" is pending
  }

  def stateOfExperiment(experiment: Experiment): Experiment.State = {
    testProbe.send(runningExperimentsActorRef, Get(experiment.id))
    val Some(exp) = testProbe.expectMsgType[Option[Experiment]]
    exp.state
  }

  def experimentById(id: Experiment.Id): Experiment = {
    testProbe.send(runningExperimentsActorRef, Get(id))
    val Some(exp) = testProbe.expectMsgType[Option[Experiment]]
    exp
  }

  def waitTillExperimentFinishes(experiment: Experiment): Unit = {
    def graphCompleted(experiment: Experiment): Boolean = {
      import io.deepsense.graph.Status._
      val inProgressStatuses = Set(Draft, Queued, Running)
      !experiment.graph.nodes.exists(n => inProgressStatuses.contains(n.state.status))
    }

    eventually {
      testProbe.send(runningExperimentsActorRef, Get(experiment.id))
      val Some(e) = testProbe.expectMsgType[Option[Experiment]]
      graphCompleted(e) shouldBe true
    }
  }

  def oneNodeExperiment(): Experiment = {
    val graph = Graph(
      Set(Node(
        Node.Id.randomId,
        LoadDataFrame(SimpleGraphExecutionIntegSuiteEntities.entityId.toString))))
    Experiment(
      Experiment.Id.randomId,
      SimpleGraphExecutionIntegSuiteEntities.entityTenantId,
      "name",
      graph)
  }

  def experimentWithFailingGraph(): Experiment = {
    val graph = Graph(
      Set(Node(Node.Id.randomId, LoadDataFrame("Invalid UUID for testing purposes"))))
    Experiment(
      Experiment.Id.randomId,
      "aTenantId",
      "name",
      graph)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()

    val config = ConfigFactory.load

    val actorSystemName = "EMtoGESpec"
    val akkaConfig = config.getConfig("deepsense")
    val actorName = "RunningExperimentsActor-" + getClass.getSimpleName

    system = ActorSystem(actorSystemName, akkaConfig)
    testProbe = TestProbe()
    runningExperimentsActorRef = TestActorRef(
      Props(new RunningExperimentsActor(SimpleGraphExecutionIntegSuiteEntities.Name,
        timeoutMillis = 3000L,
        DefaultClusterSpawner) with ProductionGraphExecutorClientFactory),
      actorName)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.shutdown()
  }

  override def requiredFiles: Map[String, String] =
    Map("/SimpleDataFrame" -> SimpleGraphExecutionIntegSuiteEntities.dataFrameLocation)
}
