/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest.requests

import io.deepsense.commons.models.Id

case class CreateSession(workflowId: Id, cluster: ClusterDetails)

case class ClusterDetails(
    clusterType: String,
    uri: String,
    executorMemory: Option[Double],
    totalExecutorCores: Option[Int],
    executorCores: Option[Int],
    numExecutors: Option[Int],
    params: Option[String])
