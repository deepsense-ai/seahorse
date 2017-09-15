/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import java.net.{InetAddress, URI}
import java.util.{List => JList}

import scala.collection.mutable

import akka.actor.ActorSystem
import com.google.inject.name.Names
import com.google.inject.{Guice, Key}
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.permission.{FsAction, FsPermission}
import org.apache.hadoop.hdfs.DFSClient
import org.apache.hadoop.yarn.api.records._
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync
import org.apache.hadoop.yarn.client.api.async.impl.AMRMClientAsyncImpl
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

import io.deepsense.commons.akka.RemoteAddressExtension
import io.deepsense.commons.config.ConfigModule
import io.deepsense.commons.spark.sql.UserDefinedFunctions
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.dataframe.DataFrameBuilder
import io.deepsense.deeplang.{CatalogRecorder, DOperable, DSHdfsClient, ExecutionContext}
import io.deepsense.entitystorage.{EntityStorageClient, EntityStorageClientFactory}
import io.deepsense.models.entities.Entity
import io.deepsense.models.experiments.Experiment

object GraphExecutor extends LazyLogging {
  val sparkEventLogDir = "/tmp/spark-events"
  var entityStorageClientFactory: EntityStorageClientFactory = _

  def main(args: Array[String]): Unit = {
    logger.debug(s"Starting with args: ${args.mkString("[", ", ", "]")}")
    logger.debug("CLASSPATH = " + System.getenv("CLASSPATH"))
    logger.debug("SPARK_CLASSPATH = " + System.getenv("SPARK_CLASSPATH"))
    val injector = Guice.createInjector(
      new ConfigModule,
      new GraphExecutorModule,
      new GraphExecutorTestModule
    )
    logger.debug("Guice Injector ready")
    val entityStorageClientFactory = injector.getInstance(
      Key.get(classOf[EntityStorageClientFactory], Names.named(args(2)))
    )
    logger.debug("entityStorageClientFactory ready: {}", entityStorageClientFactory)
    val graphExecutor = GraphExecutor(entityStorageClientFactory)
    logger.debug("graphExecutor ready: {}", graphExecutor)
    val experimentId = args(0)
    val statusActorPath = args(1)
    graphExecutor.mainLoop(experimentId, statusActorPath)
  }
}

/**
 * GraphExecutor (it is YARN asynchronous Application Master).
 * Only one graph execution per GraphExecutor can be performed.
 */
