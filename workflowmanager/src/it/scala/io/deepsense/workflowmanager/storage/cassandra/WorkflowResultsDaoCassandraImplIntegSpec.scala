/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers}

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.cassandra.CassandraTestSupport
import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.parameters.{BooleanParameter, ParametersSchema}
import io.deepsense.graph._
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.workflows._
import io.deepsense.workflowmanager.rest.CurrentBuild

class WorkflowResultsDaoCassandraImplIntegSpec
  extends StandardSpec
  with ScalaFutures
  with MockitoSugar
  with Matchers
  with BeforeAndAfter
  with CassandraTestSupport
  with GraphJsonTestSupport {

  var workflowResultsDao: WorkflowResultsDaoCassandraImpl = _
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

  val resultId = ExecutionReportWithId.Id.randomId
  val result = createResult(resultId, graph = createGraph())

  def cassandraTableName: String = "workflowresults"
  def cassandraKeySpaceName: String = "workflowmanager"

  before {
    WorkflowResultsTableCreator.create(cassandraTableName, session)
    workflowResultsDao = new WorkflowResultsDaoCassandraImpl(cassandraTableName, session, rowMapper)
  }

  "WorkflowsResultsDao" should {

    "return None when non existing workflow" in {
      whenReady(workflowResultsDao.get(Workflow.Id.randomId)) { results =>
        results shouldBe None
      }
    }

    "save and get workflow results" in {
      whenReady(workflowResultsDao.save(result)) { _ =>
        whenReady(workflowResultsDao.get(resultId)) { results =>
          results shouldBe Some(Right(result))
        }
      }
    }
  }

  def createResult(
      resultId: ExecutionReportWithId.Id,
      graph: StatefulGraph): WorkflowWithSavedResults = {
    val metadata = WorkflowMetadata(
      apiVersion = CurrentBuild.version.humanReadable,
      workflowType = WorkflowType.Batch)
    val thirdPartyData = ThirdPartyData("{}")
    val executionReport: ExecutionReportWithId = ExecutionReportWithId(
      resultId,
      graphstate.Failed(FailureDescription(DeepSenseFailure.Id.randomId, FailureCode.NodeFailure, "title")),
      DateTimeConverter.now,
      DateTimeConverter.now,
      Map(Node.Id.randomId -> nodestate.Failed(
        DateTimeConverter.now,
        DateTimeConverter.now,
        FailureDescription(DeepSenseFailure.Id.randomId, FailureCode.NodeFailure, "title"))),
      EntitiesMap())
    WorkflowWithSavedResults(
      Workflow.Id.randomId,
      metadata,
      graph,
      thirdPartyData,
      executionReport)
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
