package io.deepsense.models.workflows

import io.deepsense.graph.Graph

case class WorkflowWithVariables(
    metadata: WorkflowMetadata,
    graph: Graph,
    thirdPartyData: ThirdPartyData,
    variables: Variables)
