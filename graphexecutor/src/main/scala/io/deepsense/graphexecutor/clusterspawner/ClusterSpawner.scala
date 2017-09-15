/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor.clusterspawner

import scala.util.Try

import org.apache.hadoop.yarn.api.records.ApplicationId
import org.apache.hadoop.yarn.client.api.YarnClient

import io.deepsense.models.workflows.Workflow

trait ClusterSpawner {
  def spawnOnCluster(
      experimentId: Workflow.Id,
      graphExecutionStatusesActorPath: String,
      esFactoryName: String = "default"):
    Try[(YarnClient, ApplicationId)]
}
