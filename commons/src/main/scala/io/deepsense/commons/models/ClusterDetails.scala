/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.commons.models

case class ClusterDetails(
    id: Option[Long],
    name: String,
    clusterType: String, // TODO Enum
    uri: String,
    userIP: String,
    hadoopUser: Option[String] = None,
    isEditable: Boolean = true,
    isDefault: Boolean = false,
    executorMemory: Option[String] = None,
    totalExecutorCores: Option[Int] = None,
    executorCores: Option[Int] = None,
    numExecutors: Option[Int] = None,
    params: Option[String] = None)


