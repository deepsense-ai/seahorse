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

package ai.deepsense.deeplang.exceptions

import ai.deepsense.commons.exception.{FailureDescription, FailureCode, DeepSenseException}

class DeepLangException(
    override val message: String,
    cause: Throwable = null)
  extends DeepSenseException(
    FailureCode.NodeFailure,
    "DeepLang Exception",
    message,
    Option(cause),
    Option(cause)
      .map(e => FailureDescription.stacktraceDetails(e.getStackTrace))
      .getOrElse(Map())) {

  def toVector: Vector[DeepLangException] = Vector(this)
}
