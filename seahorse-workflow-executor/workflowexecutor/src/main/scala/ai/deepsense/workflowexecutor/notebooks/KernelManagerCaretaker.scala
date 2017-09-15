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

package ai.deepsense.workflowexecutor.notebooks

import java.util.concurrent.atomic.AtomicReference

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise, TimeoutException}
import scala.sys.process._

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import ai.deepsense.commons.utils.Logging
import ai.deepsense.models.workflows.Workflow
import ai.deepsense.workflowexecutor.rabbitmq.MQCommunicationFactory
import ai.deepsense.workflowexecutor.communication.message.notebook.KernelManagerReady
import ai.deepsense.workflowexecutor.communication.mq.MQCommunication
import ai.deepsense.workflowexecutor.pyspark.PythonPathGenerator

class KernelManagerCaretaker(
  private val actorSystem: ActorSystem,
  private val pythonBinary: String,
  private val pythonPathGenerator: PythonPathGenerator,
  private val communicationFactory: MQCommunicationFactory,
  private val kernelManagerPath: String,
  private val gatewayHost: String,
  private val gatewayPort: Int,
  private val rBackendHost: String,
  private val rBackendPort: Int,
  private val mqHost: String,
  private val mqPort: Int,
  private val mqUser: String,
  private val mqPass: String,
  private val sessionId: String,
  private val workflowId: Workflow.Id
) extends Logging {

  private val config = ConfigFactory.load.getConfig("kernelmanagercaretaker")
  private val startupScript = config.getString("startup-script")
  private val startupTimeout: Duration = config.getInt("timeout").seconds
  private val startPromise: Promise[Unit] = Promise()
  private implicit val executionContext = actorSystem.dispatcher

  def start(): Unit = {
    sys.addShutdownHook {
      destroyPythonProcess()
    }

    val extractedKernelManagerPath = s"$kernelManagerPath/$startupScript"
    val process = runKernelManager(extractedKernelManagerPath, kernelManagerPath)
    val exited = Future(process.exitValue()).map { code =>
      startPromise.failure(
        new RuntimeException(s"Kernel Manager finished prematurely (with exit code $code)!"))
      ()
    }
    kernelManagerProcess.set(Some(process))

    try {
      waitForKernelManager(exited)
    } catch {
      case e: Exception =>
        stop()
        throw e
    }
  }

  def stop(): Unit = {
    destroyPythonProcess()
  }

  private def waitForKernelManager(exited: Future[Unit]): Unit = {
    val startup = subscribe().flatMap { _ => startPromise.future }
    logger.debug("startup initiated")
    try {
      Await.result(startup, startupTimeout)
      logger.debug("startup done")
    } catch {
      case t: TimeoutException =>
        throw new RuntimeException(s"Kernel Manager did not start after $startupTimeout")
    }
  }

  private def runKernelManager(kernelManagerPath: String, workingDir: String): Process = {
    val pyLogger = ProcessLogger(fout = logger.info, ferr = logger.error)

    val chmod = s"chmod +x $kernelManagerPath"
    logger.info(s"Setting +x for $kernelManagerPath")
    chmod.run(pyLogger).exitValue()

    val command = s"$kernelManagerPath" +
      s" --working-dir $workingDir" +
      s" --python-binary $pythonBinary" +
      s" --additional-python-path ${pythonPathGenerator.pythonPath()}" +
      s" --gateway-host $gatewayHost" +
      s" --gateway-port $gatewayPort" +
      s" --r-backend-host $rBackendHost" +
      s" --r-backend-port $rBackendPort" +
      s" --mq-host $mqHost" +
      s" --mq-port $mqPort" +
      s" --mq-user $mqUser" +
      s" --mq-pass $mqPass" +
      s" --workflow-id $workflowId" +
      s" --session-id $sessionId"
    logger.info(s"Starting a new Kernel Manager process: $command")
    command.run(pyLogger)
  }

  private val kernelManagerProcess = new AtomicReference[Option[Process]](None)
  private def destroyPythonProcess(): Unit = kernelManagerProcess.get foreach { _.destroy() }


  def subscribe(): Future[Unit] = {
    val props = Props(new KernelManagerSubscriber)
    val subscriberActor = actorSystem.actorOf(props, "KernelManagerSubscriber")
    communicationFactory.registerSubscriber(
      MQCommunication.Topic.kernelManagerSubscriptionTopic(workflowId, sessionId),
      subscriberActor).map(_ => ())
  }

  private class KernelManagerSubscriber extends Actor with Logging {
    override def receive: Receive = {
      case KernelManagerReady() =>
        logger.debug("Received KernelManagerReady!")
        startPromise.success(Unit)
    }
  }
}
