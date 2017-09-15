/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.execution

import scala.language.postfixOps

import akka.actor.{ActorRef, ActorRefFactory, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Seconds, Span}

import io.deepsense.deeplang.DOperation
import io.deepsense.graph.Node
import io.deepsense.graphexecutor.clusterspawner.DefaultClusterSpawner
import io.deepsense.graphexecutor.{GraphExecutorClientActor, HdfsIntegTestSupport}
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.messages.Get

abstract class ExperimentExecutionSupport
  extends HdfsIntegTestSupport
  with MockitoSugar
  with ScalaFutures
  with Eventually
  with IntegrationPatience {

  protected def executionTimeLimitSeconds: Long

  protected def esFactoryName: String

  implicit var system: ActorSystem = _
  var runningExperimentsActorRef: TestActorRef[RunningExperimentsActor] = _
  var testProbe: TestProbe = _

  // Used by eventually block
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(executionTimeLimitSeconds, Seconds)),
    interval = scaled(Span(2, Seconds)))

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

  override def beforeAll(): Unit = {
    super.beforeAll()

    val config = ConfigFactory.load

    val actorSystemName = "EMtoGESpec"
    val akkaConfig = config.getConfig("deepsense")
    val actorName = "RunningExperimentsActor-" + getClass.getSimpleName

    system = ActorSystem(actorSystemName, akkaConfig)
    testProbe = TestProbe()
    runningExperimentsActorRef = TestActorRef(
      Props(new RunningExperimentsActor(esFactoryName,
        timeoutMillis = 3000L,
        DefaultClusterSpawner) with ProductionGraphExecutorClientFactory),
      actorName)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.shutdown()
  }

  protected def node(operation: DOperation): Node = Node(Node.Id.randomId, operation)
}
