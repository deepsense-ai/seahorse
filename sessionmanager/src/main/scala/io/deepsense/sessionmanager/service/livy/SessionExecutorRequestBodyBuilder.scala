/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.livy

import com.google.inject.Inject
import com.google.inject.name.Named

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.service.livy.requests.Create
import io.deepsense.sessionmanager.service.HostAddressResolver

/**
  * Allows to build Livy requests to run Session Executor.
  *
  * @param className Class name of the application to execute.
  * @param applicationJarPath Path to JAR with the application code.
  * @param queueHost MQ address.
  */
class SessionExecutorRequestBodyBuilder @Inject() (
  @Named("session-executor.parameters.class-name") private val className: String,
  @Named("session-executor.parameters.application-jar-path") private val applicationJarPath: String,
  @Named("session-executor.parameters.queue.host") private val configQueueHost: String,
  @Named("session-executor.parameters.queue.port") private val queuePort: Int,
  @Named("session-executor.parameters.queue.autodetect-host") private val queueHostAuto: Boolean,
  @Named("session-executor.parameters.pyexecutor.dir") private val pyExecutorDir: String,
  @Named("session-executor.parameters.pyexecutor.jar") private val pyExecutorJar: String,
  @Named("session-executor.parameters.pyspark.dir") private val pySparkDir: String,
  @Named("session-executor.parameters.pyspark.zip") private val pySparkZip: String,
  @Named("session-executor.parameters.workflow-manager.scheme") private val wmScheme: String,
  @Named("session-executor.parameters.workflow-manager.host") private val wmHost: String,
  @Named("session-executor.parameters.workflow-manager.port") private val wmPort: String,
  @Named("session-executor.parameters.workflow-manager.autodetect-host")
  private val wmHostAuto: Boolean
) extends RequestBodyBuilder {

  private val wmAddress = if (wmHostAuto) {
    s"$wmScheme://${HostAddressResolver.getHostAddress}:$wmPort"
  } else {
    s"$wmScheme://$wmHost:$wmPort"
  }

  private val queueHost = if (queueHostAuto) {
    HostAddressResolver.getHostAddress
  } else {
    configQueueHost
  }

  /**
    * Return a 'Create' request that,
    * when executed, will spawn Session Executor
    *
    * @param workflowId An identifier of a workflow that SE will operate on.
    */
  def createSession(workflowId: Id): Create = {
    val pyExecutorFile = s"$pyExecutorDir/$pyExecutorJar"
    val pySparkFile = s"$pySparkDir/$pySparkZip"

    Create(
      applicationJarPath,
      className,
      args = Seq(
        "--interactive-mode",
        "-m", queueHost,
        "--message-queue-port", queuePort.toString,
        "--wm-address", wmAddress,
        "-p", pyExecutorJar,
        "-z", pySparkZip,
        "-j", workflowId.toString()
      ),
      files = Seq(pyExecutorFile, pySparkFile),
      conf = Map("spark.driver.extraClassPath" -> pyExecutorJar)
    )
  }
}
