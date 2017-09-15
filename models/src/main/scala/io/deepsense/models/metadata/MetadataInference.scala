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

package io.deepsense.models.metadata

import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.DOperable.AbstractMetadata
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.graph.GraphKnowledge.InferenceErrors
import io.deepsense.graph.{Graph, Node}

case class MetadataInferenceResult(
  metadata: Seq[Option[AbstractMetadata]],
  warnings: InferenceWarnings,
  errors: InferenceErrors)

object MetadataInference {
  def run(
      graph: Graph,
      nodeId: Node.Id,
      portIndex: Int,
      baseContext: InferContext): MetadataInferenceResult = {

    val inferContext = InferContext(baseContext, true)
    val singlePortInferenceResult = graph.inferKnowledge(nodeId, portIndex, inferContext)

    MetadataInferenceResult(
      singlePortInferenceResult.knowledge.types.toList.map(
        (operable: DOperable) => operable.inferredMetadata),
      singlePortInferenceResult.warnings,
      singlePortInferenceResult.errors)
  }
}
