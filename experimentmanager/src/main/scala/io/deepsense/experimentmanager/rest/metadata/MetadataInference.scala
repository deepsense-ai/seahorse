/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest.metadata

import io.deepsense.deeplang.DOperable.AbstractMetadata
import io.deepsense.deeplang.doperables.dataframe.{DataFrameMetadataJsonProtocol, DataFrameMetadata}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.{DOperable, DKnowledge}
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.graph.Node
import io.deepsense.models.experiments.Experiment
import spray.json._
import DataFrameMetadataJsonProtocol._

object MetadataInference {
  def run(
      experiment: Experiment,
      nodeId: Node.Id,
      portIndex: Int,
      baseContext: InferContext): Seq[Option[AbstractMetadata]] = {
    val inferContext = InferContext(baseContext, true)
    experiment.graph.inferKnowledge(nodeId, portIndex, inferContext).types.toList.map(
      (operable: DOperable) => operable.inferredMetadata)
  }
}
