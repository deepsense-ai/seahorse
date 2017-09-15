/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor.clusterspawner

import scala.util.Try

import org.apache.hadoop.yarn.api.records.ApplicationId
import org.apache.hadoop.yarn.client.api.YarnClient

import io.deepsense.graphexecutor.Constants
import io.deepsense.models.experiments.Experiment

trait ClusterSpawner {
  def spawnOnCluster(
      experimentId: Experiment.Id,
      graphExecutionStatusesActorPath: String,
      esFactoryName: String = "default",
      applicationConfLocation: String = Constants.GraphExecutorConfigLocation):
    Try[(YarnClient, ApplicationId)]
}
