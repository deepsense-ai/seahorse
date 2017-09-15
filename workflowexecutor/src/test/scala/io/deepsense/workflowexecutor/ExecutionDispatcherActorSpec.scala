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

import akka.actor.{ActorContext, ActorRef, ActorSelection, ActorSystem}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.scalatest.concurrent.{Eventually, ScalaFutures, ScaledTimeSpans}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import io.deepsense.deeplang._
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.models.workflows.Workflow
import io.deepsense.models.workflows.Workflow._
import io.deepsense.workflowexecutor.communication.message.workflow.Init
import io.deepsense.workflowexecutor.executor.PythonExecutionCaretaker


class ExecutionDispatcherActorSpec
  extends TestKit(ActorSystem("ExecutionDispatcherActorSpec"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with MockitoSugar
  with Eventually
  with ScalaFutures
  with ScaledTimeSpans {

  "ExecutionDispatcherActor" should {
    "create WorkflowExecutorActor" when {
      "received Init messages for non existing actor" in {
        val (workflowId, testProbe, dispatcher) = fixtureWithNonExistingExecutor
        val init: Init = Init(workflowId)
        val msg = InitWorkflow(init, testProbe.ref.path)
        dispatcher ! msg
        eventually {
          testProbe.expectMsg(WorkflowExecutorActor.Messages.Init())
        }
      }
    }
    "pass Init to WorkflowExecutorActor" in {
      val (workflowId, testProbe, dispatcher) = fixtureWithExistingExecutor
      val init: Init = Init(workflowId)
      val msg = InitWorkflow(init, testProbe.ref.path)
      dispatcher ! msg
      eventually {
        testProbe.expectMsg(WorkflowExecutorActor.Messages.Init())
      }
    }
  }

  def fixtureWithExistingExecutor:
      (Workflow.Id, TestProbe, TestActorRef[ExecutionDispatcherActor]) = {
    val expectedWorkflowId = Workflow.Id.randomId
    val testProbe = TestProbe()
    val dispatcher: TestActorRef[ExecutionDispatcherActor] =
      TestActorRef(new ExecutionDispatcherActor(
        mock[SparkContext],
        mock[SQLContext],
        mock[DOperableCatalog],
        mock[DataFrameStorage],
        mock[PythonExecutionCaretaker],
        TestProbe().ref,
        TestProbe().ref,
        TestProbe().ref,
        TestProbe().ref,
        5
      ) with WorkflowExecutorsFactory with WorkflowExecutorFinder {
        override def createExecutor(
            context: ActorContext,
            executionContext: CommonExecutionContext,
            workflowId: Id,
            workflowManagerClientActor: ActorRef,
            statusLogger: ActorRef,
            publisher: ActorSelection,
            seahorseTopicPublisher: ActorRef,
            wmTimeout: Int): ActorRef = {
          fail("Tried to create an executor but it exists!")
        }

        def findFor(context: ActorContext, workflowId: Workflow.Id): Option[ActorRef] =
          Some(testProbe.ref)
      })

    (expectedWorkflowId, testProbe, dispatcher)
  }

  def fixtureWithNonExistingExecutor:
  (Workflow.Id, TestProbe, TestActorRef[ExecutionDispatcherActor]) = {
    val expectedWorkflowId = Workflow.Id.randomId
    val testProbe = TestProbe()
    val dispatcher: TestActorRef[ExecutionDispatcherActor] =
      TestActorRef(new ExecutionDispatcherActorTest(
        mock[SparkContext],
        mock[SQLContext],
        mock[DOperableCatalog],
        mock[DataFrameStorage],
        mock[PythonExecutionCaretaker]
      ) with WorkflowExecutorsFactory with WorkflowExecutorFinder {
        override def createExecutor(
            context: ActorContext,
            executionContext: CommonExecutionContext,
            workflowId: Id,
            workflowManagerClientActor: ActorRef,
            statusLogger: ActorRef,
            publisher: ActorSelection,
            seahorseTopicPublisher: ActorRef,
            wmTimeout: Int): ActorRef = {
          workflowId shouldBe expectedWorkflowId
          testProbe.ref
        }

      def findFor(context: ActorContext, workflowId: Workflow.Id): Option[ActorRef] =
        None
    })

    (expectedWorkflowId, testProbe, dispatcher)
  }

  class ExecutionDispatcherActorTest(
      sparkContext: SparkContext,
      sqlContext: SQLContext,
      dOperableCatalog: DOperableCatalog,
      dataFrameStorage: DataFrameStorage,
      pythonExecutionCaretaker: PythonExecutionCaretaker)
    extends ExecutionDispatcherActor(
      sparkContext, sqlContext, dOperableCatalog, dataFrameStorage, pythonExecutionCaretaker,
      TestProbe().ref, TestProbe().ref, TestProbe().ref, TestProbe().ref, 5) {

    self: WorkflowExecutorsFactory with WorkflowExecutorFinder =>

    override def createExecutionContext(
        dataFrameStorage: DataFrameStorage,
        pythonExecutionCaretaker: PythonExecutionCaretaker,
        sparkContext: SparkContext,
        sqlContext: SQLContext,
        dOperableCatalog: Option[DOperableCatalog]): CommonExecutionContext = {
      mock[CommonExecutionContext]
    }
  }
}
