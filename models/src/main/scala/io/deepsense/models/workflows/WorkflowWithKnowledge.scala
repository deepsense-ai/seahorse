package io.deepsense.models.workflows

import io.deepsense.graph.{Graph, GraphKnowledge}

case class WorkflowWithKnowledge(
    id: Workflow.Id,
    metadata: WorkflowMetadata,
    workflow: Graph,
    thirdPartyData: ThirdPartyData,
    knowledge: GraphKnowledge)
