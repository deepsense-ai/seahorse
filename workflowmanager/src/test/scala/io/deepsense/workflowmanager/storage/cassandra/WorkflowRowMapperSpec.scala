/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import java.util.Date

import com.datastax.driver.core.Row
import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito._
import spray.json._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.Version
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.graph.{Graph, Status}
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.{WorkflowJsonProtocol, WorkflowWithSavedResultsJsonProtocol}
import io.deepsense.models.workflows._
import io.deepsense.workflowmanager.rest.CurrentBuild

class WorkflowRowMapperSpec
  extends StandardSpec
  with UnitTestSupport
  with WorkflowJsonProtocol
  with WorkflowWithSavedResultsJsonProtocol {

  override val graphReader: GraphReader = mock[GraphReader]
  when(graphReader.read(any())).thenReturn(Graph())
  val currentVersion = CurrentBuild.version
  val otherVersion = Version(3, 2, 1)

  "WorkflowRowMaper.toWorkflow" should {
    "return a workflow as an object" when {
      "it is in the API's current version" in withWorkflow(currentVersion) {
        (workflow, _, workflowRow) =>
          workflowRowMapper().toWorkflow(workflowRow) shouldBe Right(workflow)
      }
    }
    "return a workflow as a String" when {
      "it is NOT in the API's current version" in withWorkflow(otherVersion) {
        (_, stringWorkflow, workflowRow) =>
          workflowRowMapper()
            .toWorkflow(workflowRow) shouldBe Left(stringWorkflow)
      }
      "it's version does not exist" in withStringWorkflow("{ \"foo\" : \"bar\" }") {
        (stringWorkflow, workflowRow) =>
          workflowRowMapper()
            .toWorkflow(workflowRow) shouldBe Left(stringWorkflow)
      }
      "it's version is corrupted" in
        withStringWorkflow("{ \"metadata\" : {\"apiVersion\": \"foobar\"} }") {
          (stringWorkflow, workflowRow) =>
            workflowRowMapper().toWorkflow(workflowRow) shouldBe Left(stringWorkflow)
      }
    }
    "throw an exception" when {
      "the workflow has null body" in {
        an[Exception] mustBe thrownBy(
          workflowRowMapper().toWorkflow(mock[Row]))
      }
    }
  }

  "WorkflowRowMapper.toWorkflowWithSavedResults" should {
    "return a workflow with results as an object" when {
      "it is in the API's current version" in
        withWorkflowWithSavedResults(currentVersion) {
          (workflow, _, workflowRow) =>
            workflowRowMapper()
              .toWorkflowWithSavedResults(workflowRow) shouldBe Some(Right(workflow))
      }
    }
    "return a workflow with results as a String" when {
      "it is NOT in the API's current version" in
        withWorkflowWithSavedResults(otherVersion) {
          (workflow, stringWorkflow, workflowRow) =>
            workflowRowMapper()
              .toWorkflowWithSavedResults(workflowRow) shouldBe Some(Left(stringWorkflow))
      }
      "it's version does not exist" in
        withWorkflowWithSavedResultString("{ \"foo\" : \"bar\" }") {
          (stringWorkflow, workflowRow) =>
            workflowRowMapper()
              .toWorkflowWithSavedResults(workflowRow) shouldBe Some(Left(stringWorkflow))
      }
      "it's version is corrupted" in
        withStringWorkflow("{ \"metadata\" : {\"apiVersion\": \"foobar\"} }") {
          (stringWorkflow, row) =>
            workflowRowMapper()
              .toWorkflow(row) shouldBe Left(stringWorkflow)
        }
    }
    "return None" when {
      "the workflow has null body" in {
        workflowRowMapper()
          .toWorkflowWithSavedResults(mock[Row]) shouldBe None
      }
    }
  }

  "WorkflowRowMapper.toResultsUploadTime" should {
    "return results upload time" when {
      "it's there" in
        withResultsUploadTime(DateTimeConverter.now) {
          (dateTime, row) =>
            workflowRowMapper()
              .toResultsUploadTime(row) shouldBe Some(dateTime)
        }
    }
    "return None" when {
      "results upload time is not present" in {
        val rowWorkflow = mock[Row]
        workflowRowMapper()
          .toResultsUploadTime(rowWorkflow) shouldBe None
      }
    }
  }

  def withWorkflow(version: Version)(testCode: (Workflow, String, Row) => Any): Unit = {
    val workflow = Workflow(
      WorkflowMetadata(WorkflowType.Batch, version.humanReadable),
      Graph(),
      ThirdPartyData("{}"))
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

  def withResultsUploadTime(dateTime: DateTime)(testCode: (DateTime, Row) => Any): Unit = {
    val rowWorkflow = mock[Row]
    when(rowWorkflow.getDate(WorkflowRowMapper.ResultsUploadTime)).thenReturn(
      new Date(dateTime.getMillis))
    testCode(dateTime, rowWorkflow)
  }

  def workflowRowMapper(): WorkflowRowMapper = {
    WorkflowRowMapper(graphReader)
  }

  def withWorkflowWithSavedResults(version: Version)
     (testCode: (WorkflowWithSavedResults, String, Row) => Any): Unit = {
    val started = DateTimeConverter.now
    val ended = started.plusHours(1)
    val workflowWithSavedResults =
      WorkflowWithSavedResults(
        Id.randomId,
        WorkflowMetadata(WorkflowType.Batch, version.humanReadable),
        Graph(),
        ThirdPartyData("{}"),
        ExecutionReportWithId(
          Id.randomId,
          ExecutionReport(Status.Completed, started, ended, None, Map(), EntitiesMap())))

    val stringWorkflow = workflowWithSavedResults.toJson.compactPrint
    val rowWorkflow = mock[Row]
    when(rowWorkflow.getString(WorkflowRowMapper.Results)).thenReturn(stringWorkflow)
    testCode(workflowWithSavedResults, stringWorkflow, rowWorkflow)
  }

  def withWorkflowWithSavedResultString(workflowWithSavedResults: String)
      (testCode: (String, Row) => Any): Unit = {
    val rowWorkflow = mock[Row]
    when(rowWorkflow.getString(WorkflowRowMapper.Results)).thenReturn(workflowWithSavedResults)
    testCode(workflowWithSavedResults, rowWorkflow)
  }
}
