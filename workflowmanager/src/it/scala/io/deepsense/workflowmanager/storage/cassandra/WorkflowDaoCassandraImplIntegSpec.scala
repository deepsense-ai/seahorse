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
import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.parameters.{BooleanParameter, ParametersSchema}
import io.deepsense.graph.{Edge, Endpoint, Graph, Node}
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.workflows.{ThirdPartyData, Workflow, WorkflowMetadata, WorkflowType}


class WorkflowDaoCassandraImplIntegSpec
  extends StandardSpec
  with ScalaFutures
  with MockitoSugar
  with Matchers
  with BeforeAndAfter
  with CassandraTestSupport
  with GraphJsonTestSupport {

  var workflowsDao: WorkflowDaoCassandraImpl = _
  val catalog = mock[DOperationsCatalog]
  val graphReader: GraphReader = new GraphReader(catalog)
  val inferContext: InferContext = mock[InferContext]
  val rowMapper = new WorkflowRowMapper(graphReader)

  val paramSchema = ParametersSchema("param1" -> new BooleanParameter("desc", None, false, None))

  val operation1 = mockOperation(0, 1, DOperation.Id.randomId, "name1", "version1", paramSchema)
  val operation2 = mockOperation(1, 1, DOperation.Id.randomId, "name2", "version2", paramSchema)
  val operation3 = mockOperation(1, 1, DOperation.Id.randomId, "name3", "version3", paramSchema)
  val operation4 = mockOperation(2, 1, DOperation.Id.randomId, "name4", "version4", paramSchema)

  when(catalog.createDOperation(operation1.id)).thenReturn(operation1)
  when(catalog.createDOperation(operation2.id)).thenReturn(operation2)
  when(catalog.createDOperation(operation3.id)).thenReturn(operation3)
  when(catalog.createDOperation(operation4.id)).thenReturn(operation4)

  val w1@(workflow1Id, workflow1) = createWorkflow(graph = Graph())
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
          workflow.get shouldBe modifiedWorkflow2
        }
      }
    }

    "find workflow by id" in withStoredWorkflows(storedWorkflows) {
      whenReady(workflowsDao.get(workflow1Id)) { workflow =>
        workflow shouldBe Some(workflow1)
      }
    }
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

  def createWorkflow(graph: Graph): (Workflow.Id, Workflow) = {
    val metadata = WorkflowMetadata(apiVersion = "x.x.x", workflowType = WorkflowType.Batch)
    val thirdPartyData = ThirdPartyData("{}")
    (Workflow.Id.randomId, Workflow(metadata, graph, thirdPartyData))
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
