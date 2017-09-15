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

package io.deepsense.models.json.workflow.examples

import java.util.UUID

import spray.json._

import io.deepsense.deeplang.DOperation
import io.deepsense.graph.DeeplangGraph.DeeplangNode
import io.deepsense.graph.{DeeplangGraph, Edge, Node}
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.WorkflowWithVariablesJsonProtocol
import io.deepsense.models.workflows._

abstract class WorkflowCreator extends WorkflowWithVariablesJsonProtocol {

  val apiVersion: String = "0.4.0"

  protected def nodes: Seq[DeeplangNode]

  protected def edges: Seq[Edge]

  protected def experimentName: String

  protected def node(operation: DOperation): DeeplangNode = Node(UUID.randomUUID(), operation)

  override val graphReader: GraphReader = null

  def buildWorkflow(): WorkflowWithVariables = {
    val metadata = WorkflowMetadata(WorkflowType.Batch, apiVersion)
    val graph: DeeplangGraph = DeeplangGraph(nodes.toSet, edges.toSet)
    val thirdPartyData: ThirdPartyData = ThirdPartyData("{}")
    val variables: Variables = Variables()
    val result =
      WorkflowWithVariables(Workflow.Id.randomId, metadata, graph, thirdPartyData, variables)
    // scalastyle:off println
    println(result.toJson.prettyPrint)
    // scalastyle:on println
    result
  }
}
