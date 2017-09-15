/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher

import java.io.File
import java.net.{URI, URL}

import com.google.inject.Inject
import com.google.inject.name.Named

class SparkLauncherConfig @Inject()(
  @Named("session-executor.parameters.class-name") val className: String,
  @Named("session-executor.parameters.application-jar-path") val weJarPath: String,
  @Named("session-executor.parameters.deps-zip-path") val weDepsPath: String,
  @Named("session-executor.parameters.spark-resources-jars-dir") val sparkResourcesJarsDir: String,
  @Named("session-executor.parameters.spark-home-path") val sparkHome: String,
  @Named("session-executor.parameters.queue.port") val queuePort: Int,
  @Named("session-executor.parameters.queue.host") val queueHost: String,
  @Named("session-executor.parameters.queue.user") val queueUser: String,
  @Named("session-executor.parameters.queue.pass") val queuePass: String,
  @Named("session-executor.parameters.workflow-manager.scheme") val wmScheme: String,
  @Named("session-executor.parameters.workflow-manager.address") val wmAddress: String,
  @Named("session-executor.parameters.workflow-manager.username") val wmUsername: String,
  @Named("session-executor.parameters.workflow-manager.password") val wmPassword: String,
  @Named("session-executor.parameters.mail-server.smtp.host") val mailServerHost: String,
  @Named("session-executor.parameters.mail-server.smtp.port") val mailServerPort: Int,
  @Named("session-executor.parameters.mail-server.user") val mailServerUser: String,
  @Named("session-executor.parameters.mail-server.password") val mailServerPassword: String,
  @Named("session-executor.parameters.mail-server.sender") val mailServerSender: String,
  @Named("session-executor.parameters.notebook-server.address") val notebookServerAddress: String,
  @Named("session-executor.parameters.temp-dir") val tempDir: String,
  @Named("session-executor.parameters.python-driver-binary") val pythonDriverBinary: String,
  @Named("session-executor.parameters.python-executor-binary") val pythonExecutorBinary: String
) {

  def weDepsFileName: String = {
    new File(new URI(weDepsPath).getPath).getName
  }

}
