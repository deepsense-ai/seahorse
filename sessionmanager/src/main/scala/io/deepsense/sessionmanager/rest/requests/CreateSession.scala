/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest.requests

import io.deepsense.commons.models.Id

case class CreateSession(workflowId: Id, cluster: ClusterDetails)

case class ClusterDetails(
    name: String,
    id: String,
    clusterType: String,
    uri: String,
    userIP: Option[String] = None,
    hadoopUser: Option[String] = None,
    isEditable: Boolean = true,
    isDefault: Boolean = false,
    executorMemory: Option[Double] = None,
    totalExecutorCores: Option[Int] = None,
    executorCores: Option[Int] = None,
    numExecutors: Option[Int] = None,
    params: Option[String] = None)
