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

package ai.deepsense.commons.service.server

import java.time.Duration

import com.typesafe.config.Config

case class JettyConfig(
    port: Int,
    stopTimeout: Duration,
    connectorIdleTimeout: Duration,
    maxFormContentSize: Int)

object JettyConfig {
  def apply(jettyConfig: Config): JettyConfig = new JettyConfig(
    port = jettyConfig.getInt("port"),
    stopTimeout = jettyConfig.getDuration("stopTimeout"),
    connectorIdleTimeout = jettyConfig.getDuration("connectorIdleTimeout"),
    maxFormContentSize = jettyConfig.getInt("maxFormContentSize"))

}
