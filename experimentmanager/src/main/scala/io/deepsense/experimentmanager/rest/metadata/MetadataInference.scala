/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest.metadata

import io.deepsense.deeplang.DOperable.{MetadataVisitor, AbstractMetadata}
import io.deepsense.deeplang.doperables.dataframe.DataFrameMetadata
import io.deepsense.deeplang.{DOperable, DKnowledge, InferContext}
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.graph.Node
import io.deepsense.models.experiments.Experiment
import spray.json._
import io.deepsense.experimentmanager.rest.json.DataFrameMetadataJsonProtocol._

object MetadataInference {
  def run(
      experiment: Experiment,
      nodeId: Node.Id,
      portIndex: Int,
      baseContext: InferContext): Seq[Option[AbstractMetadata]] = {
    experiment.graph.inferKnowledge(
      nodeId,
      portIndex,
      new InferContext(baseContext.dOperableCatalog, true)).types.toList.map(
        (operable: DOperable) => operable.inferredMetadata)
  }
}
