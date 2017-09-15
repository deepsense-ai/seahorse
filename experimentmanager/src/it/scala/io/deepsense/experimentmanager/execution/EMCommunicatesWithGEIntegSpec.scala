/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
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
import org.scalatest.{FlatSpec, Matchers}

import io.deepsense.deeplang.doperations.ReadDataFrame
import io.deepsense.experimentmanager.execution.RunningExperimentsActor.{GetStatus, Launch, Status}
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphexecutor.{HdfsIntegTestSupport, SimpleGraphExecutionIntegSuiteEntities}
import io.deepsense.models.experiments.Experiment

class EMCommunicatesWithGEIntegSpec
  extends FlatSpec
  with MockitoSugar
  with Matchers
  with ScalaFutures
  with HdfsIntegTestSupport
  with LazyLogging {

  implicit var system: ActorSystem = _
  var actorRef: TestActorRef[RunningExperimentsActor] = _
  var testProbe: TestProbe = _

  implicit val timeout: Timeout = Timeout(1 second)
  val tenantId = "tenantId"
  val maxRetryNumber = 10

  "ExperimentManager" should "launch graph on GraphExecutor" in {
    testProbe.send(actorRef, Launch(experiment))

    var success = false
    breakable {
      for (i <- 0 until maxRetryNumber) {
        testProbe.send(actorRef, GetStatus(experiment.id))
        val status = testProbe.expectMsgType[Status]
        logger.info("Received status: {}", status)
        getFailedNodes(status.experiment.get.graph.nodes) shouldBe empty
        if (graphCompleted(status)) {
          success = true
          break()
        }
        Thread.sleep(1500)
      }
    }
    if (!success) {
      fail(s"Max retry: $maxRetryNumber and experiment did not run. Communication FAILED!!!")
    }
  }

  def graphCompleted(status: Status): Boolean = {
    val inProgressStatuses = Set(
      io.deepsense.graph.Status.Draft,
      io.deepsense.graph.Status.Queued,
      io.deepsense.graph.Status.Running)

    !status.experiment.get.graph.nodes.exists(n => inProgressStatuses.contains(n.state.status))
  }

  def getFailedNodes(nodes: Set[Node]): Set[Node] = {
    nodes.filter(n => n.state.status == io.deepsense.graph.Status.Failed)
  }

  private val graph = Graph(
    Set(Node(Node.Id.randomId, createReadDataFrameOperation)),
    Set())

  private val experiment: Experiment = Experiment(
    Experiment.Id.randomId,
    SimpleGraphExecutionIntegSuiteEntities.entityTenantId,
    "name",
    graph,
    "experiment description"
  )

  private def createReadDataFrameOperation: ReadDataFrame = {
    val readOp = new ReadDataFrame
    readOp.parameters.getStringParameter(ReadDataFrame.idParam).value =
      Some(SimpleGraphExecutionIntegSuiteEntities.entityUuid)
    readOp
  }


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

  override def afterAll(): Unit = {
    system.shutdown()
  }
}
