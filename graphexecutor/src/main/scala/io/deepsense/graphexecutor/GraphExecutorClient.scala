/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.graphexecutor

import java.io._
import java.nio.ByteBuffer

import scala.collection.JavaConverters._
import scala.util.Try

import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.avro.AvroRuntimeException
import org.apache.avro.ipc.NettyTransceiver
import org.apache.avro.ipc.specific.SpecificRequestor
import org.apache.hadoop.fs.Path
import org.apache.hadoop.net.NetUtils
import org.apache.hadoop.yarn.api.records._
import org.apache.hadoop.yarn.client.api.YarnClient
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hadoop.yarn.util.Records

import io.deepsense.graph.Graph
import io.deepsense.graphexecutor.protocol.GraphExecutorAvroRpcProtocol
import io.deepsense.graphexecutor.util.Utils
import io.deepsense.models.experiments.Experiment
import io.deepsense.graphexecutor.GraphExecutorClient.ExecutorMemoryProperty
import io.deepsense.graphexecutor.GraphExecutorClient.HdfsHostnameProperty
import io.deepsense.graphexecutor.GraphExecutorClient.DriverMemoryProperty

/**
 * Starts Graph Executor on remote YARN cluster,
 * allows to send an Experiment for execution to Graph Executor.
 * NOTE: Only one graph launch per object is allowed.
 */
class GraphExecutorClient extends Closeable with LazyLogging {
  var yarnClient: Option[YarnClient] = None

  var applicationId: Option[ApplicationId] = None

  var rpcClient: Option[NettyTransceiver] = None

  var rpcProxy: Option[GraphExecutorAvroRpcProtocol] = None

  val geConfig: Config = ConfigFactory.load(Constants.GraphExecutorConfName)

  /**
   * Sends and experiment designated to immediate execution by Graph Executor.
   * NOTE: Call method {@link #init(timeout:Int)} before using RPC
   * @param experiment Experiment to send
   * @return true on success, false if graph has been received by Graph Executor earlier
   *         (return of false means that this is probably second method call)
   * @throws AvroRuntimeException on any critical problem (i.e. graph deserialization fail)
   */
  def sendExperiment(experiment: Experiment): Boolean = {
    val bytesOut = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(bytesOut)
    oos.writeObject(experiment)
    oos.flush()
    oos.close()
    val graphByteBuffer = ByteBuffer.wrap(bytesOut.toByteArray)
    rpcProxy.get.sendGraph(graphByteBuffer)
  }

  /**
   * Returns current state of experiment execution.
   * @return Graph with current state of execution
   * @throws AvroRuntimeException on any critical problem (i.e. graph not sent yet)
   */
  def getExecutionState(): Option[Graph] = rpcProxy.map { proxy =>
    val executionStateByteBuffer = proxy.getExecutionState
    val bufferIn = new ByteArrayInputStream(executionStateByteBuffer.array())
    val streamIn = new ObjectInputStream(bufferIn)
    val executionState = streamIn.readObject().asInstanceOf[Graph]
    executionState
  }

  /**
   * Aborts execution of graph.
   * Dequeues queued nodes, tries to stop currently processing threads.
   * @return true on success, false if graph has not been sent yet
   * @throws AvroRuntimeException on any critical problem (i.e. node executors pool shutdown fail)
   */
  def terminateExecution(): Boolean = rpcProxy.get.terminateExecution()

  /**
   * Checks if Graph Executor is in running state.
   * @return true if Graph Executor is in running state and false otherwise
   */
  def isGraphExecutorRunning(): Boolean = {
    // TODO: Application report cache could save some time and cluster resources
    val applicationReport = yarnClient.get.getApplicationReport(applicationId.get)
    applicationReport.getYarnApplicationState == YarnApplicationState.RUNNING
  }

  /**
   * Checks if Graph Executor is in finished state.
   * @return true if Graph Executor is in finished state and false otherwise
   */
  def isGraphExecutorFinished(): Boolean = {
    // TODO: Application report cache could save some time and cluster resources
    val applicationReport = yarnClient.get.getApplicationReport(applicationId.get)
    applicationReport.getYarnApplicationState == YarnApplicationState.FINISHED
  }

  /**
   * Checks if Graph Executor has finished its running.
   * @return true if Graph Executor is in end state and false otherwise
   */
  def hasGraphExecutorEndedRunning(): Boolean = {
    // TODO: Application report cache could save some time and cluster resources
    val applicationReport = yarnClient.get.getApplicationReport(applicationId.get)
    val endYarnAppStates = List(
      YarnApplicationState.FINISHED,
      YarnApplicationState.FAILED,
      YarnApplicationState.KILLED)
    endYarnAppStates.contains(applicationReport.getYarnApplicationState)
  }

