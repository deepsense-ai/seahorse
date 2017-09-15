/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.models.workflows

import io.deepsense.commons.models
import io.deepsense.graph.Graph

case class Workflow(
    metadata: WorkflowMetadata,
    workflow: Graph,
    additionalData: ThirdPartyData)

object Workflow {
  type Id = models.Id
  val Id = models.Id
}
