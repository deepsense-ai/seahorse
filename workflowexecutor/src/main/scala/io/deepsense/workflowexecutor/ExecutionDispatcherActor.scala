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
import io.deepsense.deeplang.{CommonExecutionContext, DataFrameStorage, ExecutionContext}
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.ReportLevel._
import io.deepsense.models.workflows.Workflow
import io.deepsense.models.workflows.Workflow.Id
import io.deepsense.workflowexecutor.communication.Connect
import io.deepsense.workflowexecutor.executor.Executor
import io.deepsense.workflowexecutor.rabbitmq.WorkflowConnect

class ExecutionDispatcherActor(
    sparkContext: SparkContext,
    dOperableCatalog: DOperableCatalog,
    dataFrameStorage: DataFrameStorage,
    reportLevel: ReportLevel,
    statusLogger: ActorRef)
  extends Actor
  with Logging
  with Executor {

  self: WorkflowExecutorsFactory with WorkflowExecutorFinder =>

  // TODO handle child's exceptions

  override def receive: Receive = {
    case msg @ WorkflowConnect(connect @ Connect(workflowId), publisherPath) =>
      logger.debug(s"Received $msg")
      val existingExecutor: Option[ActorRef] = findExecutor(workflowId)
      val publisher = context.actorSelection(publisherPath)
      val executor: ActorRef = existingExecutor.getOrElse(createExecutor(workflowId, publisher))
      executor.forward(connect)
  }

  private def findExecutor(workflowId: Workflow.Id): Option[ActorRef] = {
    val executor = findFor(context, workflowId)
    val existsString = executor.map(_ => "exists").getOrElse("does not exist")
    logger.debug(s"The executor for '$workflowId' $existsString!")
    executor
  }

  def createExecutor(workflowId: Workflow.Id, publisher: ActorSelection): ActorRef = {
    val executor = createExecutor(
      context,
      createExecutionContext(
        reportLevel,
        dataFrameStorage,
        sparkContext = Some(sparkContext),
        dOperableCatalog = Some(dOperableCatalog)),
      workflowId,
      statusLogger,
      publisher)
    logger.debug(s"Created an executor: '${executor.path}'")
    executor
  }
}

trait WorkflowExecutorsFactory {
  def createExecutor(
    context: ActorContext,
    executionContext: CommonExecutionContext,
    workflowId: Id,
    statusLogger: ActorRef,
    publisher: ActorSelection): ActorRef
}

trait WorkflowExecutorsFactoryImpl extends WorkflowExecutorsFactory {
  override def createExecutor(
      context: ActorContext,
      executionContext: CommonExecutionContext,
      workflowId: Id,
      statusLogger: ActorRef,
      publisher: ActorSelection): ActorRef = {
    context.actorOf(
      WorkflowExecutorActor.props(executionContext, Some(publisher), Some(statusLogger)),
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
      reportLevel: ReportLevel,
      statusLogger: ActorRef): Props =
    Props(new ExecutionDispatcherActor(
      sparkContext,
      dOperableCatalog,
      dataFrameStorage,
      reportLevel,
      statusLogger
    ) with WorkflowExecutorsFactoryImpl
      with ExecutorFinderImpl)
}
