/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.e2etests

import spray.json._

import io.deepsense.commons.utils.{Logging, Version}
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.WorkflowVersionUtil
import io.deepsense.models.workflows.WorkflowWithVariables
import io.deepsense.workflowmanager.rest.CurrentBuild

class WorkflowJsonConverter(override val graphReader: GraphReader)
    extends Logging
    with WorkflowVersionUtil {
  override def currentVersion: Version = CurrentBuild.version

  def parseWorkflow(raw: String): WorkflowWithVariables = {
    raw.parseJson.convertTo[WorkflowWithVariables](versionedWorkflowWithVariablesReader)
  }

  def printWorkflow(
      workflowWithVariables: WorkflowWithVariables,
      prettyPrint: Boolean = false): String = {
    val json = workflowWithVariables.toJson
    if (prettyPrint) {
      json.prettyPrint
    } else {
      json.compactPrint
    }
  }
}
