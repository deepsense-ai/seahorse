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
import io.deepsense.graphexecutor.{GraphExecutorClientActor, HdfsIntegTestSupport, SimpleGraphExecutionIntegSuiteEntities}
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.messages.{Abort, Get, Launch}

class EMtoGESpec
  extends HdfsIntegTestSupport
  with MockitoSugar
  with ScalaFutures
  with Eventually
  with IntegrationPatience {

  implicit var system: ActorSystem = _
  var runningExperimentsActorRef: TestActorRef[RunningExperimentsActor] = _
  var testProbe: TestProbe = _

  implicit val timeout: FiniteDuration = 2.minutes

  implicit override val patienceConfig =
    PatienceConfig(timeout = scaled(Span(60, Seconds)), interval = scaled(Span(2, Seconds)))

  "ExperimentManager" should {
    "launch experiment and be told about COMPLETED status of experiment once all nodes COMPLETED" in {
      val experiment = oneNodeExperiment()

      testProbe.send(runningExperimentsActorRef, Launch(experiment))

      eventually {
        stateOfExperiment(experiment) shouldBe Experiment.State.running
        waitTillExperimentFinishes(experiment)
        stateOfExperiment(experiment) shouldBe Experiment.State.completed
      }
    }

    "launch experiment and be told about FAILED status of experiment after some nodes FAILED" in {
      val experiment = experimentWithFailingGraph()

      testProbe.send(runningExperimentsActorRef, Launch(experiment))

      eventually {
        waitTillExperimentFinishes(experiment)

        val errorMsg = "java.lang.IllegalArgumentException: Invalid UUID string: Invalid UUID for testing purposes"

        val state = stateOfExperiment(experiment)
        state.status shouldBe Experiment.Status.Failed
        val failureDescription: FailureDescription = state.error.get
        failureDescription.code shouldBe NodeFailure
        failureDescription.title shouldBe "Node Failure"
        failureDescription.message shouldBe None
        failureDescription.details shouldBe None
      }
    }

    "get experiment by id" in {
      val experiment = oneNodeExperiment()

      testProbe.send(runningExperimentsActorRef, Launch(experiment))
      eventually {
        stateOfExperiment(experiment) shouldBe Experiment.State.running
      }
      testProbe.send(runningExperimentsActorRef, Get(experiment.id))
      val Some(exp) = testProbe.expectMsgType[Option[Experiment]](1.minute)
      exp.isRunning shouldBe true

      // FIXME Is it really needed? Can't we leave the experiment to die itself?
      eventually {
        waitTillExperimentFinishes(experiment)
        stateOfExperiment(experiment) shouldBe Experiment.State.completed
      }
    }

    "abort running experiment" in {
      val experiment = oneNodeExperiment()

      testProbe.send(runningExperimentsActorRef, Launch(experiment))
      eventually {
        stateOfExperiment(experiment) shouldBe Experiment.State.running
      }
      testProbe.send(runningExperimentsActorRef, Abort(experiment.id))
      eventually {
        val Success(exp) = testProbe.expectMsgType[Try[Experiment]]
        exp.isAborted shouldBe true
      }
    }
  }

  def stateOfExperiment(experiment: Experiment): Experiment.State = {
    testProbe.send(runningExperimentsActorRef, Get(experiment.id))
    val Some(exp) = testProbe.expectMsgType[Option[Experiment]](1.minute)
    exp.state
  }

  def waitTillExperimentFinishes(experiment: Experiment): Unit = {
    def graphCompleted(experiment: Experiment): Boolean = {
      import io.deepsense.graph.Status._
      val inProgressStatuses = Set(Draft, Queued, Running)
      !experiment.graph.nodes.exists(n => inProgressStatuses.contains(n.state.status))
    }

    eventually {
      testProbe.send(runningExperimentsActorRef, Get(experiment.id))
      val Some(e) = testProbe.expectMsgType[Option[Experiment]](1.minute)
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

    val actorSystemName = "EMtoGESpec"
    val host = "172.28.128.1"
    val port = 10000 + Random.nextInt(100)
    val actorName = "RunningExperimentsActor"

    import scala.collection.JavaConverters._
    system = ActorSystem(
      actorSystemName,
      ConfigFactory.parseMap(
        Map(
          "akka.actor.provider" -> "akka.remote.RemoteActorRefProvider",
          "akka.remote.netty.tcp.hostname" -> host,
          "akka.remote.netty.tcp.port" -> port.toString
        ).asJava
      )
    )
    val rePath = s"akka.tcp://$actorSystemName@$host:$port/user/$actorName"
    testProbe = TestProbe()
    val gecMaker: (ActorRefFactory, String, String, String) => ActorRef = {
      (f, entitystorageLabel, parentRemoteActorPath, experimentId) =>
        f.actorOf(
          Props(new GraphExecutorClientActor(entitystorageLabel, parentRemoteActorPath)),
          experimentId)
    }
    runningExperimentsActorRef = TestActorRef(
      Props(classOf[RunningExperimentsActor],
        SimpleGraphExecutionIntegSuiteEntities.Name,
        3000L,
        rePath,
        gecMaker),
      actorName)
  }

  override def afterAll(): Unit = system.shutdown()

  override def requiredFiles: Map[String, String] =
    Map("/SimpleDataFrame" -> SimpleGraphExecutionIntegSuiteEntities.dataFrameLocation)
}
