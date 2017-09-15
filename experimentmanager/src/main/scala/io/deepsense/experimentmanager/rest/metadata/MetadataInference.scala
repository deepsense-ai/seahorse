/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest.metadata

import io.deepsense.deeplang.DOperable.AbstractMetadata
import io.deepsense.deeplang.doperables.dataframe.{DataFrameMetadataJsonProtocol, DataFrameMetadata}
import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}
import io.deepsense.deeplang.{DOperable, DKnowledge}
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.graph.GraphKnowledge.InferenceErrors
import io.deepsense.graph.Node
import io.deepsense.models.experiments.Experiment
import spray.json._
import DataFrameMetadataJsonProtocol._

case class MetadataInferenceResult(
  metadata: Seq[Option[AbstractMetadata]],
  warnings: InferenceWarnings,
  errors: InferenceErrors)

object MetadataInference {
  def run(
      experiment: Experiment,
      nodeId: Node.Id,
      portIndex: Int,
      baseContext: InferContext): MetadataInferenceResult = {

    val inferContext = InferContext(baseContext, true)
    val singlePortInferenceResult = experiment.graph.inferKnowledge(nodeId, portIndex, inferContext)

    MetadataInferenceResult(
      singlePortInferenceResult.knowledge.types.toList.map(
        (operable: DOperable) => operable.inferredMetadata),
      singlePortInferenceResult.warnings,
      singlePortInferenceResult.errors)
  }
}
