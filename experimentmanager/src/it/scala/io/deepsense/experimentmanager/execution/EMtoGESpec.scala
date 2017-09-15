/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.experimentmanager.execution

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.control.Breaks._

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestProbe}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang.doperations.LoadDataFrame
import io.deepsense.experimentmanager.execution.RunningExperimentsActor.{Launched, GetStatus, Launch, Status}
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphexecutor.{HdfsIntegTestSupport, SimpleGraphExecutionIntegSuiteEntities}
import io.deepsense.models.experiments.Experiment

class EMtoGESpec extends HdfsIntegTestSupport
    with MockitoSugar
    with ScalaFutures
    with LazyLogging {

  implicit var system: ActorSystem = _
  var actorRef: TestActorRef[RunningExperimentsActor] = _
  var testProbe: TestProbe = _

  implicit val timeout: Timeout = 1.second

  // Timeout for test is 1 minute = 30 * 2000ms
  val GetStatusInterval = 2000
  val MaxRetryNumber = 30

  "ExperimentManager" should "launch graph on GraphExecutor" in {
    testProbe.send(actorRef, Launch(experiment))
    testProbe.expectMsgPF() {
      case Launched(exp) => exp.state == Experiment.State.running
    }

    var success = false
    breakable {
      for (i <- 0 until MaxRetryNumber) {
        testProbe.send(actorRef, GetStatus(experiment.id))
        val status = testProbe.expectMsgType[Status]
        logger.debug(s"Received status: $status")
        forAll(status.experiment.get.graph.nodes) { node =>
          import io.deepsense.graph.Status.Failed
          node.state.status shouldNot be (Failed)
        }
        if (graphCompleted(status)) {
          success = true
          break()
        }
        Thread.sleep(GetStatusInterval)
      }
    }
    if (!success) {
      fail(s"Max retry: $MaxRetryNumber and experiment did not run. Communication FAILED!!!")
    }
  }

  def graphCompleted(status: Status): Boolean = {
    import io.deepsense.graph.Status._
    val inProgressStatuses = Set(Draft, Queued, Running)
    !status.experiment.get.graph.nodes.exists(n => inProgressStatuses.contains(n.state.status))
  }

  private val graph = Graph(
    Set(Node(Node.Id.randomId, LoadDataFrame(SimpleGraphExecutionIntegSuiteEntities.entityUuid))))

  private val experiment = Experiment(
    Experiment.Id.randomId,
    SimpleGraphExecutionIntegSuiteEntities.entityTenantId,
    "name",
    graph,
    "experiment description"
  )

  override def beforeAll(): Unit = {
    super.beforeAll()
    system = ActorSystem("test")
    actorRef = TestActorRef(
      new RunningExperimentsActor(
        SimpleGraphExecutionIntegSuiteEntities.Name,
        3000,
        1000,
        DefaultGraphExecutorClientFactory()))
    testProbe = TestProbe()
    copyDataFrameToHdfs()
  }

  override def afterAll(): Unit = system.shutdown()
}
