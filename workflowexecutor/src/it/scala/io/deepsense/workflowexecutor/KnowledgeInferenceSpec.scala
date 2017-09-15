/**
  * Copyright 2015, deepsense.io
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package io.deepsense.workflowexecutor

import io.deepsense.deeplang.catalogs.CatalogPair
import io.deepsense.deeplang.doperations.custom.{Sink, Source}
import io.deepsense.deeplang.doperations.{CreateCustomTransformer, DefaultCustomTransformerWorkflow}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.inference.exceptions.NoInputEdgesException
import io.deepsense.deeplang.params.custom.InnerWorkflow
import io.deepsense.deeplang.{CatalogRecorder, DOperation, InnerWorkflowExecutor}
import io.deepsense.graph.{AbstractInferenceSpec, DeeplangGraph, GraphKnowledge, Node}
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.InnerWorkflowJsonProtocol
import io.deepsense.workflowexecutor.executor.InnerWorkflowExecutorImpl

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
        createCustomTransformer.setInnerWorkflow(DefaultCustomTransformerWorkflow.defaultWorkflow)
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
          createCustomTransformer.setInnerWorkflow(innerWorkflow.toJson.asJsObject)
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
    val executor: InnerWorkflowExecutor = new InnerWorkflowExecutorImpl(graphReader)
    InferContext(null, null, dOperableCatalog, executor)
  }
  override protected lazy val graphReader = new GraphReader(operationCatalog)
  private lazy val CatalogPair(dOperableCatalog, operationCatalog) = CatalogRecorder.createCatalogs()
}