  /**
   * Waits for Graph Executor start of running, then prepares RPC client for future use.
   * If Graph Executor has not ended running, this method can be called several times,
   * until it returns true.
   * @param timeout timeout in milliseconds
   * @return true on success and false on fail (possibly timeout exceed)
   * @throws Exception if RPC client has been successfully prepared before
   */
  def waitForSpawn(timeout: Int): Boolean = {
    val startOfWaitingForGraphExecutor = System.currentTimeMillis
    while (!isGraphExecutorRunning() && !hasGraphExecutorEndedRunning()
      && System.currentTimeMillis - startOfWaitingForGraphExecutor < timeout) {
      val remainingTimeout = timeout - (System.currentTimeMillis - startOfWaitingForGraphExecutor)
      try {
        Thread.sleep(math.min(Constants.EMGraphExecutorClientInitInterval, remainingTimeout))
      } catch {
        case e: InterruptedException => {
          // Silently proceed to next loop iteration
        }
      }
    }
    if (isGraphExecutorRunning() && !hasGraphExecutorEndedRunning()) {
      // Require will throw exception if
      require(rpcClient.isEmpty && rpcProxy.isEmpty)
      val applicationReport = yarnClient.get.getApplicationReport(applicationId.get)
      val graphExecutorRpcHost = applicationReport.getHost
      val graphExecutorRpcPort = applicationReport.getRpcPort

      val rpcSocket = NetUtils.createSocketAddr(graphExecutorRpcHost, graphExecutorRpcPort)
      rpcClient = Some(new NettyTransceiver(rpcSocket))
      rpcProxy = Some(SpecificRequestor.getClient(
        classOf[GraphExecutorAvroRpcProtocol],
        rpcClient.get))
      true
    } else {
      false
    }
  }

  /**
   * Submits Graph Executor on remote YARN cluster.
   * Graph Executor starts with few seconds delay after this method call due to cluster overhead,
   * or even longer delay if cluster is short of resources.
   */
  def spawnOnCluster(
      esFactoryName: String = "default",
      geUberJarLocation: String = Constants.GraphExecutorLibraryLocation,
      applicationConfLocation: String = Constants.GraphExecutorConfigLocation): Unit = {
    implicit val conf = new YarnConfiguration()
    // TODO: Configuration resource access should follow proper configuration access convention
    // or should be changed to simple configuration string access following proper convention
    conf.addResource(getClass().getResource("/conf/hadoop/core-site.xml"))
    // TODO: Configuration resource access should follow proper configuration access convention
    // or should be changed to simple configuration string access following proper convention
    conf.addResource(getClass().getResource("/conf/hadoop/yarn-site.xml"))

    yarnClient = Some(YarnClient.createYarnClient())
    yarnClient.get.init(conf)
    yarnClient.get.start()

    val app = yarnClient.get.createApplication()
    val amContainer = Records.newRecord(classOf[ContainerLaunchContext])

    val command = "/opt/spark/bin/spark-submit --class io.deepsense.graphexecutor.GraphExecutor " +
        " --master spark://" + geConfig.getString(HdfsHostnameProperty) + ":7077" +
        " --executor-memory " + geConfig.getString(ExecutorMemoryProperty) +
        " --driver-memory " + geConfig.getString(DriverMemoryProperty) +
        s" ./graphexecutor.jar $esFactoryName " + Utils.logRedirection
    logger.debug("Prepared {}", command)
    amContainer.setCommands(List(command).asJava)

    val appMasterJar = Utils.getConfiguredLocalResource(new Path(geUberJarLocation))
    amContainer.setLocalResources(Map(
      "graphexecutor.jar" -> appMasterJar
    ).asJava)

    // Setup env to get all yarn and hadoop classes in classpath
    val env = Utils.getConfiguredEnvironmentVariables
    amContainer.setEnvironment(env.asJava)
    val resource = Records.newRecord(classOf[Resource])
    // Allocated memory couldn't be less than 1GB
    resource.setMemory(1024)
    resource.setVirtualCores(1)
    val appContext = app.getApplicationSubmissionContext
    // TODO: Configuration string access should follow proper configuration access convention
    appContext.setApplicationName("Deepsense GraphExecutor")
    appContext.setAMContainerSpec(amContainer)
    appContext.setResource(resource)
    // TODO: move queue string to config file
    appContext.setQueue("default")
    // TODO: Configuration value access should follow proper configuration access convention
    // This value probably should equal to 1
    appContext.setMaxAppAttempts(1)
    // Submit the application
    Try {
      logger.info("Submitting application")
      yarnClient.get.submitApplication(appContext)
    }.map { aid =>
      logger.info("Application submitted - ID: {}", aid)
      applicationId = Some(aid)
    }.recover {
      case e: Exception =>
        logger.error("Exception thrown at submitting application", e)
        throw e
    }
  }

  /**
   * Releases all resources associated with this object.
   */
  override def close(): Unit = {
    rpcClient.foreach(_.close())
    rpcClient = None

    rpcProxy = None

    yarnClient.foreach(_.stop())
    yarnClient = None

    applicationId = None
  }
}

object GraphExecutorClient {
  val HdfsHostnameProperty = "hdfs.hostname"
  val ExecutorMemoryProperty = "spark.executor.memory"
  val DriverMemoryProperty = "spark.driver.memory"

  def apply() = new GraphExecutorClient()
}
