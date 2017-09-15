/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.workflowexecutor.pythongateway

import scala.concurrent.Promise

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.api.java.JavaSparkContext

import io.deepsense.commons.utils.Logging

/**
  * An entry point to our application designed to be accessible by Python process.
  */
class PythonEntryPoint(val sparkContext: SparkContext) extends Logging {

  def getSparkContext: JavaSparkContext = sparkContext

  def getSparkConf: SparkConf = sparkContext.getConf

  private[pythongateway] val pythonPortPromise: Promise[Int] = Promise()

  def reportCallbackServerPort(port: Int): Unit = {
    pythonPortPromise.success(port)
  }
}
