/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.graphexecutor

import java.util.UUID

import org.scalatest.{Ignore, BeforeAndAfter, Matchers}

import io.deepsense.deeplang.doperations.{ReadDataFrame, TimestampDecomposer, WriteDataFrame}
import io.deepsense.deeplang.parameters.NameSingleColumnSelection
import io.deepsense.graph._
import io.deepsense.models.experiments.Experiment

// Ignored because of problems with entitystorage
@Ignore
class GraphExecutorIntegSuite extends HdfsIntegTestSupport with Matchers with BeforeAndAfter {
  val simpleDataFramePathIn = s"$testDir/SimpleDataFrame"

  val simpleDataFramePathOut = s"$simpleDataFramePathIn.out"

  before {
    copy("src/test/resources/SimpleDataFrame", simpleDataFramePathIn)
  }

  "GraphExecutor" should
    "execute graph (ReadDF, DecomposeTimestamp, WriteDF) on external YARN cluster" in {
    val graphExecutorClient = GraphExecutorClient()
    try {
      graphExecutorClient.spawnOnCluster(
        s"$testDir/$uberJarFilename",
        s"$testDir/entitystorage-communication.conf")
      val spawned = graphExecutorClient.waitForSpawn(Constants.WaitForGraphExecutorClientInitDelay)
      spawned shouldBe true
      val graphSent = graphExecutorClient.sendExperiment(
        GraphMock.createGraphWithTimestampDecomposer(simpleDataFramePathIn, simpleDataFramePathOut))
      graphSent shouldBe true

      while (!graphExecutorClient.hasGraphExecutorEndedRunning()) {
        val graph = graphExecutorClient.getExecutionState()
        import io.deepsense.graph.Status
        graph.nodes.filter(n => n.state.status == Status.Aborted
          || n.state.status == Status.Failed).isEmpty shouldBe true
        // Sleeping to postpone next control loop iteration, delay arbitrarily chosen
        Thread.sleep(Constants.EMControlInterval)
      }
      // NOTE: Executed graph is not saved anywhere except GE. GE have to wait appropriate
      // time before closing RPC server, in order to allow to get executed graph state.
      graphExecutorClient.isGraphExecutorFinished() shouldBe true
      val graph = graphExecutorClient.getExecutionState()
      graph.nodes.filter(n => n.state.status != Status.Completed).isEmpty shouldBe true
      cli.get.exists(simpleDataFramePathOut) shouldBe true
    } finally {
      graphExecutorClient.close()
    }
  }

  object GraphMock {
    /**
     * Creates complex mocked graph
     * @return Mocked graph
     */
    def createGraphWithTimestampDecomposer(inPath: String, outPath: String): Experiment = {
      val graph = new Graph
      val readOp = new ReadDataFrame
      // TODO: not path, but id of existent entity
      readOp.parameters.getStringParameter("path").value = Some(inPath)

      val writeOp = new WriteDataFrame
      // TODO: not path, but name & description of entity to be created
      writeOp.parameters.getStringParameter("path").value = Some(outPath)

      val node1 = Node(UUID.randomUUID(), readOp)
      val node2 = Node(UUID.randomUUID(), timestampDecomposerOp("column4"))
      val node3 = Node(UUID.randomUUID(), writeOp)
      val nodes = Set(node1, node2, node3)
      val edgesList = List((node1, node2, 0, 0), (node2, node3, 0, 0))
      val edges = edgesList.map(n => Edge(Endpoint(n._1.id,  n._3), Endpoint(n._2.id, n._4))).toSet
      Experiment(Experiment.Id.randomId, "notImportant", "", Graph(nodes, edges))
    }

    private def timestampDecomposerOp(columnName: String): TimestampDecomposer = {
      val operation = new TimestampDecomposer
      val columnParam = operation.parameters.getSingleColumnSelectorParameter("timestampColumn")
      columnParam.value = Some(NameSingleColumnSelection(columnName))
      val timeUnitsParam = operation.parameters.getMultipleChoiceParameter("parts")
      timeUnitsParam.value = Some(Seq("year", "month", "day", "hour", "minutes", "seconds"))
      operation
    }
  }
}
