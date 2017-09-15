/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor.clusterspawner

import scala.collection.JavaConverters._

import scala.util.Try

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.fs.Path
import org.apache.hadoop.yarn.api.records.{ApplicationId, ContainerLaunchContext, Resource}
import org.apache.hadoop.yarn.client.api.YarnClient
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hadoop.yarn.util.Records
import io.deepsense.commons.config.ConfigurationMerger
import io.deepsense.graphexecutor.Constants
import io.deepsense.graphexecutor.util.Utils
import io.deepsense.models.experiments.Experiment

case object DefaultClusterSpawner extends ClusterSpawner with LazyLogging {

  val HdfsHostnameProperty = "hdfs.hostname"
  val ExecutorMemoryProperty = "spark.executor.memory"
  val DriverMemoryProperty = "spark.driver.memory"

  /**
   * Spawns GE as an YARN application on remote YARN cluster.
   * Graph Executor starts with few seconds delay after this method call due to cluster overhead,
   * or even longer delay if cluster is short of resources.
   */
  def spawnOnCluster(
      experimentId: Experiment.Id,
      graphExecutionStatusesActorPath: String,
      esFactoryName: String = "default",
      applicationConfLocation: String = Constants.GraphExecutorConfigLocation):
    Try[(YarnClient, ApplicationId)] = {

    logger.debug(">>> spawnOnCluster")

    implicit val conf = new YarnConfiguration()
    conf.addResource(getClass.getResource("/conf/hadoop/core-site.xml"))
    conf.addResource(getClass.getResource("/conf/hadoop/yarn-site.xml"))

    val config = ConfigFactory.load
    ConfigurationMerger.merge(conf, config.getConfig("hadoop"))

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
      " --driver-class-path \"$CLASSPATH\" " +
      " --jars ./graphexecutor-deps.jar " +
      s" ./graphexecutor.jar $experimentId $graphExecutionStatusesActorPath $esFactoryName" +
      Utils.logRedirection
    logger.debug("Prepared {}", command)
    amContainer.setCommands(List(command).asJava)

    val geConf = Utils.getConfiguredLocalResource(new Path(Constants.GraphExecutorConfigLocation))
    val geJar = Utils.getConfiguredLocalResource(new Path(Constants.GraphExecutorJarLocation))
    val log4jXml = Utils.getConfiguredLocalResource(new Path(Constants.Log4jXmlLocation))
    val geDepsJar = Utils
      .getConfiguredLocalResource(new Path(Constants.GraphExecutorDepsJarLocation))
    amContainer.setLocalResources(Map(
      Constants.GraphExecutorConfName -> geConf,
      Constants.Log4jXmlName -> log4jXml,
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

