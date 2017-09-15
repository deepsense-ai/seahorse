/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.graphexecutor

import java.util.UUID

import org.scalatest.{BeforeAndAfter, Matchers}

import io.deepsense.deeplang.doperations.{DataFrameSplitter, ReadDataFrame, TimestampDecomposer, WriteDataFrame}
import io.deepsense.deeplang.parameters.NameSingleColumnSelection
import io.deepsense.graph._
import io.deepsense.models.experiments.Experiment

/**
 * NOTE: You can observe results of this test suite by running command on HDFS cluster:
 * hadoop fs -ls /deepsense/TestTenantId/dataframe
 */
class SimpleGraphExecutionIntegSuite
  extends HdfsIntegTestSupport
  with Matchers
  with BeforeAndAfter {

  before {
    copyDataFrameToHdfs()
  }

  "GraphExecutor" should
    "execute experiment (ReadDF, DecomposeTimestamp, WriteDF) on external YARN cluster" in {
    testOnYarnCluster(experimentWithTimestampDecomposer)
    // TODO: Currently we haven't got an ability to check result DataFrame saved on HDFS
  }

  it should "execute experiment (ReadDF, Split, 2xWriteDF) on external YARN cluster" in {
    testOnYarnCluster(experimentWithDataFrameSpliter)
    // TODO: Currently we haven't got an ability to check result DataFrames saved on HDFS
  }

  private def testOnYarnCluster(experiment: Experiment): Unit = {
    val graphExecutorClient = GraphExecutorClient()
    try {
      graphExecutorClient.spawnOnCluster(SimpleGraphExecutionIntegSuiteEntities.Name)
      val spawned = graphExecutorClient.waitForSpawn(Constants.WaitForGraphExecutorClientInitDelay)
      spawned shouldBe true
      val graphSent = graphExecutorClient.sendExperiment(experiment)
      graphSent shouldBe true

      while (!graphExecutorClient.hasGraphExecutorEndedRunning()) {
        val graph = graphExecutorClient.getExecutionState()
        import io.deepsense.graph.Status._
        forAll(graph.nodes) { _.state.status should not (be (Aborted) or be (Failed)) }
        // Sleeping to postpone next control loop iteration, delay arbitrarily chosen
        Thread.sleep(Constants.EMControlInterval)
      }
      // NOTE: Executed graph is not saved anywhere except GE. GE have to wait appropriate
      // time before closing RPC server, in order to allow to get executed graph state.
      graphExecutorClient shouldBe 'graphExecutorFinished
      val graph = graphExecutorClient.getExecutionState()
      forAll(graph.nodes) { _.state.status shouldBe Status.Completed }
    } finally {
      graphExecutorClient.close()
    }
  }

  /**
   * @return Mocked experiment with TimestampDecomposer DOperation
   */
  private def experimentWithTimestampDecomposer: Experiment = {
    val graph = new Graph
    val readOp = new ReadDataFrame
    readOp.parameters.getStringParameter(ReadDataFrame.idParam).value =
      Some(SimpleGraphExecutionIntegSuiteEntities.entityUuid)

    val timestampDecomposerOp = new TimestampDecomposer
    timestampDecomposerOp.parameters.getSingleColumnSelectorParameter("timestampColumn").value =
      Some(NameSingleColumnSelection("column4"))
    timestampDecomposerOp.parameters.getMultipleChoiceParameter("parts").value =
      Some(Seq("year", "month", "day", "hour", "minutes", "seconds"))

    import io.deepsense.deeplang.doperations.WriteDataFrame._
    val writeOp = new WriteDataFrame
    writeOp.parameters.getStringParameter(nameParam).value = Some("left name")
    writeOp.parameters.getStringParameter(descriptionParam).value = Some("left description")

    val node1 = Node(UUID.randomUUID(), readOp)
    val node2 = Node(UUID.randomUUID(), timestampDecomposerOp)
    val node3 = Node(UUID.randomUUID(), writeOp)
    val nodes = Set(node1, node2, node3)
    val edgesList = List((node1, node2, 0, 0), (node2, node3, 0, 0))
    val edges = edgesList.map(n => Edge(Endpoint(n._1.id,  n._3), Endpoint(n._2.id, n._4))).toSet
    Experiment(
      Experiment.Id.randomId,
      SimpleGraphExecutionIntegSuiteEntities.entityTenantId,
      "Test experimentWithTimestampDecomposer",
      Graph(nodes, edges))
  }

  /**
   * @return Mocked experiment with DataFrameSpliter DOperation
   */
  def experimentWithDataFrameSpliter: Experiment = {
    val graph = new Graph

    val readOp = new ReadDataFrame
    readOp.parameters.getStringParameter(ReadDataFrame.idParam).value =
      Some(SimpleGraphExecutionIntegSuiteEntities.entityUuid)
    val node1 = Node(UUID.randomUUID(), readOp)

    val splitOp = new DataFrameSplitter
    splitOp.parameters.getNumericParameter(splitOp.splitRatioParam).value = Some(0.2)
    splitOp.parameters.getNumericParameter(splitOp.seedParam).value = Some(1)
    val node2 = Node(UUID.randomUUID(), splitOp)

    import io.deepsense.deeplang.doperations.WriteDataFrame._
    val writeOpLeft = new WriteDataFrame
    writeOpLeft.parameters.getStringParameter(nameParam).value = Some("left name")
    writeOpLeft.parameters.getStringParameter(descriptionParam).value = Some("left description")
    val node3 = Node(UUID.randomUUID(), writeOpLeft)

    val writeOpRight = new WriteDataFrame
    writeOpRight.parameters.getStringParameter(nameParam).value = Some("right name")
    writeOpRight.parameters.getStringParameter(descriptionParam).value = Some("right description")
    val node4 = Node(UUID.randomUUID(), writeOpRight)

    val nodes = Set(node1, node2, node3, node4)
    val edgesList = List(
      (node1, node2, 0, 0),
      (node2, node3, 0, 0),
      (node2, node4, 1, 0)
    )
    val edges = edgesList.map(n => Edge(Endpoint(n._1.id,  n._3), Endpoint(n._2.id, n._4))).toSet
    Experiment(
      Experiment.Id.randomId,
      SimpleGraphExecutionIntegSuiteEntities.entityTenantId,
      "Test experimentWithDataFrameSpliter",
      Graph(nodes, edges))
  }
}
