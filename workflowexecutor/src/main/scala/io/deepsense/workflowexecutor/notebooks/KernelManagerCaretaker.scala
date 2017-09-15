/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.workflowexecutor.notebooks

import java.util.concurrent.atomic.AtomicReference

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.sys.process._

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import io.deepsense.commons.utils.Logging
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowexecutor.Unzip
import io.deepsense.workflowexecutor.communication.message.notebook.KernelManagerReady
import io.deepsense.workflowexecutor.communication.mq.MQCommunication
import io.deepsense.workflowexecutor.rabbitmq.MQCommunicationFactory

class KernelManagerCaretaker(
  private val actorSystem: ActorSystem,
  private val communicationFactory: MQCommunicationFactory,
  private val mqHost: String,
  private val mqPort: Int,
  private val sessionId: String,
  private val workflowId: Workflow.Id
) extends Logging {

  private val config = ConfigFactory.load.getConfig("kernelmanagercaretaker")
  private val pythonExecutable = config.getString("python-binary")
  private val archive: String = config.getString("archive")
  private val startupScript = config.getString("startup-script")
  private val startupTimeout: Duration = config.getInt("timeout").seconds
  private val startPromise: Promise[Unit] = Promise()
  private implicit val executionContext = actorSystem.dispatcher

  def start(): Unit = {
    sys.addShutdownHook {
      destroyPythonProcess()
    }

    val extractedKernelManagerPath = extractKernelManager()
    val process = runKernelManager(extractedKernelManagerPath)
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
    val startup = subscribe().flatMap { _ =>
      Future.firstCompletedOf(Seq(exited, startPromise.future))
    }
    logger.debug("startup initiated")
    Await.result(startup, startupTimeout)
    logger.debug("startup done")
  }

  private def extractKernelManager(): String = {
    logger.info("Extracting Kernel Manager...")
    val tempPath = Unzip.unzipAll(archive)
    s"$tempPath/$startupScript"
  }

  private def runKernelManager(kernelManagerPath: String): Process = {
    val command = s"$pythonExecutable $kernelManagerPath" +
      s" --mq-host $mqHost" +
      s" --mq-port $mqPort" +
      s" --workflow-id $workflowId" +
      s" --session-id $sessionId"
    val pyLogger = ProcessLogger(fout = logger.error, ferr = logger.error)
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
      subscriberActor)
  }

  private class KernelManagerSubscriber extends Actor with Logging {
    override def receive: Receive = {
      case KernelManagerReady() =>
        logger.debug("Received KernelManagerReady!")
        startPromise.success(Unit)
    }
  }
}
