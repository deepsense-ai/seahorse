/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.workflowmanager


import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.name.Named

import io.deepsense.commons.auth.{Authorizator, AuthorizatorProvider}
import io.deepsense.commons.auth.usercontext.UserContext
import io.deepsense.commons.models.ClusterDetails
import io.deepsense.models.workflows.Workflow
import io.deepsense.models.workflows.Workflow._
import io.deepsense.workflowmanager.exceptions.{WorkflowNotFoundException, WorkflowOwnerMismatchException}
import io.deepsense.workflowmanager.model.WorkflowPreset
import io.deepsense.workflowmanager.storage.WorkflowStorage
import io.deepsense.workflowmanager.storage.impl.PresetsDao


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

  def updatePreset(presetId: Long, clusterConfig: ClusterDetails): Future[Long] = Future {
    presetStore.updatePreset(presetId, clusterConfig)
    presetId
  }

  def removePreset(presetId: Long): Future[Unit] = Future {
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
