/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.models.experiments

import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import io.deepsense.deeplang.DOperation
import io.deepsense.graph.{Graph, Node}

class ExperimentSpec
  extends WordSpec
  with Matchers
  with MockitoSugar {
  "Experiment" should {
    "compute experiment state to be RUNNING" in {
      val op = mock[DOperation]
      val g = Graph(Set(
        Node(Node.Id.randomId, op).markQueued,
        Node(Node.Id.randomId, op).markRunning.withProgress(0).markCompleted(Seq.empty),
        Node(Node.Id.randomId, op).markRunning.withProgress(0).markCompleted(Seq.empty),
        Node(Node.Id.randomId, op).markRunning.withProgress(0).markCompleted(Seq.empty),
        Node(Node.Id.randomId, op).markQueued,
        Node(Node.Id.randomId, op).markQueued,
        Node(Node.Id.randomId, op).markQueued,
        Node(Node.Id.randomId, op).markRunning.withProgress(0).markCompleted(Seq.empty),
        Node(Node.Id.randomId, op).markQueued,
        Node(Node.Id.randomId, op).markRunning.withProgress(0).markCompleted(Seq.empty),
        Node(Node.Id.randomId, op).markRunning.withProgress(0).markCompleted(Seq.empty),
        Node(Node.Id.randomId, op).markRunning.withProgress(0).markCompleted(Seq.empty),
        Node(Node.Id.randomId, op).markQueued,
        Node(Node.Id.randomId, op).markRunning))
      Experiment.computeExperimentState(g) shouldBe Experiment.State.running
    }
  }
}
