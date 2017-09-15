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

package io.deepsense.models.workflows

import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import io.deepsense.deeplang.DOperation
import io.deepsense.graph.{Graph, Node}

class WorkflowSpec
  extends WordSpec
  with Matchers
  with MockitoSugar {

  "Experiment.computeExperimentState" should {
    "return Completed on empty graph" in {
      val experiment = newExperiment(Set.empty)
      experiment.updateState().state shouldBe Workflow.State.completed
    }
    "return Running on graph with at least one running node" is pending
    "return Draft if all nodes are in draft" in {
      val experiment = newExperiment(Set(
        newNode().markDraft,
        newNode().markDraft))
      experiment.updateState().state shouldBe Workflow.State.draft
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

  private def newExperiment(nodes: Set[Node]): Workflow = Workflow(
    id = Workflow.Id.randomId,
    name = "some name",
    tenantId = "some tenant",
    graph = Graph(nodes)
  )

}
