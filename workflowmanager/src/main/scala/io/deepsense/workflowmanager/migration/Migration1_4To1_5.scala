/**
 * Copyright 2017, deepsense.ai
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

package io.deepsense.workflowmanager.migration

import java.net.URL
import java.util.UUID

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

import akka.actor.ActorSystem

import io.deepsense.api.datasourcemanager.model.Datasource
import io.deepsense.commons.utils.{Logging, Version}
import io.deepsense.workflowmanager.storage.WorkflowStorage
import io.deepsense.workflowmanager.versionconverter.WorkflowMetadataConverter


class Migration1_4To1_5 private (
    val datasourcemanagerUrl: URL,
    val workflowStorage: WorkflowStorage,
    val actorSystem: ActorSystem)
    extends Logging
    with SeahorseDbMigration {
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  override val previousVersion = Version(1, 4, 0)
  override val targetVersion = Version(1, 5, 0)

  def migrate(): Future[Unit] = {

    val updatedWorkflows: Future[Seq[MigrationResult]] = for {
      workflows <- workflowStorage.getAllRaw
    } yield {
      for {
        (id, raw) <- workflows.toSeq if isConvertible(id, raw)
      } yield {
        logSuccesfullConversion(id)
        val rawWorkflow = WorkflowMetadataConverter.setWorkflowVersion(raw.workflow, targetVersion)
        MigrationResult(
          id,
          UUID.fromString(raw.ownerId),
          raw.ownerName,
          rawWorkflow,
          List.empty[Datasource])
      }
    }

    for {
      migrationFutures <- updatedWorkflows.map(commitMigrationsToDb)
    } yield {
      for {
        migration <- migrationFutures
      } {

        migration.onFailure {
          case t => logger.error("Unable to migrate workflow", t)
        }
        Await.ready(migration, Duration.Inf)
        logger.info(s"Migration to ${targetVersion.humanReadable} finished")
      }
    }
  }
}

object Migration1_4To1_5 {
  def apply(datasourcemanagerUrl: URL, workflowStorage: WorkflowStorage, actorSystem: ActorSystem): Migration1_4To1_5 =
    new Migration1_4To1_5(datasourcemanagerUrl, workflowStorage, actorSystem)
}
