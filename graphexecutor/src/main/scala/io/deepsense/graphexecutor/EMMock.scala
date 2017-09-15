/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 *
 * The mock of ExperimentManager, starts GraphExecutor on remote YARN cluster.
 *
 * Usage:
 * pull graphlibrary and publish it to local ivy2 repository:
 * (use its version from commit "Remove deprecated sbt-idea plugin, upgrade sbt")
 * sbt compile publish-local
 * pull graphexecutor and assembly it to single jar file:
 * sbt assembly
 * cp ./target/scala-2.11/graphexecutor-assembly-0.1.0.jar deepsense_environment_vagrant
 * cd deepsense_environment_vagrant; vagrant up; vagrant ssh ds-dev-env-master;
 * mcedit file.txt   #write double number in every line
 * ./cluster_manager.sh hadoop stop
 * mcedit /opt/hadoop/etc/hadoop/yarn-site.xml   # request-max-mem >= 1.3GB & total-mem >=2GB
 * ./cluster_manager.sh hadoop start
 * /opt/hadoop/bin/hdfs dfs -put -f file.txt /
 * /opt/hadoop/bin/hdfs dfs -put -f /vagrant/graphexecutor-assembly-0.1.0.jar /
 * scala io.deepsense.graphexecutor.EMMock /graphexecutor-assembly-0.1.0.jar
 *
 * TODO: This class should be replaced with integration tests as soon as it is possible
 * TODO: don't print anything....
 */
package io.deepsense.graphexecutor

import java.util.UUID

import io.deepsense.graph.{Edge, Endpoint, Graph, Node}
import io.deepsense.graphexecutor.DOperationTestClasses.{DOperation0To1Test, DOperation1To1Test, DOperation2To1Test}

object EMMock {
  def main(args: Array[String]): Unit = {
    val graphExecutorClient = GraphExecutorClient()
    graphExecutorClient.init(Constants.EMWaitForGraphExecutorClientInitDelay)

    try {
      val graphSent = graphExecutorClient.sendGraph(createMockedGraph)
      println("Result of sendGraph: " + graphSent)
      while (graphSent && !graphExecutorClient.hasGraphExecutorEndedRunning()) {
        println("Result of getExecutionState: " + graphExecutorClient.getExecutionState)
        // Sleeping is necessary to postpone next control loop iteration, delay arbitrarily chosen
        Thread.sleep(Constants.EMControlInterval)
      }
    } finally {
      graphExecutorClient.close()
    }
  }

  /**
   * Creates complex mocked graph
   * @return Mocked graph
   */
  private def createMockedGraph: Graph = {
    val node1 = Node(UUID.randomUUID(), new DOperation0To1Test)
    val node2 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node3 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node4 = Node(UUID.randomUUID(), new DOperation2To1Test)
    val nodes = Set(node1, node2, node3, node4)
    val edgesList = List(
      (node1, node2, 0, 0),
      (node1, node3, 0, 0),
      (node2, node4, 0, 0),
      (node3, node4, 0, 1))
    val edges = edgesList.map(n => Edge(Endpoint(n._1.id,  n._3), Endpoint(n._2.id, n._4))).toSet
    Graph(nodes, edges)
  }
}
