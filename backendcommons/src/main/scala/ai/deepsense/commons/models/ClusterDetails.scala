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

package ai.deepsense.commons.models

case class ClusterDetails(
    id: Option[Long],
    name: String,
    clusterType: String, // TODO Enum
    uri: String,
    userIP: String,
    hadoopUser: Option[String] = None,
    isEditable: Boolean = true,
    isDefault: Boolean = false,
    driverMemory: Option[String] = None,
    executorMemory: Option[String] = None,
    totalExecutorCores: Option[Int] = None,
    executorCores: Option[Int] = None,
    numExecutors: Option[Int] = None,
    params: Option[String] = None)


