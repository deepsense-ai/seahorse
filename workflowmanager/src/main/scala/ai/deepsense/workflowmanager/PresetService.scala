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

package ai.deepsense.workflowmanager


import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.name.Named

import ai.deepsense.commons.auth.{Authorizator, AuthorizatorProvider}
import ai.deepsense.commons.auth.usercontext.UserContext
import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.models.workflows.Workflow
import ai.deepsense.models.workflows.Workflow._
import ai.deepsense.workflowmanager.exceptions.{WorkflowNotFoundException, WorkflowOwnerMismatchException}
import ai.deepsense.workflowmanager.model.WorkflowPreset
import ai.deepsense.workflowmanager.storage.WorkflowStorage
import ai.deepsense.workflowmanager.storage.impl.PresetsDao


class PresetService @Inject()(presetStore: PresetsDao,
                              workflowStorage: WorkflowStorage,
                              authorizatorProvider: AuthorizatorProvider,
                              @Named("roles.workflows.update") roleUpdate: String)
                             (implicit ec: ExecutionContext) {



  def listPresets(): Future[Seq[ClusterDetails]] = {
    presetStore.getPresets
  }

  def createPreset(clusterConfig: ClusterDetails): Future[Long] = {
    presetStore.savePreset(clusterConfig)
  }

  def getPreset(presetId: Long): Future[Option[ClusterDetails]] = {
    presetStore.getPreset(presetId)
  }

  def updatePreset(presetId: Long, clusterConfig: ClusterDetails): Future[Long] = {
    presetStore.updatePreset(presetId, clusterConfig).map(_ => presetId)
  }

  def removePreset(presetId: Long): Future[Unit] = {
    presetStore.removePreset(presetId)
  }

  def saveWorkflowsPreset(userContextFuture: Future[UserContext], workflowId: Workflow.Id,
                          workflowPreset: WorkflowPreset): Future[Unit] = {
    val authorizator: Authorizator = authorizatorProvider.forContext(userContextFuture)
    authorizator.withRole(roleUpdate) {
      userContext => {
        workflowStorage.get(workflowId).flatMap {
          case Some(w) =>
              if (w.ownerId != userContext.user.id) {
                throw new WorkflowOwnerMismatchException(workflowId)
              }
              presetStore.saveWorkflowsPreset(workflowPreset: WorkflowPreset)
          case None => throw new WorkflowNotFoundException(workflowId)
        }
      }

    }
  }

  def getWorkflowsPreset(workflowId: Id): Future[Option[ClusterDetails]] = {
    presetStore.getWorkflowsPreset(workflowId)
  }
}
