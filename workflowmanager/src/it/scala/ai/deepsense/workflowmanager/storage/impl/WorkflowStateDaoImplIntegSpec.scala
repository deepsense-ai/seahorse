/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.workflowmanager.storage.impl

import scala.concurrent.{Await, Future}

import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers}

import ai.deepsense.commons.StandardSpec
import ai.deepsense.commons.datetime.DateTimeConverter
import ai.deepsense.commons.utils.Logging
import ai.deepsense.graph.{Node, nodestate}
import ai.deepsense.models.workflows.{EntitiesMap, NodeState, Workflow}

class WorkflowStateDaoImplIntegSpec
  extends StandardSpec
  with ScalaFutures
  with MockitoSugar
  with Matchers
  with BeforeAndAfter
  with SlickTestSupport
  with Logging {

  var workflowStateDao: WorkflowStateDaoImpl = _

  before {
    workflowStateDao = new WorkflowStateDaoImpl(db, driver)
  }

  private def aDate: DateTime = DateTimeConverter.now

  private val draftNoReports = (Node.Id.randomId, NodeState(nodestate.Draft(), None))
  private val draftWithReports =
    (Node.Id.randomId, NodeState(nodestate.Draft(), Some(EntitiesMap())))
  private val completedNoReports = (Node.Id.randomId, NodeState(
      nodestate.Completed(aDate, aDate, Seq()), None))
  private val completedWithReports = (Node.Id.randomId, NodeState(
    nodestate.Completed(aDate, aDate, Seq()), Some(EntitiesMap())))

  val workflowState1@(workflowId1, state1) = createState(draftNoReports, completedNoReports)
  val workflowState2@(workflowId2, state2) = createState(completedNoReports, draftWithReports)
  val workflowState3@(workflowId3, state3) = createState(completedWithReports, draftWithReports)

  private val expectedState1 = draftStates(state1)

  private def createState(
      nodes: (Node.Id, NodeState)*): (Workflow.Id, Map[Node.Id, NodeState]) =
    (Workflow.Id.randomId, nodes.toMap)

  private def draftStates(states: Map[Node.Id, NodeState]): Map[Node.Id, NodeState] = {
    states.map {
      case (nodeId, nodeState) =>
        (nodeId, nodeState.draft)
    }
  }

  "WorkflowStateDao" should {
    "retrieve stored state" in withStored(workflowState1, workflowState2) {
      logger.debug(s"Getting state of workflow $workflowId1")
      whenReady(workflowStateDao.get(workflowId1)) { state =>
        state shouldBe expectedState1
      }
    }

    "fail to retrieve state that is not stored" in withStored(workflowState1) {
      whenReady(workflowStateDao.get(workflowId2)) { state =>
        state shouldBe Map.empty
      }
    }

    "save state" in withStored() {
      whenReady(workflowStateDao.save(workflowId1, state1)) { _ =>
        whenReady(workflowStateDao.get(workflowId1)) { state =>
          state shouldBe expectedState1
        }
      }
    }

    "update state" in withStored(workflowState1) {
      val nodeId = draftNoReports._1
      assert(state1.contains(nodeId))
      val newState = NodeState(nodestate.Draft(), Some(EntitiesMap()))

      whenReady(workflowStateDao.save(workflowId1, Map(nodeId -> newState))) { _ =>
        whenReady(workflowStateDao.get(workflowId1)) { state =>
          state shouldBe expectedState1.updated(nodeId, newState)
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
          state shouldBe draftStates(state3.updated(draftWithReports._1, newState))
        }
      }
    }
  }

  private def withStored(
      storedStates: (Workflow.Id, Map[Node.Id, NodeState])*)(
      testCode: => Any): Unit = {

    Await.ready(workflowStateDao.create().map(_ => logger.debug(s"DAO created")), operationDuration)

    val s = Future.sequence(storedStates.map {
      case (id, state) => workflowStateDao.save(id, state)
        .map(_ => logger.debug(s"Saved: workflowId: $id, state: $state"))
    })
    Await.ready(s.map(_ => logger.debug(s"States saved, executing code.")), operationDuration)

    try {
      testCode
    } finally {
      Await.ready(workflowStateDao.drop().map(_ => logger.debug(s"DAO dropped")), operationDuration)
    }
  }
}
