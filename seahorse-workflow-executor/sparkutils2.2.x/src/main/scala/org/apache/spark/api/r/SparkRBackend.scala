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

package org.apache.spark.api.r


class SparkRBackend {

  private val backend: RBackend = new RBackend()
  private val backendThread: Thread = new Thread("SparkRBackend") {
    override def run(): Unit = backend.run()
  }

  private var portNumber: Int = _
  private var entryPointTrackingId: String = _

  def start(entryPoint: Object): Unit = {
    entryPointTrackingId = backend.jvmObjectTracker.addAndGetId(entryPoint).id
    portNumber = backend.init()
    backendThread.start()
  }

  def close(): Unit = {
    backend.close()
    backendThread.join()
  }

  def port: Int = portNumber

  def entryPointId: String = entryPointTrackingId
}
