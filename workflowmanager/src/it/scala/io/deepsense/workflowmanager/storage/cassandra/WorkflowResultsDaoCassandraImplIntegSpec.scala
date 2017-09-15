/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import scala.concurrent.{Await, Future}

import com.datastax.driver.core.querybuilder.QueryBuilder
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers}

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.cassandra.CassandraTestSupport
import io.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.parameters.{BooleanParameter, ParametersSchema}
import io.deepsense.graph._
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.workflows._

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
  val rowMapper = new WorkflowResultsRowMapper(graphReader, inferContext)

  val paramSchema = ParametersSchema("param1" -> new BooleanParameter("desc", None, false, None))

  val operation1 = mockOperation(0, 1, DOperation.Id.randomId, "name1", "version1", paramSchema)
  val operation2 = mockOperation(1, 1, DOperation.Id.randomId, "name2", "version2", paramSchema)
  val operation3 = mockOperation(1, 1, DOperation.Id.randomId, "name3", "version3", paramSchema)
  val operation4 = mockOperation(2, 1, DOperation.Id.randomId, "name4", "version4", paramSchema)

  when(catalog.createDOperation(operation1.id)).thenReturn(operation1)
  when(catalog.createDOperation(operation2.id)).thenReturn(operation2)
  when(catalog.createDOperation(operation3.id)).thenReturn(operation3)
  when(catalog.createDOperation(operation4.id)).thenReturn(operation4)

  val workflowId: Workflow.Id = Workflow.Id.randomId
  val result1 = createWorkflow(workflowId, graph = Graph())
  val result2 = createWorkflow(workflowId, graph = createGraph())

  val storedWorkflows = Set(result1, result2)

  def cassandraTableName: String = "workflowresults"
  def cassandraKeySpaceName: String = "workflowmanager"

  before {
    WorkflowResultsTableCreator.create(cassandraTableName, session)
    workflowResultsDao = new WorkflowResultsDaoCassandraImpl(cassandraTableName, session, rowMapper)
  }

  "WorkflowsResultsDao" should {

    "return empty list when non existing workflow" in {
      whenReady(workflowResultsDao.get(Workflow.Id.randomId)) { results =>
        results shouldBe List()
      }
    }

    "save and get workflow results" in {
      whenReady(workflowResultsDao.save(workflowId, result1)) { _ =>
        whenReady(workflowResultsDao.save(workflowId, result2)) { _ =>
          whenReady(workflowResultsDao.get(workflowId)) { results =>
            results shouldBe List(result1, result2)
          }
        }
      }
    }
  }

  private def withStoredWorkflows(
      storedWorkflows: Set[(Workflow.Id, WorkflowWithResults)])(testCode: => Any): Unit = {
    val s = Future.sequence(storedWorkflows.map {
      case (id, workflow) => workflowResultsDao.save(id, workflow)
    })
    Await.ready(s, operationDuration)
    try {
      testCode
    } finally {
      session.execute(QueryBuilder.truncate(cassandraTableName))
    }
  }

  def createWorkflow(id: Workflow.Id, graph: Graph): WorkflowWithResults = {
    val metadata = WorkflowMetadata(apiVersion = "x.x.x", workflowType = WorkflowType.Batch)
    val thirdPartyData = ThirdPartyData("{}")
    val executionReport: ExecutionReport = ExecutionReport(
      Status.Failed,
      Some(FailureDescription(DeepSenseFailure.Id.randomId, FailureCode.NodeFailure, "title")),
      Map(Node.Id.randomId -> State(Status.Failed)),
      EntitiesMap())
    WorkflowWithResults(id, metadata, graph, thirdPartyData, executionReport)
  }

  def createGraph() : Graph = {
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
    Graph(nodes, edges)
  }
}
