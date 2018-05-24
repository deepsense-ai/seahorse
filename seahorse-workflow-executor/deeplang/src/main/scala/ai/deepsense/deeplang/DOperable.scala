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

package ai.deepsense.deeplang

import ai.deepsense.deeplang.doperables.descriptions.HasInferenceResult
import ai.deepsense.deeplang.doperables.report.Report

/**
 * Objects of classes with this trait can be used in two ways.
 * 1. It can be object on which you can perform DOperations.
 * 2. It can be used to infer knowledge about objects that will be used later in workflow,
 * before it's execution.
 * DOperable that can be used for execution can ALWAYS be used for inference, but not vice-versa.
 */
trait DOperable extends HasInferenceResult {
  def report(extended: Boolean = true): Report = Report()
}
