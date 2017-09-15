/*
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

    if(standalone.isEmpty) {
      logger.warn(
        """Standalone spark master ip is not provided.
          |To run tests against Standalone cluster set SPARK_STANDALONE_MASTER_IP env variable
        """.stripMargin
      )
    }

    Seq(
      standalone,
      Some(local)
    ).flatten
  }

  def local() = ClusterDetails(
    name = "some-local" + UUID.randomUUID(),
    id = None,
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

}
