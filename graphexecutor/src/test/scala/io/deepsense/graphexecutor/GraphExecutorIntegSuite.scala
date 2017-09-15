package io.deepsense.graphexecutor

import java.io._
import java.net.URI

import scala.util.Random

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hdfs.DFSClient
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

import io.deepsense.graph.{Status, Node}

class GraphExecutorIntegSuite extends FunSuite with Matchers with BeforeAndAfter {

  // TODO: Is this a proper way to construct filepath? (how to do this?)
  // Maybe some constants can be used (project name, project version, scala version)?
  val projectUberJar = new File("target/scala-2.11/deepsense-graphexecutor-assembly-0.1.0.jar")

  private val config = new Configuration()

  var hdfsLocation: Option[String] = None

  before {
    // TODO: Configuration resource access should follow proper configuration access convention
    config.addResource("conf/hadoop/core-site.xml")
    config.addResource("conf/hadoop/yarn-site.xml")
    config.addResource("conf/hadoop/hdfs-site.xml")

    var cli: Option[DFSClient] = None
    var out: Option[BufferedOutputStream] = None
    var in: Option[BufferedInputStream] = None
    try {
      cli = Some(new DFSClient(
        new URI("hdfs://" + Constants.MasterHostname + ":" + Constants.HdfsNameNodePort),
        config))
      hdfsLocation = Some("/tmp/deepsense-integ-test/graphexecutor-" +
        Random.alphanumeric.take(10).mkString + ".jar")
      out = Some(new BufferedOutputStream(cli.get.create(hdfsLocation.get, false)))
      in = Some(new BufferedInputStream(new FileInputStream(projectUberJar)))
      val buffer = new Array[Byte](1024 * 1024)

      // Copy local file to hdfs
      var len = 0
      while (len != -1) {
        len = in.get.read(buffer)
        if (len != -1) {
          out.get.write(buffer, 0, len)
        }
      }

    } finally {
      assert(hdfsLocation.nonEmpty)
      if (in.nonEmpty) {
        in.get.close()
      }
      if (out.nonEmpty) {
        out.get.flush()
        out.get.close()
      }
      if (cli.nonEmpty) {
        cli.get.close()
      }
    }
  }

  after {
    var cli: Option[DFSClient] = None
    if (hdfsLocation.nonEmpty) {
      try {
        cli = Some(new DFSClient(new URI("hdfs://" + Constants.MasterHostname + ":8020"), config))
        cli.get.delete(hdfsLocation.get, true)
      } finally {
        if (cli.nonEmpty) {
          cli.get.close()
        }
      }
    }
  }

  test("GraphExecutor could execute graph on external YARN cluster") {
    val graphExecutorClient = GraphExecutorClient()
    try {
      graphExecutorClient.spawnOnCluster(hdfsLocation.get)
      val spawned = graphExecutorClient.waitForSpawn(Constants.WaitForGraphExecutorClientInitDelay)
      assert(spawned)
      val graphSent = graphExecutorClient.sendGraph(GraphMock.createMockedGraph)
      assert(graphSent)

      while (!graphExecutorClient.hasGraphExecutorEndedRunning()) {
        val graph = graphExecutorClient.getExecutionState()
        import io.deepsense.graph.Status
        assert(graph.nodes
          .filter(n => n.state.status == Status.Aborted|| n.state.status == Status.Failed).isEmpty)
        // Sleeping to postpone next control loop iteration, delay arbitrarily chosen
        Thread.sleep(Constants.EMControlInterval)
      }
      // NOTE: Executed graph is not saved anywhere except GE. GE have to wait appropriate
      // time before closing RPC server, in order to allow to get executed graph state.
      assert(graphExecutorClient.isGraphExecutorFinished())
      val graph = graphExecutorClient.getExecutionState()
      assert(graph.nodes.filter(n => n.state.status != Status.Completed).isEmpty)
    } finally {
      graphExecutorClient.close()
    }
  }
}
