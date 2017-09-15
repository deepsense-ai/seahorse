/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import io.deepsense.deeplang.inference.InferenceWarnings
import io.deepsense.deeplang.{DKnowledge, DOperable, DOperation, ExecutionContext}
import io.deepsense.graph.{Endpoint, Graph, Node}
import io.deepsense.graphexecutor.GraphExecutorActor.Messages.{NodeFinished, NodeStarted}
import io.deepsense.graphexecutor.GraphNodeExecutorActor.Messages.Start
import io.deepsense.models.entities.Entity
import io.deepsense.models.experiments.Experiment

class GraphNodeExecutorActorSpec
  extends TestKit(ActorSystem("GraphNodeExecutorActorSpec"))
  with WordSpecLike
  with Matchers
  with MockitoSugar
  with BeforeAndAfterAll
  with ImplicitSender {

  override protected def afterAll(): Unit = {
    system.shutdown()
  }

  // TODO increase coverage of these tests: https://codilime.atlassian.net/browse/DS-823
  "GraphNodeExecutorActor" should {
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

        val mExperiment = mock[Experiment]
        val mGraph = mock[Graph]
        when(mExperiment.graph).thenReturn(mGraph)
        val predecessors: Map[Node.Id, IndexedSeq[Option[Endpoint]]] = Map(node.id -> Vector())
        when(mGraph.predecessors).thenReturn(predecessors)
        val dOperationCache = Map.empty[Entity.Id, DOperable]

        val gnea = system.actorOf(
          Props(new GraphNodeExecutorActor(mEC, node, mExperiment, dOperationCache)))
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

        val mExperiment = mock[Experiment]
        val mGraph = mock[Graph]
        when(mExperiment.graph).thenReturn(mGraph)
        val predecessors: Map[Node.Id, IndexedSeq[Option[Endpoint]]] = Map(node.id -> Vector())
        when(mGraph.predecessors).thenReturn(predecessors)
        val dOperationCache = Map.empty[Entity.Id, DOperable]

        val gnea = system.actorOf(
          Props(new GraphNodeExecutorActor(mEC, node, mExperiment, dOperationCache)))
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

        val mExperiment = mock[Experiment]
        val mGraph = mock[Graph]
        when(mExperiment.graph).thenReturn(mGraph)
        val predecessors: Map[Node.Id, IndexedSeq[Option[Endpoint]]] = Map(node.id -> Vector())
        when(mGraph.predecessors).thenReturn(predecessors)
        val dOperationCache = Map.empty[Entity.Id, DOperable]

        val gnea = system.actorOf(
          Props(new GraphNodeExecutorActor(mEC, node, mExperiment, dOperationCache)))
        gnea ! Start()

        expectMsgType[NodeStarted] shouldBe NodeStarted(node.id)
        expectMsgType[NodeFinished].node shouldBe 'Failed
      }
    }
  }
}
