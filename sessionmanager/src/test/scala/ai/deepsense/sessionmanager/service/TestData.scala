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

package ai.deepsense.sessionmanager.service

import ai.deepsense.commons.models.ClusterDetails

object TestData {

  lazy val someClusterDetails = ClusterDetails(
    clusterType = "mesos",
    id = Some(1),
    name = "some-name",
    uri = "localhost",
    userIP = "127.0.0.1"
  )

  lazy val localClusterDetails = ClusterDetails(
    clusterType = "local",
    id = Some(2),
    name = "some-other-name",
    uri = "uri-doesnt-matter",
    userIP = "127.0.0.1"
  )

}
