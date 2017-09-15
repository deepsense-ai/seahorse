/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.workflowmanager


import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.models.workflows.Workflow._
import io.deepsense.workflowmanager.model.WorkflowPreset
import io.deepsense.workflowmanager.storage.impl.PresetsDao


class PresetService @Inject()(presetStore: PresetsDao)(implicit ec: ExecutionContext) {
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

  def saveWorkflowsPreset(workflowPreset: WorkflowPreset): Future[Unit] = Future {
    presetStore.saveWorkflowsPreset(workflowPreset: WorkflowPreset)
  }

  def getWorkflowsPreset(workflowId: Id): Future[Option[ClusterDetails]] = {
    presetStore.getWorkflowsPreset(workflowId)
  }
}
