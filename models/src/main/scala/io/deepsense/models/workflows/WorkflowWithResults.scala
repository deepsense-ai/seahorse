package io.deepsense.models.workflows

import io.deepsense.graph.Graph

case class WorkflowWithResults(
    metadata: WorkflowMetadata,
    graph: Graph,
    thirdPartyData: ThirdPartyData,
    executionReport: ExecutionReport)
