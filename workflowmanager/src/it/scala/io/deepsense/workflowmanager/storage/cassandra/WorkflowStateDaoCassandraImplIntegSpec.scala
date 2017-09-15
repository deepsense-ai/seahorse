/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import scala.concurrent.{Await, Future}

import com.datastax.driver.core.querybuilder.QueryBuilder
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers}

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.cassandra.CassandraTestSupport
import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.utils.Logging
import io.deepsense.graph.{Node, nodestate}
import io.deepsense.models.workflows.{NodeState, EntitiesMap, Workflow}

class WorkflowStateDaoCassandraImplIntegSpec
  extends StandardSpec
  with ScalaFutures
  with MockitoSugar
  with Matchers
  with BeforeAndAfter
  with CassandraTestSupport with Logging {

  override def cassandraTableName: String = "workflow_state"

  override def cassandraKeySpaceName: String = "workflowmanager"

  var workflowStateDao: WorkflowStateDaoCassandraImpl = _

  before {
    WorkflowStateStorageCreator.createWorkflowStateTableCommand(cassandraTableName, session)
    workflowStateDao = new WorkflowStateDaoCassandraImpl(
      cassandraTableName, session, new WorkflowStateRowMapper)
  }

  private def aDate: DateTime = DateTimeConverter.now

  val draftNoReports = (Node.Id.randomId, NodeState(nodestate.Draft(), None))
  val draftWithReports =
    (Node.Id.randomId, NodeState(nodestate.Draft(), Some(EntitiesMap())))
  val completedNoReports = (Node.Id.randomId, NodeState(
      nodestate.Completed(aDate, aDate, Seq()), None))
  val completedWithReports = (Node.Id.randomId, NodeState(
    nodestate.Completed(aDate, aDate, Seq()), Some(EntitiesMap())))

  val workflowState1@(workflowId1, state1) = createState(draftNoReports, completedNoReports)
  val workflowState2@(workflowId2, state2) = createState(completedNoReports, draftWithReports)
  val workflowState3@(workflowId3, state3) = createState(completedWithReports, draftWithReports)

  private def createState(
      nodes: (Node.Id, NodeState)*): (Workflow.Id, Map[Node.Id, NodeState]) =
    (Workflow.Id.randomId, nodes.toMap)

  "WorkflowStateDao" should {
    "retrieve stored state" in withStored(workflowState1, workflowState2) {
      whenReady(workflowStateDao.get(workflowId1)) { state =>
        state shouldBe state1
      }
    }

    "fail to retrieve state that is not stored" in withStored(workflowState1) {
      whenReady(workflowStateDao.get(workflowId2)) { state =>
        state shouldBe Map.empty
      }
    }

    "save state" in {
      whenReady(workflowStateDao.save(workflowId1, state1)) { _ =>
        whenReady(workflowStateDao.get(workflowId1)) { state =>
          state shouldBe state1
        }
      }
    }

    "update state" in withStored(workflowState1) {
      val nodeId = draftNoReports._1
      assert(state1.contains(nodeId))
      val newState = NodeState(nodestate.Draft(), Some(EntitiesMap()))

      whenReady(workflowStateDao.save(workflowId1, Map(nodeId -> newState))) { _ =>
        whenReady(workflowStateDao.get(workflowId1)) { state =>
          state shouldBe state1.updated(nodeId, newState)
        }
      }
    }

    "update state without overwriting reports" in withStored(workflowState3) {
      // In this case, we update the state of a node with reports == None.
      // This means, that reports present in storage before, will not be overwritten.
      val nodeId = draftWithReports._1
      assert(state3.contains(nodeId))

      val newNodeState = nodestate.Completed(aDate, aDate, Seq())
      val stateUpdate = NodeState(newNodeState, None)
      val newState = NodeState(newNodeState, draftWithReports._2.reports)

      whenReady(workflowStateDao.save(workflowId3, Map(nodeId -> stateUpdate))) { _ =>
        whenReady(workflowStateDao.get(workflowId3)) { state =>
          state shouldBe state3.updated(draftWithReports._1, newState)
        }
      }
    }
  }

  private def withStored(
      storedStates: (Workflow.Id, Map[Node.Id, NodeState])*)(
      testCode: => Any): Unit = {

    val s = Future.sequence(storedStates.map {
      case (id, state) => workflowStateDao.save(id, state)
    })

    Await.ready(s, operationDuration)
    try {
      testCode
    } finally {
      session.execute(new QueryBuilder(session.getCluster).truncate(cassandraTableName))
    }
  }

}
