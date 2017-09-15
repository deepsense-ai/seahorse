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

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.Eventually
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import io.deepsense.deeplang.doperables.Report
import io.deepsense.deeplang.inference.InferenceWarnings
import io.deepsense.deeplang.{DKnowledge, DOperable, DOperation, ExecutionContext}
import io.deepsense.graph.Node
import io.deepsense.reportlib.model.ReportContent
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages.{NodeCompleted, NodeFailed, NodeStarted}
import io.deepsense.workflowexecutor.WorkflowNodeExecutorActor.Messages.Start

class WorkflowNodeExecutorActorSpec
  extends TestKit(ActorSystem("WorkflowNodeExecutorActorSpec"))
  with WordSpecLike
  with Matchers
  with MockitoSugar
  with BeforeAndAfter
  with BeforeAndAfterAll
  with Eventually {

  override protected def afterAll(): Unit = system.shutdown()

  "WorkflowNodeExecutorActor" when {
    "receives start" should {
      "infer knowledge and start execution of a node with correct parameters" in {
        val (probe, testedActor, node, operation, input) = fixutre()
        probe.send(testedActor, Start())
        probe.expectMsg(NodeStarted(node.id))

        eventually {
          verify(operation).inferKnowledge(any())(any())
          verify(operation).execute(any())(same(input))
        }
      }
    }
    "node completed" should {
      "respond NodeCompleted" in {
        val (probe, testedActor, node, output) = fixtureSucceedingOperation()
        probe.send(testedActor, Start())
        probe.expectMsg(NodeStarted(node.id))
        val nodeCompleted = probe.expectMsgType[NodeCompleted]
        nodeCompleted.id shouldBe node.id
        nodeCompleted.results.doperables.values should contain theSameElementsAs output
      }
    }
    "respond NodeFailed" when {
      "node failed" in {
        val (probe, testedActor, node, cause) = fixtureFailingOperation()
        probe.send(testedActor, Start())
        probe.expectMsg(NodeStarted(node.id))
        probe.expectMsgType[NodeFailed] shouldBe NodeFailed(node.id, cause)
      }
      "node's inference throws an exception" in {
        val (probe, testedActor, node, cause) = fixtureFailingInference()
        probe.send(testedActor, Start())
        probe.expectMsg(NodeStarted(node.id))
        probe.expectMsgType[NodeFailed] shouldBe NodeFailed(node.id, cause)
      }
    }
  }

  private def nodeExecutorActor(input: Vector[DOperable], node: Node): ActorRef = {
    system.actorOf(
      Props(new WorkflowNodeExecutorActor(executionContext, node, input)))
  }

  private def inferableOperable: DOperable = {
    val operable = mock[DOperable]
    when(operable.toInferrable).thenReturn(operable)
    operable
  }

  private def operableWithReports: DOperable = {
    val operable = mock[DOperable]
    val report = mock[Report]
    when(report.content).thenReturn(mock[ReportContent])
    when(operable.report(any())).thenReturn(report)
    operable
  }

  private def mockOperation: DOperation = {
    val dOperation = mock[DOperation]
    when(dOperation.name).thenReturn("mockedName")
    dOperation
  }

  private def fixtureFailingInference(): (TestProbe, ActorRef, Node, NullPointerException) = {
    val operation = mockOperation
    val cause = new NullPointerException("test exception")
    when(operation.inferKnowledge(any())(any()))
      .thenThrow(cause)
    val (probe, testedActor, node, _, _) = fixtureWithOperation(operation)
    (probe, testedActor, node, cause)
  }

  private def fixtureFailingOperation(): (TestProbe, ActorRef, Node, NullPointerException) = {
    val operation = mockOperation
    val cause = new NullPointerException("test exception")
    when(operation.execute(any[ExecutionContext]())(any[Vector[DOperable]]()))
      .thenThrow(cause)
    val (probe, testedActor, node, _, _) = fixtureWithOperation(operation)
    (probe, testedActor, node, cause)
  }

  private def fixtureSucceedingOperation(): (TestProbe, ActorRef, Node, Vector[DOperable]) = {
    val operation = mockOperation
    val output = Vector(operableWithReports, operableWithReports)
    when(operation.execute(any())(any()))
      .thenReturn(output)
    val (probe, testedActor, node, _, _) = fixtureWithOperation(operation)
    (probe, testedActor, node, output)
  }

  private def fixtureWithOperation(dOperation: DOperation):
      (TestProbe, ActorRef, Node, DOperation, Vector[DOperable]) = {
    val node = mock[Node]
    when(node.id).thenReturn(Node.Id.randomId)
    when(node.operation).thenReturn(dOperation)
    val probe = TestProbe()
    val input = Vector(inferableOperable, inferableOperable)
    val testedActor = nodeExecutorActor(input, node)
    (probe, testedActor, node, dOperation, input)
  }

  private def fixutre(): (TestProbe, ActorRef, Node, DOperation, Vector[DOperable]) = {
    val dOperation = mockOperation
    when(dOperation.inferKnowledge(any())(any()))
      .thenReturn((Vector[DKnowledge[DOperable]](), mock[InferenceWarnings]))
    when(dOperation.execute(any())(any()))
      .thenReturn(Vector())
    fixtureWithOperation(dOperation)
  }

  val executionContext = mock[ExecutionContext]
}
