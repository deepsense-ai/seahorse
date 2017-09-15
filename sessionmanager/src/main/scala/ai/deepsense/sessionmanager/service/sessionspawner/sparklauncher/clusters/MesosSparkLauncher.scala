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

import ai.deepsense.commons.buildinfo.BuildInfo
import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherConfig
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.SeahorseSparkLauncher.RichSparkLauncher
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.executor.CommonEnv
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser.SparkOptionsMultiMap
import org.apache.spark.launcher.SparkLauncher

private [clusters] object MesosSparkLauncher {
  import scala.collection.JavaConversions._

  val sparkVersion = BuildInfo.sparkVersion
  val hadoopVersion = BuildInfo.hadoopVersion

  def apply(applicationArgs: Seq[String],
            config: SparkLauncherConfig,
            clusterConfig: ClusterDetails,
            args: SparkOptionsMultiMap): SparkLauncher = {
    new SparkLauncher(env(config, clusterConfig))
      .setSparkArgs(args)
      .setVerbose(true)
      .setMainClass(config.className)
      .setMaster(clusterConfig.uri)
      .setDeployMode("client")
      .setAppResource(config.weJarPath)
      .setSparkHome(config.sparkHome)
      .setAppName("Seahorse Workflow Executor")
      .addAppArgs(applicationArgs: _*)
      .addFile(config.weDepsPath)
      .setConf("spark.executor.uri",
        s"http://d3kbcqa49mib13.cloudfront.net/spark-$sparkVersion-bin-hadoop$hadoopVersion.tgz")
  }

  private def env(
      config: SparkLauncherConfig,
      clusterConfig: ClusterDetails) = CommonEnv(config, clusterConfig) ++ Map(
    "MESOS_NATIVE_JAVA_LIBRARY" -> "/usr/lib/libmesos.so",
    "LIBPROCESS_IP" -> clusterConfig.userIP,
    "LIBPROCESS_ADVERTISE_IP" -> clusterConfig.userIP
  )

}
