/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.e2etests

import java.util.UUID

import io.deepsense.commons.utils.Logging
import io.deepsense.commons.models.ClusterDetails
import io.deepsense.sessionmanager.service.HostAddressResolver
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.ClusterType

object TestClusters extends Logging {

  def allAvailableClusters: Seq[ClusterDetails] = {
    val standalone = standaloneClusterPresetOpt()
    val mesos = mesosClusterPresetOpt()

    if(standalone.isEmpty) {
      logger.warn(
        """Standalone spark master ip is not provided.
          |To run tests against Standalone cluster set SPARK_STANDALONE_MASTER_IP env variable
        """.stripMargin
      )
    }
    if(mesos.isEmpty) {
      logger.warn(
        """Mesos master ip is not provided.
          |To run tests against Mesos cluster set MESOS_MASTER_IP env variable
        """.stripMargin
      )
    }

    Seq(
      mesos,
      standalone,
      Some(local)
    ).flatten
  }

  def local() = ClusterDetails(
    name = "some-local" + UUID.randomUUID(),
    id = None,
    executorMemory = Some("2G"),
    clusterType = ClusterType.local,
    uri = ignoredInLocal,
    userIP = ignoredInLocal
  )
  private val ignoredInLocal = ""

  private def standaloneClusterPresetOpt() = for {
    standaloneClusterMasterIp <- standaloneClusterMasterIpOpt()
  } yield ClusterDetails(
    name = "some-standalone" + UUID.randomUUID(),
    id = None,
    clusterType = ClusterType.standalone,
    uri = s"spark://$standaloneClusterMasterIp:7077",
    userIP = HostAddressResolver.getHostAddress()
  )

  private def standaloneClusterMasterIpOpt() = sys.env.get(sparkStandaloneEnv)
  private val sparkStandaloneEnv = "SPARK_STANDALONE_MASTER_IP"

  private def mesosClusterPresetOpt() = for {
    mesosClusterMasterIp <- mesosClusterMasterIpOpt()
  } yield ClusterDetails(
    name = "some-mesos" + UUID.randomUUID(),
    id = None,
    clusterType = ClusterType.mesos,
    uri = s"mesos://$mesosClusterMasterIp:5050",
    userIP = HostAddressResolver.getHostAddress()
  )

  private def mesosClusterMasterIpOpt() = sys.env.get(mesosStandaloneEnv)
  private val mesosStandaloneEnv = "MESOS_MASTER_IP"

}
