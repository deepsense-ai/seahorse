/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import com.datastax.driver.core.Row
import org.mockito.Matchers._
import org.mockito.Mockito._
import spray.json._

import io.deepsense.commons.utils.Version
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.graph.DeeplangGraph
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.WorkflowJsonProtocol
import io.deepsense.models.workflows._
import io.deepsense.workflowmanager.rest.CurrentBuild

class WorkflowRowMapperSpec
  extends StandardSpec
  with UnitTestSupport
  with WorkflowJsonProtocol {

  override val graphReader: GraphReader = mock[GraphReader]
  when(graphReader.read(any())).thenReturn(DeeplangGraph())
  val currentVersion = CurrentBuild.version
  val otherVersion = Version(3, 2, 1)

  "WorkflowRowMaper.toWorkflow" should {
    "return a workflow as an object" when {
      "it is in the API's current version" in withWorkflow(currentVersion) {
        (workflow, _, workflowRow) =>
          workflowRowMapper().toWorkflow(workflowRow) shouldBe workflow
      }
    }
    "throw an exception" when {
      "the workflow has null body" in {
        an[Exception] mustBe thrownBy(
          workflowRowMapper().toWorkflow(mock[Row]))
      }
    }
  }

  def withWorkflow(version: Version)(testCode: (Workflow, String, Row) => Any): Unit = {
    val workflow = Workflow(
      WorkflowMetadata(WorkflowType.Batch, version.humanReadable),
      DeeplangGraph(),
      JsObject())
    val stringWorkflow = workflow.toJson.compactPrint
    val rowWorkflow = mock[Row]
    when(rowWorkflow.getString(WorkflowRowMapper.Workflow)).thenReturn(stringWorkflow)
    testCode(workflow, stringWorkflow, rowWorkflow)
  }

  def withStringWorkflow(stringWorkflow: String)(testCode: (String, Row) => Any): Unit = {
    val rowWorkflow = mock[Row]
    when(rowWorkflow.getString(WorkflowRowMapper.Workflow)).thenReturn(stringWorkflow)
    testCode(stringWorkflow, rowWorkflow)
  }

  def workflowRowMapper(): WorkflowRowMapper = {
    WorkflowRowMapper(graphReader)
  }
}
