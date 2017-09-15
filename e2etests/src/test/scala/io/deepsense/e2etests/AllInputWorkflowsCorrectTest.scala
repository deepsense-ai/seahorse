/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.e2etests

import org.scalatest.WordSpec
import spray.json._

import io.deepsense.commons.utils.{Logging, Version}
import io.deepsense.deeplang.CatalogRecorder
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.WorkflowVersionUtil
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.rest.CurrentBuild


class AllInputWorkflowsCorrectTest extends WordSpec {

  TestWorkflowsIterator.foreach { case TestWorkflowsIterator.Input(path, fileContents) =>
    s"Workflow from '$path'" should {
      "be correctly formatted" in {
        WorkflowParser.parseWorkflow(fileContents)
      }
    }
  }
}

object WorkflowParser extends Logging with WorkflowVersionUtil {
  override def currentVersion: Version = CurrentBuild.version

  override val graphReader: GraphReader = {
    new GraphReader(CatalogRecorder.catalogs.dOperationsCatalog)
  }

  def parseWorkflow(raw: String): Unit = {
    raw.parseJson.convertTo[Workflow](versionedWorkflowReader)
  }
}
