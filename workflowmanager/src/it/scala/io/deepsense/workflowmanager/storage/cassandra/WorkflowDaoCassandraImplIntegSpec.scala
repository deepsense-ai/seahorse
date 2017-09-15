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
import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.parameters.{BooleanParameter, ParametersSchema}
import io.deepsense.graph.{Edge, Endpoint, Graph, Node}
import io.deepsense.model.json.graph.GraphJsonProtocol
import io.deepsense.model.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.workflows.Workflow
import io.deepsense.models.workflows.Workflow.State
import io.deepsense.workflowmanager.WorkflowTableCreator

class WorkflowDaoCassandraImplIntegSpec
  extends StandardSpec
  with ScalaFutures
  with MockitoSugar
  with Matchers
  with BeforeAndAfter
  with CassandraTestSupport
  with GraphJsonTestSupport {

  var experimentsDao : WorkflowDaoCassandraImpl = _
  val catalog = mock[DOperationsCatalog]
  val graphReader: GraphReader = new GraphReader(catalog)
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

  val tenantId1 = "TestTenantId1"
  val tenantId2 = "TestTenantId2"
  val tenantId3 = "TestTenantId3"

  val experiment1 = createExperiment(tenantId1, name = "name1", state = State.running)
  val experiment2 = createExperiment(tenantId1, name = "name2", graph = createGraph)
  val experiment3 = createExperiment(tenantId2, name = "name3")
  val experiment4 = createExperiment(tenantId2, state = State.failed(createFailureDescription))
  val experiment5 = createExperiment(tenantId2, graph = createGraph)
  val storedExperiments = Set(experiment1, experiment2, experiment3, experiment4, experiment5)

  def cassandraTableName : String = "experiments"
  def cassandraKeySpaceName : String = "experimentmanager"

  before {
    WorkflowTableCreator.create(cassandraTableName, session)
    experimentsDao = new WorkflowDaoCassandraImpl(cassandraTableName, session, rowMapper)
  }

  "ExperimentsDao" should {
    "select all rows owned by tenantId1" in withStoredExperiments(storedExperiments) {
      whenReady(experimentsDao.list(tenantId1)) { experiments =>
        experiments should contain theSameElementsAs Seq(experiment1, experiment2)
      }
    }
    "select all rows owned by tenantId2" in withStoredExperiments(storedExperiments) {
      whenReady(experimentsDao.list(tenantId2)) { experiments =>
        experiments should contain theSameElementsAs Seq(experiment3, experiment4, experiment5)
      }
    }

    "not list deleted experiment" in withStoredExperiments(storedExperiments) {
      whenReady(experimentsDao.delete(tenantId2, experiment3.id)) { _ =>
        whenReady(experimentsDao.list(tenantId2)) { experiments =>
          experiments should contain theSameElementsAs Seq(experiment4, experiment5)
        }
      }
    }

    "not get deleted experiment" in withStoredExperiments(storedExperiments) {
      whenReady(experimentsDao.delete(tenantId2, experiment3.id)) { _ =>
        whenReady(experimentsDao.get(tenantId2, experiment3.id)) { experiment =>
          experiment shouldBe None
        }
      }
    }

    "update text fields in experiment" in withStoredExperiments(storedExperiments) {
      val modifiedExperiment3 = experiment3.copy(description = "Description2", name = "Name2")
      whenReady(experimentsDao.save(modifiedExperiment3)) { _ =>
        whenReady(experimentsDao.list(tenantId2)) { experiments =>
          experiments should contain theSameElementsAs
            Seq(modifiedExperiment3, experiment4, experiment5)
        }
      }
    }

    "update json fields in experiment" in withStoredExperiments(storedExperiments) {
      val modifiedExperiment3 = experiment3.copy(graph = createGraph)
      whenReady(experimentsDao.save(modifiedExperiment3)) { _ =>
        whenReady(experimentsDao.list(tenantId2)) { experiments =>
          experiments should contain theSameElementsAs
            Seq(modifiedExperiment3, experiment4, experiment5)
        }
      }
    }

    "find experiment by id" in withStoredExperiments(storedExperiments) {
      whenReady(experimentsDao.get(tenantId1, experiment1.id)) { experiment =>
        experiment shouldBe Some(experiment1)
      }
    }

    "not find experiment if tenantId is not correct" in withStoredExperiments(storedExperiments) {
      whenReady(experimentsDao.get(tenantId2, experiment1.id)) { experiment =>
        experiment shouldBe None
      }
    }
  }

  private def withStoredExperiments(storedExperiments: Set[Workflow])(testCode: => Any): Unit = {
    val s = Future.sequence(storedExperiments.map(experimentsDao.save))
    Await.ready(s, operationDuration)
    try {
      testCode
    } finally {
      session.execute(QueryBuilder.truncate(cassandraTableName))
    }
  }

  def createFailureDescription() = {
    FailureDescription(
      id = DeepSenseFailure.Id.randomId,
      code = FailureCode.ExperimentNotFound,
      title = "Problem",
      message = Some("Problem"),
      details = Map("problem1" -> "description1", "problem2" -> "description2"))
  }

  def createExperiment(
      tenant: String,
      graph: Graph = new Graph(),
      name: String = "test",
      state: Workflow.State = Workflow.State.draft) : Workflow = {
    val now = DateTimeConverter.now
    new Workflow(Workflow.Id.randomId, tenant, name, graph, now, now, "description", state)
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
