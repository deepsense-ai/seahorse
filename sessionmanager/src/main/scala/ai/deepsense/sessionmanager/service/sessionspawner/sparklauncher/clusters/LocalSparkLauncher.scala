/**
 * Copyright 2016, deepsense.ai
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

package ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import org.apache.spark.launcher.SparkLauncher

import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherConfig
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.SeahorseSparkLauncher.RichSparkLauncher
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.executor.CommonEnv
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser._

private [clusters] object LocalSparkLauncher {

  import scala.collection.JavaConversions._

  def apply(applicationArgs: Seq[String],
            config: SparkLauncherConfig,
            clusterConfig: ClusterDetails,
            args: SparkOptionsMultiMap): SparkLauncher = {
    new SparkLauncher(env(config, clusterConfig))
      .setSparkArgs(args)
      .setVerbose(true)
      .setMainClass(config.className)
      .setMaster("local[*]")
      .setDeployMode("client")
      .setAppResource(config.weJarPath)
      .setAppName("Seahorse Workflow Executor")
      .addAppArgs(applicationArgs: _*)
      .addFile(config.weDepsPath)
      .setConf("spark.executorEnv.PYTHONPATH", config.weDepsPath)
      .setConf("spark.default.parallelism", parallelism.toString)
  }

  private def env(config: SparkLauncherConfig,
                  clusterConfig: ClusterDetails) = CommonEnv(config, clusterConfig) ++ Map(
    // For local cluster driver python binary IS executors python binary
    "PYSPARK_PYTHON" -> config.pythonDriverBinary
  )

  /**
    * We put a limit parallelism on maximum number of partitions in local mode.
    * Local mode is used with limited memory.
    * With limited memory too large number of partitions will cause problems with GC.
    * 4 seems like a good maximum number of partitions for small datasets and 1GB memory.
    */
  private lazy val parallelism = {
    val availableCoresNumber: Int = Runtime.getRuntime.availableProcessors()
    math.min(availableCoresNumber, 4)
  }
}
