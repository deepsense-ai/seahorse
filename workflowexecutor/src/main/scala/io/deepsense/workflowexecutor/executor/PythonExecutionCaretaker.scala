/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.workflowexecutor.executor

import java.util.concurrent.atomic.AtomicReference

import scala.annotation.tailrec
import scala.sys.process._

import org.apache.spark.SparkContext

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.{CustomOperationExecutor, DataFrameStorage, PythonCodeExecutor}
import io.deepsense.workflowexecutor.pythongateway.PythonGateway
import io.deepsense.workflowexecutor.pythongateway.PythonGateway.GatewayConfig


/**
 * This object is responsible for a companion Python process, that executes
 * user-defined custom operations.
 *
 * It starts PyExecutor and takes care that it's restarted if it dies for any reason.
 * Also, PyExecutor is killed when the JVM process dies.
 *
 * Another of its functions is to provide a facade for everything Python/UDF-related.
 */
class PythonExecutionCaretaker(
  pythonExecutorPath: String,
  val sparkContext: SparkContext,
  val dataFrameStorage: DataFrameStorage) extends Logging {

  import PythonExecutionCaretaker._

  def start(): Unit = {
    sys.addShutdownHook {
      pythonGateway.stop()
      destroyPyExecutorProcess()
    }

    pythonGateway.start()
    pyExecutorMonitorThread.start()
  }

  def stop(): Unit = {
    pythonGateway.stop()
    destroyPyExecutorProcess()
    pyExecutorMonitorThread.join()
  }

  def pythonCodeExecutor: PythonCodeExecutor = pythonGateway.codeExecutor

  def customOperationExecutor: CustomOperationExecutor = pythonGateway.customOperationExecutor

  def gatewayListeningPort: Option[Int] = pythonGateway.listeningPort

  private val pythonGateway =
    PythonGateway(GatewayConfig(), sparkContext, dataFrameStorage, dataFrameStorage)

  private val pyExecutorProcess = new AtomicReference[Option[Process]]

  private def runPyExecutor(gatewayPort: Int): Process = {
    val pyLogger = ProcessLogger(fout = logger.debug, ferr = logger.error)
    val command = s"$PythonExecutable $pythonExecutorPath --gateway-address localhost:$gatewayPort"
    logger.info(s"Starting a new PyExecutor process: $command")
    command run pyLogger
  }

  /**
   * This thread starts PyExecutor in a loop as long
   * as gateway's listening port is available.
   */
  private val pyExecutorMonitorThread = new Thread(new Runnable {
    override def run(): Unit = {
      @tailrec
      def go(): Unit = {
        pythonGateway.listeningPort match {
          case None =>
            logger.info("Listening port unavailable, not running PyExecutor")
          case Some(port) =>
            val process = runPyExecutor(port)
            pyExecutorProcess.set(Some(process))
            val exitCode = process.exitValue()
            pyExecutorProcess.set(None)
            logger.info(s"PyExecutor exited with code $exitCode")

            go()
        }
      }

      go()
    }
  })

  private def destroyPyExecutorProcess(): Unit = pyExecutorProcess.get foreach { _.destroy() }
}

object PythonExecutionCaretaker {
  val PythonExecutable = "python"
}
