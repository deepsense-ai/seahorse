/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest.requests

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkAgumentParser
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkAgumentParser.UnknownOption

import scala.collection.Map
import scalaz.Validation

case class CreateSession(workflowId: Id, cluster: ClusterDetails)

case class ClusterDetails(
    name: String,
    id: String,
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
    params: Option[String] = None) {

  def parsedParams: Validation[UnknownOption, Map[String, String]] =
    SparkAgumentParser.parse(params).map(_.toMap)

}
