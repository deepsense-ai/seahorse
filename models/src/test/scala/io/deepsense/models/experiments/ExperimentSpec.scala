/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.models.experiments

import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.deeplang.DOperation
import io.deepsense.graph.{Graph, Node}
import io.deepsense.models.experiments.Experiment

class ExperimentSpec
  extends WordSpec
  with Matchers
  with MockitoSugar {

  "Experiment.computeExperimentState" should {
    "return Completed on empty graph" in {
      val experiment = newExperiment(Set.empty)
      experiment.updateState().state shouldBe Experiment.State.completed
    }
    "return Running on graph with at least one running node" in {
      val experiment = newExperiment(Set(
        newNode().markDraft,
        newNode().markFailed(mock[FailureDescription]),
        newNode().markRunning.withProgress(0).markCompleted(Seq.empty),
        newNode().markQueued,
        newNode().markRunning))
      experiment.updateState().state shouldBe Experiment.State.running
    }
    "return Draft if all nodes are in draft" in {
      val experiment = newExperiment(Set(
        newNode().markDraft,
        newNode().markDraft))
      experiment.updateState().state shouldBe Experiment.State.draft
    }
    "return appropriate status for graph" is pending
  }

  "Experiment" should {
    "mark itself and all not finished nodes as aborted" is pending
  }

  private def newNode(): Node = {
    val op = mock[DOperation]
    Node(Node.Id.randomId, op)
  }

  private def newExperiment(nodes: Set[Node]): Experiment = Experiment(
    id = Experiment.Id.randomId,
    name = "some name",
    tenantId = "some tenant",
    graph = Graph(nodes)
  )

}
