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

package ai.deepsense.workflowexecutor.executor

import org.apache.spark.api.r.SparkRBackend

import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang.CustomCodeExecutor
import ai.deepsense.workflowexecutor.Unzip
import ai.deepsense.workflowexecutor.customcode.CustomCodeEntryPoint

class RExecutionCaretaker(rExecutorPath: String,
                          customCodeEntryPoint: CustomCodeEntryPoint) extends Logging {

  private val backend = new SparkRBackend()

  def backendListeningPort: Int = backend.port

  def rCodeExecutor: CustomCodeExecutor = new RExecutor(
    backend.port, backend.entryPointId, customCodeEntryPoint, extractRExecutor())

  def start(): Unit = backend.start(customCodeEntryPoint)

  private def extractRExecutor(): String = {
    if (rExecutorPath.endsWith(".jar")) {
      val tempDir = Unzip.unzipToTmp(rExecutorPath, _.equals("r_executor.R"))
      s"$tempDir/r_executor.R"
    } else {
      rExecutorPath
    }
  }
}
