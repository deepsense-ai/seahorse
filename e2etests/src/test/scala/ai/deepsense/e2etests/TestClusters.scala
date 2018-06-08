/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.e2etests

import java.util.UUID

import ai.deepsense.commons.utils.Logging
import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.sessionmanager.service.HostAddressResolver
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.ClusterType

object TestClusters extends Logging {

  def allAvailableClusters: Seq[ClusterDetails] = {
    val standalone = standaloneClusterPresetOpt()
    val mesos = mesosClusterPresetOpt()
    val yarn = yarnClusterPresetOpt()

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
    if(yarn.isEmpty) {
      logger.warn(
        """YARN master ip is not provided.
          |To run tests against YARN cluster set YARN_MASTER_IP env variable
        """.stripMargin
      )
    }
    // TODO We don't test on standalone and mesos now
    // Add them later on
    val _ = Seq(
      standalone,
      yarn,
      mesos)

    Seq(Some(local), yarn).flatten
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


  private def yarnClusterPresetOpt() = for {
    yarnClusterMasterIp <- yarnClusterMasterIpOpt()
  } yield ClusterDetails(
    name = "some-yarn" + UUID.randomUUID(),
    id = None,
    clusterType = ClusterType.yarn,
    hadoopUser = Some("hdfs"),
    uri = "/resources/data/hadoop",
    userIP = HostAddressResolver.getHostAddress()
  )

  private def yarnClusterMasterIpOpt() = sys.env.get(yarnEnv)
  private val yarnEnv = "YARN_MASTER_IP"
}
