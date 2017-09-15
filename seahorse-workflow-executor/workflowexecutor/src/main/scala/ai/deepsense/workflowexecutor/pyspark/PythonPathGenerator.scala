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

package ai.deepsense.workflowexecutor.pyspark

import scala.collection.JavaConverters._

import com.typesafe.config.ConfigFactory


class PythonPathGenerator(
  private val pySparkPath: String
) {

  private val config = ConfigFactory.load.getConfig("pyspark")
  private val additionalPythonPath: Seq[String] = config.getStringList("python-path").asScala
  private val additionalPaths = additionalPythonPath.map(p => s"$pySparkPath/$p")
  private val pythonPathEnvKey = "PYTHONPATH"
  private val envPythonPath = Option(System.getenv().get(pythonPathEnvKey))

  val generatedPythonPath: Seq[String] = pySparkPath +: (additionalPaths ++ envPythonPath)

  def pythonPath(additionalPaths: String*): String =
    (generatedPythonPath ++ additionalPaths).mkString(":")

  def env(additionalPaths: String*): (String, String) =
    (pythonPathEnvKey, pythonPath(additionalPaths: _*))
}
