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

import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.commons.utils.Logging
import ai.deepsense.models.workflows.Workflow._
import ai.deepsense.workflowmanager.model.WorkflowPreset

class PresetsDao @Inject()(
   @Named("workflowmanager") db: JdbcDriver#API#Database,
   @Named("workflowmanager") driver: JdbcDriver)(implicit ec: ExecutionContext) extends Logging {

  import driver.api._

  def savePreset(clusterDetails: ClusterDetails): Future[Long] = {
    val insert = (presets returning presets.map(_.id)) += clusterDetails
    db.run(insert)
  }

  def updatePreset(presetId: Long, clusterConfig: ClusterDetails): Future[Unit] = {
    val updatedCount = db.run(
      presets.filter(x => x.id === presetId && x.isEditable === true).update(clusterConfig))
    updatedCount.map(_ => ())
  }

  def getPreset(presetId: Long): Future[Option[ClusterDetails]] = {
    db.run(presets.filter(_.id === presetId).result.headOption)
  }

  def getPresets(): Future[Seq[ClusterDetails]] = {
    val query = presets.result
    val storedPresets = db.run(query)
    storedPresets.onSuccess {
      case x => logger.info(s"Stored presets are: $x")
    }
    storedPresets
  }

  def removePreset(presetId: Long): Future[Unit] = {
    val q = presets.filter(x => x.id === presetId && x.isDefault =!= true)
    val action = q.delete
    db.run(action).map(_ => ())
  }

  private class PresetsTable(tag: Tag)
    extends Table[ClusterDetails](tag, "PRESETS") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def clusterType = column[String]("clusterType")
    def uri = column[String]("uri")
    def userIP = column[String]("userIP")
    def hadoopUser = column[Option[String]]("hadoopUser")
    def isEditable = column[Boolean]("isEditable")
    def isDefault = column[Boolean]("isDefault")
    def driverMemory = column[Option[String]]("driverMemory")
    def executorMemory = column[Option[String]]("executorMemory")
    def totalExecutorCores = column[Option[Int]]("totalExecutorCores")
    def executorCores = column[Option[Int]]("executorCores")
    def numExecutors = column[Option[Int]]("numExecutors")
    def params = column[Option[String]]("params")

    override def * = (
      id.?,
      name,
      clusterType,
      uri,
      userIP,
      hadoopUser,
      isEditable,
      isDefault,
      driverMemory,
      executorMemory,
      totalExecutorCores,
      executorCores,
      numExecutors,
      params) <> (ClusterDetails.tupled, ClusterDetails.unapply)
  }

  private val presets = TableQuery[PresetsTable]

  def matchesError(ex: java.sql.SQLException, errorCode: Int): Boolean =
    ex.getErrorCode == errorCode

  def saveWorkflowsPreset(workflowPreset: WorkflowPreset): Future[Unit] = {
    val insert = workflowsPreset.insertOrUpdate(workflowPreset)
    db.run(insert).map(_ => ())
  }

  def getWorkflowsPreset(workflowId: Id): Future[Option[ClusterDetails]] = {
    val presetForWorkflowDBIO = for {
      workflowPreset <- workflowsPreset.filter(
        _.workflow_id === workflowId.value
      ).result.headOption
      presetsResult <- {
        val filteredPreset = workflowPreset match {
          case Some(x) => presets.filter(_.id === x.presetId)
          case None => presets.filter(_.isDefault === true)
        }
        filteredPreset.result.headOption
      }
    } yield presetsResult

    db.run(presetForWorkflowDBIO)
  }

  val workflowPresetTableName: String = "WORKFLOWSPRESETS"

  private class WorkflowsPresetTable(tag: Tag)
    extends Table[WorkflowPreset](tag, workflowPresetTableName) {

    private val workflowIdColumn = "workflow_id"
    private val presetIdColumn = "preset_id"

    def workflow_id = column[UUID](workflowIdColumn, O.PrimaryKey)
    def preset_id = column[Long](presetIdColumn)

    override def * = (
      workflow_id,
      preset_id) <> (
      {
        tuple: (UUID, Long) =>
          WorkflowPreset(Id.fromUuid(tuple._1), tuple._2)
      },
      { w: WorkflowPreset =>
          Some((w.id.value, w.presetId))
      })
  }

  private val workflowsPreset = TableQuery[WorkflowsPresetTable]
}
