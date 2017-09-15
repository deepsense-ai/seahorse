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

import java.net.InetAddress
import java.util.concurrent.atomic.AtomicReference

import scala.annotation.tailrec
import scala.sys.process._

import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.{CustomOperationExecutor, DataFrameStorage, PythonCodeExecutor}
import io.deepsense.workflowexecutor.{ExecutionParams, Unzip}
import io.deepsense.workflowexecutor.pyspark.PythonPathGenerator
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
  pythonPathGenerator: PythonPathGenerator,
  pythonBinaryOverride: Option[String],
  val sparkContext: SparkContext,
  val sqlContext: SQLContext,
  val dataFrameStorage: DataFrameStorage,
  val hostAddress: InetAddress) extends Logging {

  val pythonBinary = {
    def pythonBinaryDefault = ConfigFactory.load.getString("pythoncaretaker.python-binary-default")
    pythonBinaryOverride.getOrElse(pythonBinaryDefault)
  }

  def waitForPythonExecutor(): Unit = {
    pythonGateway.codeExecutor
    pythonGateway.customOperationExecutor
  }

  def start(): Unit = {
    sys.addShutdownHook {
      pythonGateway.stop()
      destroyPyExecutorProcess()
    }

    pythonGateway.start()
    pyExecutorMonitorThread.start()

    try {
      waitForPythonExecutor()
    } catch {
      case e: Exception =>
        stop()
        throw e
    }
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
    PythonGateway(GatewayConfig(), sparkContext, sqlContext, dataFrameStorage, hostAddress)

  private val pyExecutorProcess = new AtomicReference[Option[Process]](None)

  private def extractPyExecutor(): String = {
    if (pythonExecutorPath.endsWith(".jar")) {
      val tempDir = Unzip.unzipToTmp(pythonExecutorPath, _.startsWith("pyexecutor/"))
      s"$tempDir/pyexecutor/pyexecutor.py"
    } else {
      pythonExecutorPath
    }
  }

  private def runPyExecutor(
      gatewayPort: Int,
      pythonExecutorPath: String): Process = {
    logger.info(s"Initializing PyExecutor from: $pythonExecutorPath")
    val command = s"$pythonBinary $pythonExecutorPath " +
      s"--gateway-address ${hostAddress.getHostAddress}:$gatewayPort"
    logger.info(s"Starting a new PyExecutor process: $command")

    val pyLogger = ProcessLogger(fout = logger.error, ferr = logger.error)
    val processBuilder: ProcessBuilder =
      Process(command, None, pythonPathGenerator.env())
    processBuilder.run(pyLogger)
  }

  /**
   * This thread starts PyExecutor in a loop as long
   * as gateway's listening port is available.
   */
  private val pyExecutorMonitorThread = new Thread(new Runnable {
    override def run(): Unit = {
      val extractedPythonExecutorPath = extractPyExecutor()

      @tailrec
      def go(): Unit = {
        pythonGateway.listeningPort match {
          case None =>
            logger.info("Listening port unavailable, not running PyExecutor")
          case Some(port) =>
            val process = runPyExecutor(port, extractedPythonExecutorPath)
            pyExecutorProcess.set(Some(process))
            val exitCode = process.exitValue()
            pyExecutorProcess.set(None)
            logger.info(s"PyExecutor exited with code $exitCode")

            Thread.sleep(250)
            go()
        }
      }

      go()
    }
  })

  private def destroyPyExecutorProcess(): Unit = pyExecutorProcess.get foreach { _.destroy() }
}
