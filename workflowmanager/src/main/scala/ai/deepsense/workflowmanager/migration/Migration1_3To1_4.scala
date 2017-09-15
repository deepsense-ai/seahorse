/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowmanager.migration

import java.net.URL
import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

import akka.actor.ActorSystem

import ai.deepsense.commons.utils.{Logging, Version}
import ai.deepsense.models.workflows.Workflow
import ai.deepsense.workflowmanager.storage.WorkflowStorage
import ai.deepsense.workflowmanager.versionconverter.VersionConverter

class Migration1_3To1_4 private (
    val datasourcemanagerUrl: URL,
    val workflowStorage: WorkflowStorage,
    val actorSystem: ActorSystem)
    extends Logging
    with SeahorseDbMigration {

  implicit val ec: ExecutionContext = actorSystem.dispatcher

  override val previousVersion = Version(1, 3, 0)
  override val targetVersion = Version(1, 4, 0)

  def migrate(): Future[Unit] = {

    val updatedWorkflowsAndNewDatasourcesFut: Future[Seq[MigrationResult]] = for {
      workflows <- workflowStorage.getAllRaw

    } yield {
      for {
        (id, raw) <- workflows.toSeq if isConvertible(id, raw)
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
        logger.info(s"Migration to ${targetVersion.humanReadable} finished")
      }
    }
  }

}

object Migration1_3To1_4 {
  def apply(datasourcemanagerUrl: URL, workflowStorage: WorkflowStorage, actorSystem: ActorSystem): Migration1_3To1_4 =
    new Migration1_3To1_4(datasourcemanagerUrl, workflowStorage, actorSystem)
}
