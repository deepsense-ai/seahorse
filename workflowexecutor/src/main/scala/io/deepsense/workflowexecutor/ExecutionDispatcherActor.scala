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

package io.deepsense.workflowexecutor

import akka.actor._
import org.apache.spark.SparkContext

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang._
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.ReportLevel._
import io.deepsense.models.workflows.Workflow
import io.deepsense.models.workflows.Workflow.Id
import io.deepsense.workflowexecutor.communication.message.global.Ready
import io.deepsense.workflowexecutor.communication.message.workflow.Init
import io.deepsense.workflowexecutor.executor.{Executor, PythonExecutionCaretaker}

class ExecutionDispatcherActor(
    sparkContext: SparkContext,
    dOperableCatalog: DOperableCatalog,
    dataFrameStorage: DataFrameStorage,
    pythonExecutionCaretaker: PythonExecutionCaretaker,
    reportLevel: ReportLevel,
    statusLogger: ActorRef,
    workflowManagerClientActor: ActorRef,
    seahorseTopicPublisher: ActorRef,
    notebookTopicPublisher: ActorRef,
    wmTimeout: Int)
  extends Actor
  with Logging
  with Executor {

  self: WorkflowExecutorsFactory with WorkflowExecutorFinder =>

  override def receive: Receive = {
    case msg @ InitWorkflow(init @ Init(workflowId), publisherPath) =>
      val existingExecutor: Option[ActorRef] = findExecutor(workflowId)
      val publisher = context.actorSelection(publisherPath)
      val executor: ActorRef = existingExecutor.getOrElse(
        createExecutor(
          workflowId,
          publisher,
          seahorseTopicPublisher,
          workflowManagerClientActor))
      executor ! WorkflowExecutorActor.Messages.Init()
  }


  override def postRestart(reason: Throwable): Unit = {
    super.postRestart(reason)
    logger.warn("ExecutionDispatcherActor restarted.", reason)
    logger.debug("Sending Ready message to seahorse topic.")
    seahorseTopicPublisher ! Ready()
    notebookTopicPublisher ! Ready()
  }

  private def findExecutor(workflowId: Workflow.Id): Option[ActorRef] = {
    val executor = findFor(context, workflowId)
    val existsString = executor.map(_ => "exists").getOrElse("does not exist")
    logger.debug(s"The executor for '$workflowId' $existsString!")
    executor
  }

  def createExecutor(
    workflowId: Workflow.Id,
    publisher: ActorSelection,
    seahorseTopicPublisher: ActorRef,
    workflowManagerClientActor: ActorRef): ActorRef = {
    val executor = createExecutor(
      context,
      createExecutionContext(
        reportLevel,
        dataFrameStorage,
        pythonExecutionCaretaker,
        sparkContext,
        dOperableCatalog = Some(dOperableCatalog)),
      workflowId,
      workflowManagerClientActor,
      statusLogger,
      publisher,
      seahorseTopicPublisher,
      wmTimeout)
    logger.debug(s"Created an executor: '${executor.path}'")
    executor
  }
}

case class InitWorkflow(init: Init, publisherPath: ActorPath)

trait WorkflowExecutorsFactory {
  def createExecutor(
    context: ActorContext,
    executionContext: CommonExecutionContext,
    workflowId: Id,
    workflowManagerClientActor: ActorRef,
    statusLogger: ActorRef,
    publisher: ActorSelection,
    seahorseTopicPublisher: ActorRef,
    wmTimeout: Int): ActorRef
}

trait WorkflowExecutorsFactoryImpl extends WorkflowExecutorsFactory {
  override def createExecutor(
      context: ActorContext,
      executionContext: CommonExecutionContext,
      workflowId: Id,
      workflowManagerClientActor: ActorRef,
      statusLogger: ActorRef,
      publisher: ActorSelection,
      seahorseTopicPublisher: ActorRef,
      wmTimeout: Int): ActorRef = {
    context.actorOf(
      SessionWorkflowExecutorActor.props(
        executionContext,
        workflowManagerClientActor,
        publisher,
        seahorseTopicPublisher,
        wmTimeout,
        Some(statusLogger)),
      workflowId.toString)
  }
}

trait WorkflowExecutorFinder {
  def findFor(context: ActorContext, workflowId: Workflow.Id): Option[ActorRef]
}

trait ExecutorFinderImpl extends WorkflowExecutorFinder {
  def findFor(context: ActorContext, workflowId: Workflow.Id): Option[ActorRef] =
    context.child(workflowId.toString)
}

object ExecutionDispatcherActor {
  def props(
      sparkContext: SparkContext,
      dOperableCatalog: DOperableCatalog,
      dataFrameStorage: DataFrameStorage,
      pythonExecutionCaretaker: PythonExecutionCaretaker,
      reportLevel: ReportLevel,
      statusLogger: ActorRef,
      workflowManagerClientActor: ActorRef,
      seahorseTopicPublisher: ActorRef,
      notebookTopicPublisher: ActorRef,
      wmTimeout: Int): Props =
    Props(new ExecutionDispatcherActor(
      sparkContext,
      dOperableCatalog,
      dataFrameStorage,
      pythonExecutionCaretaker,
      reportLevel,
      statusLogger,
      workflowManagerClientActor,
      seahorseTopicPublisher,
      notebookTopicPublisher,
      wmTimeout
    ) with WorkflowExecutorsFactoryImpl
      with ExecutorFinderImpl)
}
