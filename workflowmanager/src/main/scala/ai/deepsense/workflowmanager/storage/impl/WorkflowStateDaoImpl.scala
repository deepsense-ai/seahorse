/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.name.Named
import slick.driver.JdbcDriver
import slick.lifted.{Index, PrimaryKey, ProvenShape}
import spray.json._

import ai.deepsense.commons.datetime.DateTimeConverter
import ai.deepsense.commons.models.Entity
import ai.deepsense.graph.Node
import ai.deepsense.graph.nodestate.Draft
import ai.deepsense.models.json.graph.NodeStatusJsonProtocol
import ai.deepsense.models.json.workflow.EntitiesMapJsonProtocol
import ai.deepsense.models.workflows.Workflow.Id
import ai.deepsense.models.workflows.{EntitiesMap, NodeState}
import ai.deepsense.workflowmanager.storage.WorkflowStateStorage

class WorkflowStateDaoImpl @Inject()(
    @Named("workflowmanager") db: JdbcDriver#API#Database,
    @Named("workflowmanager") driver: JdbcDriver)
    (implicit ec: ExecutionContext)
  extends WorkflowStateStorage
  with EntitiesMapJsonProtocol
  with NodeStatusJsonProtocol{

  private type Results = Seq[Entity.Id]
  private type Reports = Option[EntitiesMap]

  import driver.api._

  override def get(workflowId: Id): Future[Map[Node.Id, NodeState]] = {
    db.run(workflowStates.filter(_.workflowId === workflowId.value).result)
      .map(_.map(toIdAndNodeState).toMap)
  }

  override def save(workflowId: Id, state: Map[Node.Id, NodeState]): Future[Unit] = {

    def update(nodeId: Node.Id, nodeResults: Results, nodeReports: Reports): Future[Unit] =
      for {
        previousState <- db.run(workflowStates.filter(
          s => s.workflowId === workflowId.value && s.nodeId === nodeId.value).result)
        _ <- {
          val previousReports = previousState.headOption.flatMap {
            case (_, _, _, _, reports) => reports
          }
          val reportsToInsert = nodeReports.map(entitiesMapToCell).orElse(previousReports)
          val value = (workflowId.value, nodeId.value, DateTimeConverter.now.getMillis,
            nodeResultsToCell(nodeResults), reportsToInsert)

          db.run(workflowStates.insertOrUpdate(value))
        }
      } yield ()

    val futures = state.map {
      case (nodeId, nodeState) => update(nodeId, nodeState.nodeStatus.results, nodeState.reports)
    }

    Future.sequence(futures).map(_ => ())
  }

  private def toIdAndNodeState(
      row: (UUID, UUID, Long, String, Option[String])): (Node.Id, NodeState) = {
    val (_, nodeId, _, results, reportsJson) = row
    val nodeStatus = Draft(results.parseJson.convertTo[Seq[Entity.Id]])
    val reports = reportsJson map { _.parseJson.convertTo[EntitiesMap] }
    (Node.Id(nodeId), NodeState(nodeStatus, reports))
  }

  private def nodeResultsToCell(results: Results): String = results.toJson.compactPrint

  private def entitiesMapToCell(entitiesMap: EntitiesMap): String = entitiesMap.toJson.compactPrint

  val WorkflowId = "workflow_id"
  val NodeId = "node_id"
  val UpdateTime = "update_time"
  val Results = "results"
  val Reports = "reports"

  private class WorkflowStates(tag: Tag)
    extends Table[(UUID, UUID, Long, String, Option[String])](tag, "WORKFLOW_STATES") {

    def workflowId: Rep[UUID] = column[UUID](WorkflowId)
    def nodeId: Rep[UUID] = column[UUID](NodeId)
    def updateTime: Rep[Long] = column[Long](UpdateTime)
    def results: Rep[String] = column[String](Results)
    def reports: Rep[Option[String]] = column[Option[String]](Reports)

    def pk: PrimaryKey = primaryKey("pk_workflow_states", (workflowId, nodeId))
    def index: Index = index("idx_workflow_states_workflow_id", workflowId, unique = false)

    def * : ProvenShape[(UUID, UUID, Long, String, Option[String])] =
      (workflowId, nodeId, updateTime, results, reports)
  }

  private val workflowStates = TableQuery[WorkflowStates]

  private[impl] def create(): Future[Unit] = db.run(workflowStates.schema.create)
  private[impl] def drop(): Future[Unit] = db.run(workflowStates.schema.drop)
}
