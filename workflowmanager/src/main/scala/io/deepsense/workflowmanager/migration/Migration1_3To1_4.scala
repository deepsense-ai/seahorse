/**
 * Copyright (c) 2017, CodiLime Inc.
 */

package io.deepsense.workflowmanager.migration

import java.net.URL
import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scalaz.{Validation, ValidationNel}

import akka.actor.ActorSystem
import akka.util.Timeout
import spray.json.JsValue

import io.deepsense.commons.rest.client.RestClient
import io.deepsense.commons.rest.client.datasources.DatasourceTypes.DatasourceList
import io.deepsense.commons.utils.Logging
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.migration.Migration1_3To1_4.MigrationResult
import io.deepsense.workflowmanager.storage.WorkflowStorage
import io.deepsense.workflowmanager.versionconverter.{DatasourcesClient, VersionConverter}

class Migration1_3To1_4 private (
    val datasourcemanagerUrl: URL,
    val workflowStorage: WorkflowStorage,
    val actorSystem: ActorSystem) extends Logging {

  implicit val ec: ExecutionContext = actorSystem.dispatcher

  def waitForDatasourceManager(): Future[Unit] = {
    DatasourceManagerPoller(actorSystem, new {
      override implicit val timeout: Timeout = 1 minute
      override implicit val as: ActorSystem = actorSystem
    } with RestClient {
      override def apiUrl: URL = datasourcemanagerUrl

      override def userName = None

      override def credentials = None

      override def userId: Option[UUID] = Some(UUID.fromString("0-0-0-0-0"))
    }).tryWork
  }

  def migrate(): Future[Unit] = {

    val updatedWorkflowsAndNewDatasourcesFut: Future[Seq[MigrationResult]] = for {
      workflows <- workflowStorage.getAllRaw
    } yield {
      for {
        (id, raw) <- workflows.toSeq
        if {
          val workflowVersion = VersionConverter.extractWorkflowVersion(raw.workflow)
          val isVersion13 = VersionConverter.isVersion13(raw.workflow)

          if (!isVersion13 && !VersionConverter.isVersion14(raw.workflow)) {
            logger.warn(s"Workflow $id in version $workflowVersion cannot be migrated to version 1.4. Ignoring.")
          }

          isVersion13
        }
      } yield {
        logger.info(s"Found version 1.3.x workflow: $id - will perform conversion to current version 1.4")
        val (rawWorkflow, newDatasources) = VersionConverter.convert13to14(raw.workflow, raw.ownerId, raw.ownerName)
        MigrationResult(
          id,
          UUID.fromString(raw.ownerId),
          raw.ownerName,
          rawWorkflow,
          newDatasources)
      }
    }

    for {
      migrationFutures <- updatedWorkflowsAndNewDatasourcesFut.map(commitMigrationsToDb)
    } yield {
      for {
        migration <- migrationFutures
      } {

        migration.onFailure {
          case t => logger.error("Unable to migrate workflow", t)
        }

        Await.ready(migration, Duration.Inf)
      }
    }
  }

  private implicit def tryToValidation[R](`try`: util.Try[R]): Validation[Throwable, R] = {
    `try` match {
      case util.Success(a) => scalaz.Success(a)
      case util.Failure(t) => scalaz.Failure(t)
    }
  }

  private def commitMigrationsToDb(migrationResults: Seq[MigrationResult]): Seq[Future[Unit]] =
    migrationResults.map(commitMigrationToDb)

  private def commitMigrationToDb(migrationResult: MigrationResult): Future[Unit] = {

    val MigrationResult(id, ownerId, ownerName, updatedWorkflow, newDatasources) = migrationResult

    val datasourcesClient = new DatasourcesClient(datasourcemanagerUrl, ownerId, ownerName)

    val datasourcesMigrations = for {
      newDatasource <- newDatasources
    } yield {
      datasourcesClient.insertDatasource(UUID.fromString(newDatasource.getId), newDatasource.getParams)
        .toValidationNel
    }

    import scalaz.Applicative
    import scalaz.std.list.listInstance

    val datasourceValidation = Applicative[({type L[alpha] = ValidationNel[Throwable, alpha]})#L]
      .sequence[Unit, List](datasourcesMigrations)

    datasourceValidation match {
      case scalaz.Success(_) =>
        logger.info(s"Successfully updated workflow $id to version 1.4, " +
            s"added ${newDatasources.size} new datasource(s)")
        workflowStorage.updateRaw(id, updatedWorkflow)

      case scalaz.Failure(nel) => Future.failed(new RuntimeException(
          s"There were errors when trying to insert datasources for workflow $id: " +
            s"${nel.map(_.getMessage).list.toList.mkString("\n")}"))
    }
  }
}

object Migration1_3To1_4 {
  def apply(datasourcemanagerUrl: URL, workflowStorage: WorkflowStorage, actorSystem: ActorSystem): Migration1_3To1_4 =
    new Migration1_3To1_4(datasourcemanagerUrl, workflowStorage, actorSystem)

  private case class MigrationResult(
      id: Workflow.Id,
      ownerId: UUID,
      ownerName: String,
      rawWorkflow: JsValue,
      newDatasources: DatasourceList
  )
}
