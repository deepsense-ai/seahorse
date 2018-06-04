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
import ai.deepsense.deeplang.{CatalogRecorder, DOperation, DeeplangIntegTestSupport, DeeplangTestSupport}
import ai.deepsense.graph.{DefaultKnowledgeService, Node, NodeInferenceImpl, NodeInferenceResult}

class InferKnowledgeIntegTest extends DeeplangIntegTestSupport with DeeplangTestSupport {
  val nodeInference = new NodeInferenceImpl{}

  val DCatalog(_, doplCatalog, dopsCatalog) = CatalogRecorder.resourcesCatalogRecorder.catalogs
  val inferCtx = createInferContext(doplCatalog)

  for (operation <- dopsCatalog.operations.values) {
    operation.name should {
      "not throw in inferKnowledge" in {
        val op = dopsCatalog.createDOperation(operation.id)
        val opNode = Node[DOperation](operation.id, op)
        val inputKnowledge = DefaultKnowledgeService.defaultInputKnowledge(doplCatalog, op)
        noException should be thrownBy
          nodeInference.inferKnowledge(opNode, inferCtx, NodeInferenceResult(inputKnowledge))
      }
    }
  }
}
