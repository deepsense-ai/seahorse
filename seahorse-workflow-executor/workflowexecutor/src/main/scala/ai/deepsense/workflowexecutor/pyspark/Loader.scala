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

import scala.reflect.io.Path

import ai.deepsense.commons.utils.Logging

object Loader extends Logging {

  def load: Option[String] = Option(System.getenv("SPARK_HOME")).map(pysparkPath)

  private def pysparkPath(sparkHome: String): String = {
    val path = Path(sparkHome)./("python").toAbsolute.toString()
    logger.info("Found PySpark at: {}", path)
    path
  }
}
