/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.e2etests

import spray.json._

import io.deepsense.commons.utils.{Logging, Version}
import io.deepsense.deeplang.CatalogRecorder
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.WorkflowVersionUtil
import io.deepsense.models.workflows.WorkflowWithVariables
import io.deepsense.workflowmanager.rest.CurrentBuild

object WorkflowParser extends Logging with WorkflowVersionUtil {
  override def currentVersion: Version = CurrentBuild.version

  override val graphReader: GraphReader = {
    new GraphReader(CatalogRecorder.catalogs.dOperationsCatalog)
  }

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
