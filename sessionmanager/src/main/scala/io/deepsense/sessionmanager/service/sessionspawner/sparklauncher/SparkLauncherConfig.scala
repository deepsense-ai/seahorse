/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher

import java.io.File
import java.net.URI

import com.google.inject.Inject
import com.google.inject.name.Named

import io.deepsense.sessionmanager.service.HostAddressResolver

class SparkLauncherConfig @Inject()(
  @Named("session-executor.parameters.class-name") val className: String,
  @Named("session-executor.parameters.application-jar-path") val weJarPath: String,
  @Named("session-executor.parameters.deps-zip-path") val weDepsPath: String,
  @Named("session-executor.parameters.spark-home-path") val sparkHome: String,
  @Named("session-executor.parameters.queue.host") private val configQueueHost: String,
  @Named("session-executor.parameters.queue.port") val queuePort: Int,
  @Named("session-executor.parameters.queue.autodetect-host") private val queueHostAuto: Boolean,
  @Named("session-executor.parameters.workflow-manager.scheme") private val wmScheme: String,
  @Named("session-executor.parameters.workflow-manager.host") private val wmHost: String,
  @Named("session-executor.parameters.workflow-manager.port") private val wmPort: String,
  @Named("session-executor.parameters.workflow-manager.autodetect-host")
  private val wmHostAuto: Boolean,
  @Named("session-executor.parameters.yarn.hadoop-conf-dir") val hadoopConfDir: String,
  @Named("session-executor.parameters.yarn.hadoop-user-name") val hadoopUserName: String,
  @Named("session-executor.parameters.workflow-manager.username") val wmUsername: String,
  @Named("session-executor.parameters.workflow-manager.password") val wmPassword: String,
  @Named("session-executor.parameters.temp-dir") val tempDir: String
) {

  def wmAddress: String = if (wmHostAuto) {
    s"$wmScheme://${HostAddressResolver.getHostAddress()}:$wmPort"
  } else {
    s"$wmScheme://$wmHost:$wmPort"
  }

  def queueHost: String = if (queueHostAuto) {
    HostAddressResolver.getHostAddress()
  } else {
    configQueueHost
  }

  def weDepsFileName: String = {
    new File(new URI(weDepsPath).getPath).getName
  }

}
