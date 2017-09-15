/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.executor

import ai.deepsense.sessionmanager.service.sessionspawner.SessionConfig
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherConfig

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
