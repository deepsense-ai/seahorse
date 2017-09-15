/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.executor

import io.deepsense.sessionmanager.service.sessionspawner.SessionConfig
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherConfig

object SessionExecutorArgs {

  // TODO Have args parser/formatter in we.jar and use it here to have type-safety

  def apply(
      sessionConfig: SessionConfig,
      config: SparkLauncherConfig): Seq[String] = Seq(
    "--interactive-mode",
    "--message-queue-host", config.queueHost,
    "--message-queue-port", config.queuePort.toString,
    "--message-queue-user", config.queueUser,
    "--message-queue-pass", config.queuePass,
    "--wm-address", s"${config.wmScheme}://${config.wmAddress}",
    "--wm-username", config.wmUsername,
    "--wm-password", config.wmPassword,
    "--mail-server-host", config.mailServerHost,
    "--mail-server-port", config.mailServerPort.toString,
    "--mail-server-user", config.mailServerUser,
    "--mail-server-password", config.mailServerPassword,
    "--mail-server-sender", config.mailServerSender,
    "--notebook-server-address", config.notebookServerAddress,
    "--datasource-server-address", config.datasourceManagerServerAddress,
    "--workflow-id", sessionConfig.workflowId.toString(),
    "--deps-zip", config.weDepsPath,
    "--user-id", sessionConfig.userId,
    "--temp-dir", config.tempDir,
    "--python-binary", config.pythonDriverBinary
  )

}
