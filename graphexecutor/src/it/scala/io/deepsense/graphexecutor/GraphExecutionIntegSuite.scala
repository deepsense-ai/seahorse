/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfter, Matchers}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.DOperation
import io.deepsense.graph._
import io.deepsense.graphexecutor.clusterspawner.DefaultClusterSpawner
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.messages.{Launch, Update}

/**
 * NOTE: You can observe results of this test suite by running command on HDFS cluster:
 * hadoop fs -ls /deepsense/TestTenantId/dataframe
 */
abstract class GraphExecutionIntegSuite
  extends HdfsIntegTestSupport
  with Matchers
  with BeforeAndAfter
  with Eventually {

  val created = DateTimeConverter.now
  val updated = created.plusHours(1)

  implicit var system: ActorSystem = _
  var testProbe: TestProbe = _


  experimentName should {
    "run on external YARN cluster" in {
      testOnYarnCluster(experiment)
    }
  }

  protected def testOnYarnCluster(experiment: Experiment): Unit = {
    testProbe = TestProbe()

    val graphExecutorClient = system.actorOf(
      Props(new GraphExecutorClientActor(esFactoryName, DefaultClusterSpawner)),
      experiment.id.toString)

    graphExecutorClient ! Launch(experiment)

    eventually {
      testProbe.expectMsgType[Update].experiment shouldBe 'Completed
    }
  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.shutdown()
  }

  protected def experiment = Experiment(
    Experiment.Id.randomId,
    tenantId,
    experimentName,
    Graph(nodes.toSet, edges.toSet),
    created,
    updated)

  protected def node(operation: DOperation): Node = Node(Node.Id.randomId, operation)

  protected def nodes: Seq[Node]

  protected def edges: Seq[Edge]

  protected def experimentName: String

  protected def esFactoryName: String

  protected def tenantId: String
}
