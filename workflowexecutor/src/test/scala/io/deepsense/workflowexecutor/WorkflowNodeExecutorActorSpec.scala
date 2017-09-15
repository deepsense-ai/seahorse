/**
 * Copyright 2015, CodiLime Inc.
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

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import io.deepsense.deeplang.inference.InferenceWarnings
import io.deepsense.deeplang.{DKnowledge, DOperable, DOperation, ExecutionContext}
import io.deepsense.graph.{Endpoint, Graph, Node}
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages.{NodeFinished, NodeStarted}
import io.deepsense.workflowexecutor.WorkflowNodeExecutorActor.Messages.Start
import io.deepsense.models.entities.Entity
import io.deepsense.models.workflows.Workflow

class WorkflowNodeExecutorActorSpec
  extends TestKit(ActorSystem("WorkflowNodeExecutorActorSpec"))
  with WordSpecLike
  with Matchers
  with MockitoSugar
  with BeforeAndAfterAll
  with ImplicitSender {

  override protected def afterAll(): Unit = {
    system.shutdown()
  }

  // TODO increase coverage of these tests: https://codilime.atlassian.net/browse/DS-823
  "WorkflowNodeExecutorActor" should {
    "start execution of Nodes and respond NodeStarted (and NodeFinished)" when {
      "receives Start" in {
        val mEC = mock[ExecutionContext]

        val mDOp = mock[DOperation]
        when(mDOp.name).thenReturn("mockedName")
        when(mDOp.execute(any[ExecutionContext])(any[Vector[DOperable]]))
          .thenReturn(Vector[DOperable]())
        when(mDOp.inferKnowledge(any())(any()))
          .thenReturn((Vector[DKnowledge[DOperable]](), mock[InferenceWarnings]))

        // FIXME node in DRAFT state must not be Completed -> None.get exception
        val node = Node(Node.Id.randomId, mDOp).markRunning

        val mGraph = mock[Graph]
        val predecessors: Map[Node.Id, IndexedSeq[Option[Endpoint]]] = Map(node.id -> Vector())
        when(mGraph.predecessors).thenReturn(predecessors)
        val dOperationCache = Map.empty[Entity.Id, DOperable]

        val gnea = system.actorOf(
          Props(new WorkflowNodeExecutorActor(mEC, node, mGraph, dOperationCache)))
        gnea ! Start()

        expectMsgType[NodeStarted] shouldBe NodeStarted(node.id)
        expectMsgType[NodeFinished].node shouldBe 'Completed
      }
    }
    "respond NodeFinished with appropriate failure description" when {
      "its Node throws an exception" in {
        val mEC = mock[ExecutionContext]

        val mDOp = mock[DOperation]
        when(mDOp.name).thenReturn("mockedName")
        when(mDOp.inferKnowledge(any())(any()))
          .thenReturn((Vector[DKnowledge[DOperable]](), mock[InferenceWarnings]))
        when(mDOp.execute(any[ExecutionContext])(any[Vector[DOperable]]))
          .thenThrow(new RuntimeException("Exception from mock"))

        // FIXME node in DRAFT state must not be Completed -> None.get exception
        val node = Node(Node.Id.randomId, mDOp).markRunning

        val mExperiment = mock[Workflow]
        val mGraph = mock[Graph]
        val predecessors: Map[Node.Id, IndexedSeq[Option[Endpoint]]] = Map(node.id -> Vector())
        when(mGraph.predecessors).thenReturn(predecessors)
        val dOperationCache = Map.empty[Entity.Id, DOperable]

        val gnea = system.actorOf(
          Props(new WorkflowNodeExecutorActor(mEC, node, mGraph, dOperationCache)))
        gnea ! Start()

        expectMsgType[NodeStarted] shouldBe NodeStarted(node.id)
        expectMsgType[NodeFinished].node shouldBe 'Failed
      }
      "its Node inference throws an exception" in {
        val mEC = mock[ExecutionContext]

        val mDOp = mock[DOperation]
        when(mDOp.name).thenReturn("mockedName")
        when(mDOp.inferKnowledge(any())(any()))
          .thenThrow(new RuntimeException("Inference exception from mock"))

        // FIXME node in DRAFT state must not be Completed -> None.get exception
        val node = Node(Node.Id.randomId, mDOp).markRunning

        val mExperiment = mock[Workflow]
        val mGraph = mock[Graph]
        val predecessors: Map[Node.Id, IndexedSeq[Option[Endpoint]]] = Map(node.id -> Vector())
        when(mGraph.predecessors).thenReturn(predecessors)
        val dOperationCache = Map.empty[Entity.Id, DOperable]

        val gnea = system.actorOf(
          Props(new WorkflowNodeExecutorActor(mEC, node, mGraph, dOperationCache)))
        gnea ! Start()

        expectMsgType[NodeStarted] shouldBe NodeStarted(node.id)
        expectMsgType[NodeFinished].node shouldBe 'Failed
      }
    }
  }
}
