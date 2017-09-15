/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations.exceptions

import scala.compat.Platform.EOL

import org.apache.spark.SparkException

import ai.deepsense.commons.exception.{DeepSenseException, FailureCode}

case class WriteFileException(path: String, e: SparkException)
  extends DeepSenseException(
    code = FailureCode.NodeFailure,
    title = "WriteFileException",
    message = s"Unable to write file: $path",
    cause = Some(e),
    details = Map(
      "stacktrace" -> (e.getMessage + EOL + EOL + e.getStackTrace.mkString("", EOL, EOL))
    ))
