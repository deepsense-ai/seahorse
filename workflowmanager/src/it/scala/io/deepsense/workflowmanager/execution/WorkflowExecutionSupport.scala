/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.execution

import scala.language.postfixOps

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestProbe}
import com.typesafe.config.ConfigFactory
import org.apache.hadoop.conf.Configuration
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Seconds, Span}

import io.deepsense.commons.config.{ConfigFactoryExt, ConfigurationMerger}
import io.deepsense.deeplang.DOperation
import io.deepsense.graph.Node
import io.deepsense.graphexecutor.HdfsIntegTestSupport
import io.deepsense.graphexecutor.clusterspawner.DefaultClusterSpawner
import io.deepsense.models.messages.Get
import io.deepsense.models.workflows.Workflow

abstract class WorkflowExecutionSupport
  extends HdfsIntegTestSupport
  with MockitoSugar
  with ScalaFutures
  with Eventually
  with IntegrationPatience {

  protected def executionTimeLimitSeconds: Long

  protected def esFactoryName: String

  implicit var system: ActorSystem = _
  var runningExperimentsActorRef: TestActorRef[RunningWorkflowsActor] = _
  var testProbe: TestProbe = _


  override protected def enhanceConfiguration(config: Configuration): Unit = {
    super.enhanceConfiguration(config)
    val integrationTestConfig = ConfigFactory.load
    ConfigurationMerger.merge(config, integrationTestConfig.getConfig("hadoop"))
  }

  // Used by eventually block
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(executionTimeLimitSeconds, Seconds)),
    interval = scaled(Span(2, Seconds)))

  def stateOfExperiment(experiment: Workflow): Workflow.State = {
    testProbe.send(runningExperimentsActorRef, Get(experiment.id))
    val Some(exp) = testProbe.expectMsgType[Option[Workflow]]
    exp.state
  }

  def experimentById(id: Workflow.Id): Workflow = {
    testProbe.send(runningExperimentsActorRef, Get(id))
    val Some(exp) = testProbe.expectMsgType[Option[Workflow]]
    exp
  }

  def waitTillExperimentFinishes(experiment: Workflow): Unit = {
    def graphCompleted(experiment: Workflow): Boolean = {
      import io.deepsense.graph.Status._
      val inProgressStatuses = Set(Draft, Queued, Running)
      !experiment.graph.nodes.exists(n => inProgressStatuses.contains(n.state.status))
    }

    eventually {
      testProbe.send(runningExperimentsActorRef, Get(experiment.id))
      val Some(e) = testProbe.expectMsgType[Option[Workflow]]
      graphCompleted(e) shouldBe true
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()

    ConfigFactoryExt.enableEnvOverride()
    ConfigFactory.invalidateCaches()
    val config = ConfigFactory.load
    val actorSystemName = "EMtoGESpec"
    val akkaConfig = config.getConfig("deepsense")
    val actorName = "RunningExperimentsActor-" + getClass.getSimpleName

    system = ActorSystem(actorSystemName, akkaConfig)
    testProbe = TestProbe()
    runningExperimentsActorRef = TestActorRef(
      Props(new RunningWorkflowsActor(esFactoryName,
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
