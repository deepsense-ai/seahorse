/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher

import scala.concurrent.{Future, Promise}
import akka.actor.ActorSystem
import com.google.inject.Inject
import org.apache.spark.launcher.{SparkAppHandle, SparkLauncher, SparkSubmitOptionParser}
import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.rest.requests.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner.{SessionSpawner, SessionSpawnerException}

class SparkLauncherSessionSpawner @Inject()(
  private val system: ActorSystem,
  private val config: SparkLauncherConfig
) extends SessionSpawner with Logging {

  import scala.collection.JavaConversions._

  override def createSession(workflowId: Id, userId: String,
    clusterDetails: ClusterDetails): Future[Unit] = {

    logger.info(s"Creating session for workflow: $workflowId")
    logger.info(s"Cluster Details: $clusterDetails")

    val listener = new AppHandleListener()

    new SparkLauncher()
      .setVerbose(true)
      .setMainClass(config.className)
      .setMaster("spark://sessionmanager:7077")
      .setDeployMode("cluster")
      .setAppResource(config.weJarPath)
      .setAppName("SessionExecutor")
      .setSparkHome(config.sparkHome)
      .addFile(config.weDepsPath)
      .addAppArgs(args(workflowId, userId): _*)
      .setConf("spark.driver.extraClassPath", config.weJarPath)
      .setConf("spark.executorEnv.PYTHONPATH", config.weDepsPath)
      .setConf("spark.driver.extraJavaOptions",
        "-XX:MaxPermSize=1024m -XX:PermSize=256m -Dfile.encoding=UTF8")
      .setConf("spark.yarn.appMasterEnv.PYSPARK_PYTHON", config.pythonBinary)
      .startApplication(listener)

    logger.info(s"Waiting for proper status ${workflowId}")
    listener.executorStartedFuture
  }

  private def args(workflowId: Id, userId: String) = Seq(
    "--interactive-mode",
    "--message-queue-host", config.queueHost,
    "--message-queue-port", config.queuePort.toString,
    "--message-queue-user", config.queueUser,
    "--message-queue-pass", config.queuePass,
    "--wm-address", config.wmAddress,
    "--workflow-id", workflowId.toString(),
    "-d", config.weDepsPath,
    "--wm-username", config.wmUsername,
    "--wm-password", config.wmPassword,
    "--user-id", userId,
    "--temp-dir", config.tempDir,
    "--python-binary", config.pythonBinary
  )

  private class AppHandleListener extends SparkAppHandle.Listener {

    def executorStartedFuture: Future[Unit] = promise.future

    private val promise = Promise[Unit]

    override def infoChanged(handle: SparkAppHandle): Unit = {
      logger.info(s"App ${handle.getAppId} info changed: ${handle.toString}")
    }

    override def stateChanged(handle: SparkAppHandle): Unit = {
      logger.info(s"App ${handle.getAppId} state changed: ${handle.getState}")
      if (!promise.isCompleted) {
        handle.getState match {
          case SparkAppHandle.State.SUBMITTED => promise.success(())
          case SparkAppHandle.State.FAILED | SparkAppHandle.State.KILLED => {
            val msg = s"Spark process could not start. State: ${handle.getState} "
            val exception = new SessionSpawnerException(msg)
            promise.failure(exception)
          }
          case other => logger.info(s"No default action for ${handle}")
        }
      }
    }
  }
}
