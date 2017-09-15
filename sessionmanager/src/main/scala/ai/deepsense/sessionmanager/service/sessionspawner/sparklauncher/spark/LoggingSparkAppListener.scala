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

package ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark

import scala.concurrent.{ExecutionContext, Future, Promise}

import org.apache.spark.launcher.SparkAppHandle
import org.apache.spark.launcher.SparkAppHandle.State

import ai.deepsense.commons.utils.Logging

class LoggingSparkAppListener extends SparkAppHandle.Listener with Logging {

  override def infoChanged(handle: SparkAppHandle): Unit = {
    logger.info(s"App ${handle.getAppId} info changed: ${handle.toString}")
  }

  override def stateChanged(handle: SparkAppHandle): Unit = {
    logger.info(s"App ${handle.getAppId} state changed: ${handle.getState}")
  }
}
