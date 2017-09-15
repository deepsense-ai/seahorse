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

package ai.deepsense.workflowexecutor

import ai.deepsense.deeplang.doperations.exceptions.EmptyDataframeException
import ai.deepsense.deeplang.exceptions.DeepLangException

/**
  * Unfortunetely Spark exceptions are stringly typed. Spark does not have their exception classes.
  * This extractor hides Sparks strings and converts spark exceptions to deeplangs.
  */
object SparkExceptionAsDeeplangException {
  def unapply(exception: Exception): Option[DeepLangException] = exception match {
    case emptyCollectionEx if emptyCollectionEx.getMessage == "empty collection" =>
      Some(EmptyDataframeException)
    case unknown => None
  }
}
