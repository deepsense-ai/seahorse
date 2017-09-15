/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

import akka.actor.ActorSystem
import com.google.inject.Inject
import org.apache.spark.launcher.{SparkAppHandle, SparkLauncher}

import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.auth.AuthContextInitiator
import io.deepsense.sessionmanager.service.sessionspawner.{SessionSpawner, SessionSpawnerException}

class SparkLauncherSessionSpawner @Inject()(
  private val system: ActorSystem,
  private val config: SparkLauncherConfig,
  private val authContextInitiator: AuthContextInitiator
) extends SessionSpawner with Logging {

  import scala.collection.JavaConversions._

  override def createSession(workflowId: Id, userId: String, token: String): Future[Unit] = {
    logger.info(s"Creating session for workflow $workflowId")

    authContextInitiator.init(userId, token).flatMap { ccache =>

      val listener = new AppHandleListener()

      new SparkLauncher(env(ccache))
        .setVerbose(true)
        .setMainClass(config.className)
        .setMaster("yarn-cluster")
        .setAppResource(config.weJarPath)
        .setAppName("SessionExecutor")
        .setSparkHome(config.sparkHome)
        .addFile(config.weDepsPath)
        .addAppArgs(args(workflowId, userId): _*)
        .setConf("spark.driver.extraClassPath", "__app__.jar")
        .setConf("spark.executorEnv.PYTHONPATH", config.weDepsFileName)
        .setConf("spark.driver.extraJavaOptions", "-XX:MaxPermSize=1024m -XX:PermSize=256m")
        .startApplication(listener)

      listener.executorStartedFuture
    }
  }

  private def env(ccache: String) = Map(
    "HADOOP_CONF_DIR" -> config.hadoopConfDir,
    "SPARK_YARN_MODE" -> "true",
    "HADOOP_USER_NAME" -> config.hadoopUserName,
    "KRB5CCNAME" -> s"FILE:$ccache"
  )

  private def args(workflowId: Id, userId: String) = Seq(
    "--interactive-mode",
    "-m", config.queueHost,
    "--message-queue-port", config.queuePort.toString,
    "--wm-address", config.wmAddress,
    "--workflow-id", workflowId.toString(),
    "-d", config.weDepsFileName,
    "--wm-username", config.wmUsername,
    "--wm-password", config.wmPassword,
    "--user-id", userId,
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
          case other =>
        }
      }
    }
  }

}
