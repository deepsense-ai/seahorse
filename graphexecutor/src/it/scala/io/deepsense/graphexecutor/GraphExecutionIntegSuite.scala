/**
 * Copyright (c) 2015, CodiLime Inc.
 */
package io.deepsense.graphexecutor

import java.util.UUID

import org.scalatest.{BeforeAndAfter, Matchers}

import io.deepsense.deeplang.DOperation
import io.deepsense.graph._
import io.deepsense.models.experiments.Experiment

/**
 * NOTE: You can observe results of this test suite by running command on HDFS cluster:
 * hadoop fs -ls /deepsense/TestTenantId/dataframe
 */
abstract class GraphExecutionIntegSuite
  extends HdfsIntegTestSupport
  with Matchers
  with BeforeAndAfter {

  experimentName should "run on external YARN cluster" in {
    testOnYarnCluster(experiment)
  }

  protected def testOnYarnCluster(experiment: Experiment): Unit = {
    val graphExecutorClient = GraphExecutorClient()
    try {
      graphExecutorClient.spawnOnCluster(esFactoryName)
      val spawned = graphExecutorClient.waitForSpawn(Constants.WaitForGraphExecutorClientInitDelay)
      spawned shouldBe true
      val graphSent = graphExecutorClient.sendExperiment(experiment)
      graphSent shouldBe true

      while (!graphExecutorClient.hasGraphExecutorEndedRunning()) {
        val graph = graphExecutorClient.getExecutionState()
        import io.deepsense.graph.Status._
        forAll(graph.nodes) {
          _.state.status should not(be(Aborted) or be(Failed))
        }
        // Sleeping to postpone next control loop iteration, delay arbitrarily chosen
        Thread.sleep(Constants.EMControlInterval)
      }
      // NOTE: Executed graph is not saved anywhere except GE. GE have to wait appropriate
      // time before closing RPC server, in order to allow to get executed graph state.
      graphExecutorClient shouldBe 'graphExecutorFinished
      val graph = graphExecutorClient.getExecutionState()
      forAll(graph.nodes) {
        _.state.status shouldBe Status.Completed
      }
    } finally {
      graphExecutorClient.close()
    }
  }

  protected def experiment = Experiment(
    Experiment.Id.randomId,
    tenantId,
    experimentName,
    Graph(nodes.toSet, edges.toSet))

  protected def node(operation: DOperation): Node = Node(UUID.randomUUID(), operation)

  protected def nodes: Seq[Node]

  protected def edges: Seq[Edge]

  protected def experimentName: String

  protected def esFactoryName: String

  protected def tenantId: String
}
