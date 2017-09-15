/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import scala.concurrent.{Await, Future}

import com.datastax.driver.core.querybuilder.QueryBuilder
import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers}

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.cassandra.CassandraTestSupport
import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.parameters.{BooleanParameter, ParametersSchema}
import io.deepsense.graph.{Edge, Endpoint, Node, StatefulGraph, graphstate, nodestate}
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.workflows._
import io.deepsense.workflowmanager.rest.CurrentBuild


class WorkflowDaoCassandraImplIntegSpec
  extends StandardSpec
  with ScalaFutures
  with MockitoSugar
  with Matchers
  with BeforeAndAfter
  with CassandraTestSupport
  with GraphJsonTestSupport with Logging {

  var workflowsDao: WorkflowDaoCassandraImpl = _
  val catalog = mock[DOperationsCatalog]
  val graphReader: GraphReader = new GraphReader(catalog)
  val inferContext: InferContext = mock[InferContext]
  val rowMapper = new WorkflowRowMapper(graphReader)

  val paramSchema = ParametersSchema("param1" -> new BooleanParameter("desc", None, None))

  val operation1 = mockOperation(0, 1, DOperation.Id.randomId, "name1", paramSchema)
  val operation2 = mockOperation(1, 1, DOperation.Id.randomId, "name2", paramSchema)
  val operation3 = mockOperation(1, 1, DOperation.Id.randomId, "name3", paramSchema)
  val operation4 = mockOperation(2, 1, DOperation.Id.randomId, "name4", paramSchema)

  when(catalog.createDOperation(operation1.id)).thenReturn(operation1)
  when(catalog.createDOperation(operation2.id)).thenReturn(operation2)
  when(catalog.createDOperation(operation3.id)).thenReturn(operation3)
  when(catalog.createDOperation(operation4.id)).thenReturn(operation4)

  val w1@(workflow1Id, workflow1) = createWorkflow(graph = StatefulGraph())
  val w2@(workflow2Id, workflow2) = createWorkflow(graph = createGraph())

  val storedWorkflows = Set(w1, w2)

  def cassandraTableName: String = "workflows"
  def cassandraKeySpaceName: String = "workflowmanager"

  before {
    WorkflowTableCreator.create(cassandraTableName, session)
    workflowsDao = new WorkflowDaoCassandraImpl(cassandraTableName, session, rowMapper)
  }

  "WorkflowsDao" should {

    "not get deleted workflow" in withStoredWorkflows(storedWorkflows) {
      whenReady(workflowsDao.delete(workflow2Id)) { _ =>
        whenReady(workflowsDao.get(workflow2Id)) { workflow =>
          workflow shouldBe None
        }
      }
    }

    "update workflow" in withStoredWorkflows(storedWorkflows) {
      val modifiedWorkflow2 = workflow2.copy(additionalData = ThirdPartyData("[]"))
      whenReady(workflowsDao.save(workflow2Id, modifiedWorkflow2)) { _ =>
        whenReady(workflowsDao.get(workflow2Id)) { workflow =>
          workflow.get.right.get shouldBe modifiedWorkflow2
        }
      }
    }

    "find workflow by id" in withStoredWorkflows(storedWorkflows) {
      whenReady(workflowsDao.get(workflow1Id)) { workflow =>
        workflow shouldBe Some(Right(workflow1))
      }
    }

    "get latest execution result" when {
      "workflow does not exist" in {
        whenReady(workflowsDao.getLatestExecutionResults(Workflow.Id.randomId)) { result =>
          result shouldBe None
        }
      }

      "no results are available" in withStoredWorkflows(storedWorkflows) {
        whenReady(workflowsDao.getLatestExecutionResults(workflow1Id)) { result =>
          result shouldBe None
        }
      }

      "results exist" in withStoredWorkflows(storedWorkflows) {
        val resultId = ExecutionReportWithId.Id.randomId
        whenReady(workflowsDao.saveExecutionResults(
          executionResults(workflow1Id, workflow1, resultId))) { _ =>
          whenReady(workflowsDao.getLatestExecutionResults(workflow1Id)) { result =>
            result.get.right.get.executionReport.id shouldBe resultId
          }
        }
      }

      "results were updated" in withStoredWorkflows(storedWorkflows) {
        val resultId1 = ExecutionReportWithId.Id.randomId
        val resultId2 = ExecutionReportWithId.Id.randomId
        whenReady(workflowsDao.saveExecutionResults(
          executionResults(workflow1Id, workflow1, resultId1))) { _ =>
          whenReady(workflowsDao.saveExecutionResults(
            executionResults(workflow1Id, workflow1, resultId2))) { _ =>
            whenReady(workflowsDao.getLatestExecutionResults(workflow1Id)) { result =>
              result.get.right.get.executionReport.id shouldBe resultId2
            }
          }
        }
      }
    }

    "get results upload time" when {
      "the entire row is not there" in {
        whenReady(workflowsDao.getResultsUploadTime(Workflow.Id.randomId)) { result =>
          result shouldBe None
        }
      }

      "the row is there without the results upload time" in withStoredWorkflows(storedWorkflows) {
        whenReady(workflowsDao.getResultsUploadTime(workflow1Id)) { result =>
          result shouldBe None
        }
      }

      // Note, that this tests updating the results upload time
      // when saveExecutionResults is called
      "the results upload time is there" in withStoredWorkflows(storedWorkflows) {
        val resultId = ExecutionReportWithId.Id.randomId
        val startTime = DateTimeConverter.now

        whenReady(workflowsDao.saveExecutionResults(
          executionResults(workflow1Id, workflow1, resultId))) { _ =>

          whenReady(workflowsDao.getResultsUploadTime(workflow1Id)) { dateTimeOpt =>
            val endTime = DateTimeConverter.now
            dateTimeOpt match {
              case None => fail("Couldn't retrieve results upload time")
              case Some(dateTime: DateTime) =>
                logger.info(s"TIMES: $startTime, $dateTime")
                (dateTime.isAfter(startTime) || dateTime.isEqual(startTime)) shouldBe true
                (dateTime.isBefore(endTime) || dateTime.isEqual(endTime)) shouldBe true
            }
          }
        }
      }
    }
  }

  private def executionResults(
      workflowId: Workflow.Id,
      workflow: Workflow,
      resultId: ExecutionReportWithId.Id): WorkflowWithSavedResults = {
    WorkflowWithSavedResults(
        workflowId,
        workflow.metadata,
        workflow.graph,
        workflow.additionalData,
        ExecutionReportWithId(
          resultId,
          graphstate.Completed,
          DateTimeConverter.now,
          DateTimeConverter.now,
          Map[Node.Id, nodestate.NodeState](),
          EntitiesMap()))
  }

  private def withStoredWorkflows(
      storedWorkflows: Set[(Workflow.Id, Workflow)])(testCode: => Any): Unit = {
    val s = Future.sequence(storedWorkflows.map {
      case (id, workflow) => workflowsDao.save(id, workflow)
    })
    Await.ready(s, operationDuration)
    try {
      testCode
    } finally {
      session.execute(QueryBuilder.truncate(cassandraTableName))
    }
  }

  def createWorkflow(graph: StatefulGraph): (Workflow.Id, Workflow) = {
    val metadata = WorkflowMetadata(
      apiVersion = CurrentBuild.version.humanReadable,
      workflowType = WorkflowType.Batch)
    val thirdPartyData = ThirdPartyData("{}")
    (Workflow.Id.randomId, Workflow(metadata, graph, thirdPartyData))
  }

  def createGraph() : StatefulGraph = {
    val node1 = Node(Node.Id.randomId, operation1)
    val node2 = Node(Node.Id.randomId, operation2)
    val node3 = Node(Node.Id.randomId, operation3)
    val node4 = Node(Node.Id.randomId, operation4)
    val nodes = Set(node1, node2, node3, node4)
    val edgesList = List(
      (node1, node2, 0, 0),
      (node1, node3, 0, 0),
      (node2, node4, 0, 0),
      (node3, node4, 0, 1))
    val edges = edgesList.map(n => Edge(Endpoint(n._1.id, n._3), Endpoint(n._2.id, n._4))).toSet
    StatefulGraph(nodes, edges)
  }
}
