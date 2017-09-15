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

package ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.executor

import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherConfig

object CommonEnv {

  def apply(config: SparkLauncherConfig,
            clusterConfig: ClusterDetails): Map[String, String] = Map(
    "HADOOP_USER_NAME" -> clusterConfig.hadoopUser.getOrElse("root"),
    "PYSPARK_DRIVER_PYTHON" -> config.pythonDriverBinary,
    "PYSPARK_PYTHON" -> config.pythonExecutorBinary,
    "SPARK_LOCAL_IP" -> clusterConfig.userIP
  )

}
