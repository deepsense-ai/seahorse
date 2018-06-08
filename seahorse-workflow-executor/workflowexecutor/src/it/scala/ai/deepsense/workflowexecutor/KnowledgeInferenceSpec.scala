/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowexecutor

import ai.deepsense.deeplang.catalogs.DCatalog
import ai.deepsense.deeplang.doperations.custom.{Sink, Source}
import ai.deepsense.deeplang.doperations.CreateCustomTransformer
import ai.deepsense.deeplang.inference.InferContext
import ai.deepsense.deeplang.inference.exceptions.NoInputEdgesException
import ai.deepsense.deeplang.params.custom.InnerWorkflow
import ai.deepsense.deeplang.{CatalogRecorder, DOperation, MockedInferContext}
import ai.deepsense.graph.{AbstractInferenceSpec, DeeplangGraph, GraphKnowledge, Node}
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.json.workflow.InnerWorkflowJsonProtocol

class KnowledgeInferenceSpec
  extends AbstractInferenceSpec
    with InnerWorkflowJsonProtocol {

  import spray.json._

  "Graph" should {
    "infer knowledge for nested nodes (ex. CreateCustomTransformer)" in {
      val rootWorkflowWithSomeInnerWorkflow = {
        val createCustomTransformer = operationCatalog.createDOperation(
          CreateCustomTransformer.id
        ).asInstanceOf[CreateCustomTransformer]

        createCustomTransformer.setInnerWorkflow(CreateCustomTransformer.default)
        DeeplangGraph(Set(createCustomTransformer.toNode()))
      }

      val inferenceResult = rootWorkflowWithSomeInnerWorkflow.inferKnowledge(
        inferContext, GraphKnowledge()
      )

      // There is 1 node in root workflow and two more in inner workflow.
      inferenceResult.results.size should be > 1
    }
  }

  "Node errors" should {
    "be properly inferred for inner workflow. For example" when {
      "sink node has no input connected " in {
        val sinkExpectedToHaveErrors = operationCatalog.createDOperation(Sink.id).toNode()
        val rootWorkflowWithInvalidInnerWorkflow = {
          val createCustomTransformer = operationCatalog.createDOperation(
            CreateCustomTransformer.id
          ).asInstanceOf[CreateCustomTransformer]

          val innerWorkflow = {
            val source = operationCatalog.createDOperation(Source.id).toNode()
            val graph = DeeplangGraph(Set(source, sinkExpectedToHaveErrors), Set.empty)
            InnerWorkflow(graph, JsObject(), List.empty)
          }
          createCustomTransformer.setInnerWorkflow(innerWorkflow)
          DeeplangGraph(Set(createCustomTransformer.toNode()), Set.empty)
        }

        val inferenceResult = rootWorkflowWithInvalidInnerWorkflow.inferKnowledge(
          inferContext, GraphKnowledge()
        )

        inferenceResult.errors(sinkExpectedToHaveErrors.id).head should matchPattern {
          case NoInputEdgesException(0) =>
        }
      }
    }
  }

  implicit class DOperationTestExtension(val dOperation: DOperation) {
    def toNode(): Node[DOperation] = Node(Node.Id.randomId, dOperation)
  }

  private lazy val inferContext: InferContext = {
    MockedInferContext(dOperableCatalog = dOperableCatalog)
  }
  override protected lazy val graphReader = new GraphReader(operationCatalog)
  private lazy val DCatalog(_, dOperableCatalog, operationCatalog) =
    CatalogRecorder.resourcesCatalogRecorder.catalogs
}
