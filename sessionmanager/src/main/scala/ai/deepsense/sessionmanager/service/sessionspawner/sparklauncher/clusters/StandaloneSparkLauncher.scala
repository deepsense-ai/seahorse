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

package ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import scalaz.Validation

import org.apache.spark.launcher.SparkLauncher

import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.sessionmanager.service.sessionspawner.SessionConfig
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherConfig
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.SeahorseSparkLauncher.RichSparkLauncher
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.executor.{CommonEnv, SessionExecutorArgs}
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser._

private [clusters] object StandaloneSparkLauncher {

  import scala.collection.JavaConversions._

  def apply(applicationArgs: Seq[String],
            config: SparkLauncherConfig,
            clusterConfig: ClusterDetails,
            args: SparkOptionsMultiMap): SparkLauncher = {
    new SparkLauncher(CommonEnv(config, clusterConfig))
      .setSparkArgs(args)
      .setVerbose(true)
      .setMainClass(config.className)
      .setMaster(clusterConfig.uri)
      .setDeployMode("client")
      .setAppResource(config.weJarPath)
      .setAppName("Seahorse Workflow Executor")
      .addAppArgs(applicationArgs: _*)
      .addFile(config.weDepsPath)
      .setConf("spark.driver.host", clusterConfig.userIP)
      .setConf("spark.executorEnv.PYTHONPATH", config.weDepsPath)
  }
}
