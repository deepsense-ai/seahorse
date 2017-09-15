/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowexecutor

import java.net.URL

case class ExecutionParams(
    workflowFilename: Option[String] = None,
    outputDirectoryPath: Option[String] = None,
    extraVars: Map[String, String] = Map.empty,
    interactiveMode: Boolean = false,
    messageQueueHost: Option[String] = None,
    messageQueuePort: Option[Int] = None,
    messageQueueUser: Option[String] = None,
    messageQueuePass: Option[String] = None,
    customCodeExecutorsPath: Option[String] = None,
    pythonBinaryPath: Option[String] = None,
    workflowId: Option[String] = None,
    wmAddress: Option[String] = None,
    wmUsername: Option[String] = None,
    wmPassword: Option[String] = None,
    mailParams: MailParams = MailParams(),
    notebookServerAddress: Option[URL] = None,
    datasourceServerAddress: Option[URL] = None,
    depsZip: Option[String] = None,
    userId: Option[String] = None,
    tempPath: Option[String] = None)

// It's a separate case class because in Scala 2.10 case classes cannot have more than 22 params.
case class MailParams(
    mailServerHost: Option[String] = None,
    mailServerPort: Option[Int] = None,
    mailServerUser: Option[String] = None,
    mailServerPassword: Option[String] = None,
    mailServerSender: Option[String] = None)