case class GraphExecutor(entityStorageClientFactory: EntityStorageClientFactory)
  extends AMRMClientAsync.CallbackHandler
  with LazyLogging {

  implicit val conf: YarnConfiguration = new YarnConfiguration()
  val geConfig: Config = ConfigFactory.load

  val dOperableCache = mutable.Map[Entity.Id, DOperable]()

  val entityStorageClient = createEntityStorageClient(entityStorageClientFactory, geConfig)

  def mainLoop(
    experimentId: Experiment.Id,
    experimentStatusesReceiverActorPath: String): Unit = {
    logger.debug("mainLoop({}, {})", experimentId, experimentStatusesReceiverActorPath)
    val resourceManagerClient = new AMRMClientAsyncImpl[ContainerRequest](
      geConfig.getInt("graphexecutor.yarn.heartbeat.interval"),
      this)
    resourceManagerClient.init(conf)
    resourceManagerClient.start()
    logger.debug("AMRMClientAsyncImpl started")

    val dOperableCatalog = new DOperableCatalog
    CatalogRecorder.registerDOperables(dOperableCatalog)
    val executionContext = new ExecutionContext(dOperableCatalog)

    val sparkConf = new SparkConf()
    sparkConf.setAppName("Spark DeepSense Akka")
      .set("spark.eventLog.dir", s"hdfs://${GraphExecutor.sparkEventLogDir}")
      .set("spark.eventLog.enabled", "true")
      .set("spark.ui.retainedStages", "5000")
      .set("spark.logConf", "true")
      .set("spark.ui.enabled", "false")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .registerKryoClasses(Array())

    executionContext.hdfsClient = new DSHdfsClient(
      new DFSClient(new URI(getHdfsAddressFromConfig(geConfig)), new Configuration()))
    // NOTE: GraphExecutor.sparkEventLogDir have to be created before Spark context
    createHdfsDir(executionContext.hdfsClient, GraphExecutor.sparkEventLogDir)

    executionContext.sparkContext = new SparkContext(sparkConf)
    executionContext.sqlContext = new SQLContext(executionContext.sparkContext)
    UserDefinedFunctions.registerFunctions(executionContext.sqlContext.udf)
    executionContext.dataFrameBuilder = DataFrameBuilder(executionContext.sqlContext)
    executionContext.entityStorageClient = entityStorageClient

    val (actorSystem, remotePort) = startActorSystem()
    registerApplicationMaster(resourceManagerClient, remotePort)

    val geRef = actorSystem.actorOf(
      GraphExecutorActor.props(executionContext, experimentStatusesReceiverActorPath))
    geRef ! GraphExecutorActor.Messages.Start(experimentId)

    logger.debug("awaitTermination BEFORE")
    actorSystem.awaitTermination()
    logger.debug("awaitTermination AFTER")
    cleanup(resourceManagerClient, entityStorageClientFactory, actorSystem)
  }

  /**
   * Starts an actor system on the local domain.
   * @return The started actor system and port on which the system works.
   */
  def startActorSystem(): (ActorSystem, Int) = {
    val akkaConfig = geConfig.getConfig("graphexecutor-yarn-worker")
    logger.debug("graphexecutor-yarn-worker config:")
    logger.debug(s"$akkaConfig")

    val addressKey = "akka.remote.netty.tcp.hostname"
    val configuredAddress = akkaConfig.getString(addressKey)
    val address = InetAddress.getLocalHost.getHostName
    logger.debug(s"Overriding '$addressKey'. Using '$address' instead of '$configuredAddress'")
    val modifiedConfig = akkaConfig.withValue(
      "akka.remote.netty.tcp.hostname",
      ConfigValueFactory.fromAnyRef(address))

    val actorSystem = ActorSystem("graphexecutor-on-yarn", modifiedConfig)
    val remotePort = RemoteAddressExtension(actorSystem).address.port.get
    logger.debug(s">>> Actor system's port: $remotePort")
    (actorSystem, remotePort)
  }

  def registerApplicationMaster(
    resourceManagerClient: AMRMClientAsyncImpl[ContainerRequest], port: Int): Unit = {
    // Application Master registration
    val appMasterHostname = InetAddress.getLocalHost.getHostName
    val appMasterPort = port
    try {
      resourceManagerClient.registerApplicationMaster(appMasterHostname, appMasterPort, "")
    } catch {
      case e: Exception =>
        logger.error("Registering ApplicationMaster failed", e)
        throw e
    }
    logger.debug(s"AppMaster(hostname=$appMasterHostname, port=$appMasterPort) registered")
  }

  private def getHdfsAddressFromConfig(geConfig: Config): String = {
    val hdfsHostname = geConfig.getString("hdfs.hostname")
    val hdfsPort = geConfig.getString("hdfs.port")
    s"hdfs://$hdfsHostname:$hdfsPort"
  }

  /**
   * Performs orderly closing of Graph Executor components:
   * Application Master, entityStorageClientFactory and actorSystem.
   */
  private def cleanup(
    rmClient: AMRMClientAsyncImpl[ContainerRequest],
    entityStorageClientFactory: EntityStorageClientFactory,
    actorSystem: ActorSystem): Unit = {
    logger.debug("cleanup started")
    rmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "")
    entityStorageClientFactory.close()
    logger.debug(s"[GraphExecutor] Shutting down actorSystem: $actorSystem")
    actorSystem.shutdown()
    logger.debug("[GraphExecutor] ==>...DONE")
    logger.debug("cleanup end")
  }

  def createHdfsDir(dsHdfsClient: DSHdfsClient, path: String): Unit = {
    dsHdfsClient
      .hdfsClient
      .mkdirs(path, new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL), true)
  }

  override def onError(throwable: Throwable): Unit = {}

  override def getProgress: Float = 0f // TODO do we need it?

  override def onShutdownRequest(): Unit = {}

  override def onNodesUpdated(list: JList[NodeReport]): Unit = {}

  override def onContainersCompleted(list: JList[ContainerStatus]): Unit = {}

  override def onContainersAllocated(list: JList[Container]): Unit = {}

  private def createEntityStorageClient(
      entityStorageClientFactory: EntityStorageClientFactory,
      geConfig: Config): EntityStorageClient = {
    val actorSystemName = geConfig.getString("entityStorage.actorSystemName")
    val hostName = geConfig.getString("entityStorage.hostname")
    val port = geConfig.getInt("entityStorage.port")
    val actorName = geConfig.getString("entityStorage.actorName")
    val timeoutSeconds = geConfig.getInt("entityStorage.timeoutSeconds")
    logger.debug(
      s"EntityStorageClient($actorSystemName, $hostName, $port, $actorName, $timeoutSeconds)")
    entityStorageClientFactory.create(actorSystemName, hostName, port, actorName, timeoutSeconds)
  }
}
