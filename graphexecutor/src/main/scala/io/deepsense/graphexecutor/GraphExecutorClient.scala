/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.{Success, Try}

import akka.actor._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.fs.Path
import org.apache.hadoop.yarn.api.records._
import org.apache.hadoop.yarn.client.api.YarnClient
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hadoop.yarn.util.Records

import io.deepsense.commons.exception.FailureCode._
import io.deepsense.commons.exception.{DeepSenseFailure, FailureDescription}
import io.deepsense.graphexecutor.util.Utils
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.messages._

class GraphExecutorClientActor(
    entitystorageLabel: String,
    parentRemoteActorPath: String)
  extends Actor with LazyLogging {

  var experiment: Experiment = _
  var applicationId: ApplicationId = _
  var yarnClient: YarnClient = _
  var ge: ActorRef = _

  import GraphExecutorClient._

  override def receive: Receive = handleLaunchRequests orElse handleUnknownRequests

  def handleLaunchRequests: Receive = {
    case Launch(e) =>
      logger.info(">>> Launch(id: {} / status: {})", e.id, e.state.status)
      logger.info("entitystorageLabel={}", entitystorageLabel)
      experiment = e

      import scala.concurrent.duration._

      import context.dispatcher

      implicit val timeout: Timeout = 60.seconds

      val actorPath = parentRemoteActorPath + "/" + self.path.name
      logger.debug("Using actorPath={}", actorPath)

      val me = self
      val rea = sender()
      Future(spawnOnCluster(e.id, actorPath, entitystorageLabel)).onComplete {
        case Success(Success((yc, appId))) =>
          logger.info("Application spawned: {}", appId)
          me ! ExecutorSpawned(appId, yc)
        case r =>
          val error = s"Spawning experiment on cluster failed: $r"
          logger.error(error)
          val experimentFailureDetails = FailureDescription(
            DeepSenseFailure.Id.randomId,
            LaunchingFailure,
            error)
          val msg = Update(Some(experiment.markFailed(experimentFailureDetails)))
          rea ! msg
          logger.debug("<<< {}", msg)
          self ! PoisonPill
      }
      logger.info(s"Awaiting YARN up (via ExecutorSpawned request)")
      context.become(handleExecutorSpawnedRequests orElse handleUnknownRequests)
  }

  def handleExecutorSpawnedRequests: Receive = {
    case msg @ ExecutorSpawned(appId, yc) =>
      logger.info(">>> {}", msg)
      this.applicationId = appId
      this.yarnClient = yc
      context.become(handleExecutorReadyRequests orElse handleUnknownRequests)
      logger.info("<<< {}", msg)
  }

  def handleExecutorReadyRequests: Receive = {
    case msg @ ExecutorReady(eid) =>
      logger.info(">>> {}", msg)
      ge = sender()
      logger.debug("Sending Launch({}) to {}", experiment.id, ge)
      ge ! Launch(experiment)
      context.become(handleSuccessRequest orElse handleUnknownRequests)
  }

  def handleSuccessRequest: Receive = {
    case msg @ Success(exp) =>
      logger.debug(">>> {}", msg)
      // FIXME The type in Success is Any and hence the need to have custom message type
      experiment = msg.get.asInstanceOf[Experiment]
      context.parent.forward(msg)
      context.become(handleStatusRequests orElse handleAbortRequests orElse handleUnknownRequests)
  }

  def handleStatusRequests: Receive = {
    case msg @ Update(Some(exp)) =>
      logger.info(">>> Update({}) / status: {}", exp.id, exp.state.status)
      experiment = exp
      context.parent.forward(msg)
      if (exp.isCompleted || exp.isFailed) {
        logger.info("Experiment [{}] / status: {} - cleaning up", exp.id, exp.state.status)
        yarnClient.stop()
        self ! PoisonPill
      }
  }

  def handleAbortRequests: Receive = {
    case msg @ Abort(eid) =>
      logger.info(">>> $msg / status: {}", experiment.state.status)
      ge.forward(msg)
      logger.info("<<< {}", msg)
  }

  def handleUnknownRequests: Receive = {
    case m => logger.info(s"UNHANDLED: $m from ${sender()}")
  }

  /**
   * Submits Graph Executor on remote YARN cluster.
   * Graph Executor starts with few seconds delay after this method call due to cluster overhead,
   * or even longer delay if cluster is short of resources.
   */
  def spawnOnCluster(
    experimentId: Experiment.Id,
    graphExecutionStatusesActorPath: String,
    esFactoryName: String = "default",
    applicationConfLocation: String = Constants.GraphExecutorConfigLocation): Try[(YarnClient, ApplicationId)] = {

    logger.debug(">>> spawnOnCluster")

    implicit val conf = new YarnConfiguration()
    conf.addResource(getClass.getResource("/conf/hadoop/core-site.xml"))
    conf.addResource(getClass.getResource("/conf/hadoop/yarn-site.xml"))

    val yarnClient = YarnClient.createYarnClient()
    yarnClient.init(conf)
    yarnClient.start()

    val app = yarnClient.createApplication()
    val amContainer = Records.newRecord(classOf[ContainerLaunchContext])

    val geCfg = ConfigFactory.load(Constants.GraphExecutorConfName)
    val command = "/opt/spark/bin/spark-submit --class io.deepsense.graphexecutor.GraphExecutor" +
      " --master spark://" + geCfg.getString(HdfsHostnameProperty) + ":7077" +
      " --executor-memory " + geCfg.getString(ExecutorMemoryProperty) +
      " --driver-memory " + geCfg.getString(DriverMemoryProperty) +
      " --jars ./graphexecutor-deps.jar" +
      s" ./graphexecutor.jar $experimentId $graphExecutionStatusesActorPath $esFactoryName" + Utils.logRedirection
    logger.debug("Prepared {}", command)
    amContainer.setCommands(List(command).asJava)

    val geConf = Utils.getConfiguredLocalResource(new Path(Constants.GraphExecutorConfigLocation))
    val esConf = Utils.getConfiguredLocalResource(new Path(Constants.EntityStorageConfigLocation))
    val geJar = Utils.getConfiguredLocalResource(new Path(Constants.GraphExecutorJarLocation))
    val geDepsJar = Utils.getConfiguredLocalResource(new Path(Constants.GraphExecutorDepsJarLocation))
    amContainer.setLocalResources(Map(
      Constants.GraphExecutorConfName -> geConf,
      Constants.EntityStorageConfName -> esConf,
      "graphexecutor.jar" -> geJar,
      "graphexecutor-deps.jar" -> geDepsJar
    ).asJava)

    // Setup env to get all yarn and hadoop classes in classpath
    val env = Utils.getConfiguredEnvironmentVariables
    amContainer.setEnvironment(env.asJava)
    val resource = Records.newRecord(classOf[Resource])
    // Allocated memory couldn't be less than 1GB
    resource.setMemory(1024)
    resource.setVirtualCores(1)
    val appContext = app.getApplicationSubmissionContext
    appContext.setApplicationName(s"DeepSense.io GraphExecutor [experiment: $experimentId]")
    appContext.setAMContainerSpec(amContainer)
    appContext.setResource(resource)
    appContext.setQueue("default")
    appContext.setMaxAppAttempts(1)
    Try {
      logger.debug("Submitting application to YARN cluster: {}", appContext)
      yarnClient.submitApplication(appContext)
    }.map { aid =>
      logger.info("Application submitted - ID: {}", aid)
      (yarnClient, aid)
    }.recover {
      case e: Exception =>
        logger.error("Exception thrown at submitting application", e)
        throw e
    }
  }
}

object GraphExecutorClient extends LazyLogging {
  val HdfsHostnameProperty = "hdfs.hostname"
  val ExecutorMemoryProperty = "spark.executor.memory"
  val DriverMemoryProperty = "spark.driver.memory"

  sealed trait Message

  case class ExecutorSpawned(aid: ApplicationId, yarnClient: YarnClient) extends Message
}
