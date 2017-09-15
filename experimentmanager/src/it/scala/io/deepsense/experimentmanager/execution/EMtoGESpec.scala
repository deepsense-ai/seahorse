/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.execution

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestProbe}
import akka.util.Timeout
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}

import io.deepsense.commons.exception.FailureCode.NodeFailure
import io.deepsense.commons.exception.FailureDescription
import io.deepsense.deeplang.doperations.LoadDataFrame
import io.deepsense.experimentmanager.execution.RunningExperimentsActor.{GetStatus, Launch, Launched, Status}
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphexecutor.{HdfsIntegTestSupport, SimpleGraphExecutionIntegSuiteEntities}
import io.deepsense.models.experiments.Experiment

class EMtoGESpec
  extends HdfsIntegTestSupport
  with MockitoSugar
  with ScalaFutures
  with Eventually
  with IntegrationPatience {

  implicit var system: ActorSystem = _
  var runningExperimentsActorRef: TestActorRef[RunningExperimentsActor] = _
  var testProbe: TestProbe = _

  implicit val timeout: Timeout = 1.second

  implicit override val patienceConfig =
    PatienceConfig(timeout = scaled(Span(60, Seconds)), interval = scaled(Span(2000, Millis)))

  "ExperimentManager" should {
    "change status of experiment to COMPLETED after all nodes COMPLETED successfully" in {
      val experiment = createExperiment()

      testProbe.send(runningExperimentsActorRef, Launch(experiment))
      testProbe.expectMsgPF() {
        case Launched(exp) => exp.state == Experiment.State.running
      }

      stateOfExperiment(experiment) shouldBe Experiment.State.running
      waitTillExperimentFinishes(experiment)
      stateOfExperiment(experiment) shouldBe Experiment.State.completed
    }

    "change status of experiment to FAILED after some nodes FAILED" in {
      val experiment = createExperimentWithFailingGraph()

      testProbe.send(runningExperimentsActorRef, Launch(experiment))
      testProbe.expectMsgPF() {
        case Launched(exp) => exp.state == Experiment.State.running
      }

      stateOfExperiment(experiment) shouldBe Experiment.State.running
      waitTillExperimentFinishes(experiment)
      val state = stateOfExperiment(experiment)
      state.status shouldBe Experiment.Status.Failed
      val failureDescription: FailureDescription = state.error.get
      failureDescription.code shouldBe NodeFailure
      failureDescription.title shouldBe "Node Failure"
      failureDescription.message shouldBe None
      failureDescription.details shouldBe None
    }
  }

  def stateOfExperiment(experiment: Experiment): Experiment.State = {
    testProbe.send(runningExperimentsActorRef, GetStatus(experiment.id))
    val Status(Some(exp)) = testProbe.expectMsgType[Status]
    exp.state
  }

  def waitTillExperimentFinishes(experiment: Experiment): Unit = {
    def graphCompleted(status: Status): Boolean = {
      import io.deepsense.graph.Status._
      val inProgressStatuses = Set(Draft, Queued, Running)
      !status.experiment.get.graph.nodes.exists(n => inProgressStatuses.contains(n.state.status))
    }

    eventually {
      testProbe.send(runningExperimentsActorRef, GetStatus(experiment.id))
      val status = testProbe.expectMsgType[Status]
      graphCompleted(status) shouldBe true
    }
  }

  def createExperiment(): Experiment = {
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

  def createExperimentWithFailingGraph(): Experiment = {
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
    system = ActorSystem("test")
    runningExperimentsActorRef = TestActorRef(
      new RunningExperimentsActor(
        SimpleGraphExecutionIntegSuiteEntities.Name,
        3000,
        1000,
        15000))
    testProbe = TestProbe()
  }

  override def afterAll(): Unit = system.shutdown()

  override def requiredFiles: Map[String, String] =
    Map("/SimpleDataFrame" -> SimpleGraphExecutionIntegSuiteEntities.dataFrameLocation)
}
