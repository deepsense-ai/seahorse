/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.impl

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.name.Named
import slick.driver.JdbcDriver

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.commons.utils.Logging


class PresetsDao @Inject()(
   @Named("workflowmanager") db: JdbcDriver#API#Database,
   @Named("workflowmanager") driver: JdbcDriver)(implicit ec: ExecutionContext)
      extends Logging {

  import driver.api._


  def savePreset(clusterDetails: ClusterDetails): Future[Long] = {
    val insert = (presets returning presets.map(_.id)) += (clusterDetails)
    db.run(insert)
  }

  def updatePreset(presetId: Long, clusterConfig: ClusterDetails): Future[Unit] = Future {
    db.run(presets.filter(x => x.id === presetId && x.isEditable === true).update(clusterConfig))
  }

  def getPreset(presetId : Long): Future[Option[ClusterDetails]] = {
    db.run(presets.filter(_.id === presetId).result.headOption)
  }


  def getPresets: Future[Seq[ClusterDetails]] = {
    val query = presets.result
    val storedPresets = db.run(query)
    storedPresets.onSuccess {
      case x => logger.info(s"Stored presets are: $x")
    }
    storedPresets
  }

  def removePreset(presetId: Long): Future[Unit] = Future {
    val q = presets.filter(x => x.id === presetId && x.isDefault =!= true)
    val action = q.delete
    val affectedRowsCount: Future[Int] = db.run(action)
  }

  val tableName: String = "PRESETS"

  private class PresetsTable(tag: Tag)
    extends Table[ClusterDetails](tag, tableName) {

    private val idColumn = "id"
    private val nameColumn = "name"
    private val clusterTypeColumn = "clusterType"
    private val uriColumn = "uri"
    private val userIPColumn = "userIP"
    private val hadoopUserColumn = "hadoopUser"
    private val isEditableColumn = "isEditable"
    private val isDefaultColumn = "isDefault"
    private val executorMemoryColumn = "executorMemory"
    private val totalExecutorCoresColumn = "totalExecutorCores"
    private val executorCoresColumn = "executorCores"
    private val numExecutorsColumn = "numExecutors"
    private val paramsColumn = "params"


    def id: Rep[Long] = column[Long](idColumn, O.PrimaryKey, O.AutoInc)
    def name: Rep[String] = column[String](nameColumn)
    def clusterType: Rep[String] = column[String](clusterTypeColumn)
    def uri: Rep[String] = column[String](uriColumn)
    def userIP: Rep[String] = column[String](userIPColumn)
    def hadoopUser: Rep[Option[String]] = column[Option[String]](hadoopUserColumn)
    def isEditable: Rep[Boolean] = column[Boolean](isEditableColumn)
    def isDefault: Rep[Boolean] = column[Boolean](isDefaultColumn)
    def executorMemory: Rep[Option[String]] = column[Option[String]](executorMemoryColumn)
    def totalExecutorCores: Rep[Option[Int]] = column[Option[Int]](totalExecutorCoresColumn)
    def executorCores: Rep[Option[Int]] = column[Option[Int]](executorCoresColumn)
    def numExecutors: Rep[Option[Int]] = column[Option[Int]](numExecutorsColumn)
    def params: Rep[Option[String]] = column[Option[String]](paramsColumn)


    override def * = (
      id.?,
      name,
      clusterType,
      uri,
      userIP,
      hadoopUser,
      isEditable,
      isDefault,
      executorMemory,
      totalExecutorCores,
      executorCores,
      numExecutors,
      params) <> (ClusterDetails.tupled, ClusterDetails.unapply)
  }

  private val presets = TableQuery[PresetsTable]


  def matchesError(ex: java.sql.SQLException, errorCode: Int): Boolean =
    ex.getErrorCode == errorCode

  object ErrorCodes {
    val UniqueViolation = 23505 // Defined in SQL Standard
  }

}
