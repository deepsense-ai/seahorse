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

import java.io._
import java.util.concurrent.atomic.AtomicReference
import java.util.zip.ZipInputStream

import scala.annotation.tailrec
import scala.sys.process._

import com.google.common.io.Files
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
    PythonGateway(GatewayConfig(), sparkContext, dataFrameStorage, dataFrameStorage)

  private val pyExecutorProcess = new AtomicReference[Option[Process]](None)

  private def extractPyExecutor(): String = {
    if (pythonExecutorPath.endsWith(".jar")) {
      val zis: ZipInputStream = new ZipInputStream(new FileInputStream(pythonExecutorPath))

      val tempDir = Files.createTempDir()
      logger.info("Created temporary directory for PyExecutor: " + tempDir.getAbsolutePath)

      var entry = zis.getNextEntry
      while (entry != null) {
        if (entry.getName.startsWith("pyexecutor/")) {
          val entryFilename = entry.getName.substring(entry.getName.lastIndexOf('/') + 1)
          val entryDirName = entry.getName.substring(0, entry.getName.lastIndexOf('/'))

          logger.debug(s"Entry found in jar file: " +
            s"directory: $entryDirName filename: $entryFilename isDirectory: " + entry.isDirectory)

          new File(tempDir + "/" + entryDirName).mkdirs()
          if (!entry.isDirectory) {
            val target = new File(tempDir + "/" + entryDirName, entryFilename)
            val fos = new BufferedOutputStream(new FileOutputStream(target, true))
            transferImpl(zis, fos, close = false)
          }
        }
        entry = zis.getNextEntry
      }
      zis.close()

      s"$tempDir/pyexecutor/pyexecutor.py"
    } else {
      pythonExecutorPath
    }
  }

  private def transferImpl(in: InputStream, out: OutputStream, close: Boolean): Unit = {
    try {
      val buffer = new Array[Byte](4096)
      def read(): Unit = {
        val byteCount = in.read(buffer)
        if (byteCount >= 0) {
          out.write(buffer, 0, byteCount)
          read()
        }
      }
      read()
      out.close()
    }
    finally {
      if (close) {
        in.close()
      }
    }
  }

  private def runPyExecutor(gatewayPort: Int, pythonExecutable: String): Process = {
    logger.info(s"Initializing PyExecutor from: $pythonExecutorPath")
    val command =
      s"$PythonExecutable $pythonExecutable --gateway-address localhost:$gatewayPort"
    logger.info(s"Starting a new PyExecutor process: $command")

    val pyLogger = ProcessLogger(fout = logger.debug, ferr = logger.error)
    command run pyLogger
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
