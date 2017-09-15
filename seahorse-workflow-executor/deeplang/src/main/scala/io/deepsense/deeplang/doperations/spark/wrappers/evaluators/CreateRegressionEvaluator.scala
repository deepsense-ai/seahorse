/**
 * Copyright 2015, deepsense.ai
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

package io.deepsense.deeplang.doperations.spark.wrappers.evaluators

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.documentation.SparkOperationDocumentation
import io.deepsense.deeplang.doperables.spark.wrappers.evaluators.RegressionEvaluator
import io.deepsense.deeplang.doperations.EvaluatorAsFactory

class CreateRegressionEvaluator
  extends EvaluatorAsFactory[RegressionEvaluator]
  with SparkOperationDocumentation {

  override val id: Id = "d9c3026c-a3d0-4365-8d1a-464a656b72de"
  override val name: String = "Regression Evaluator"
  override val description: String = "Creates a regression evaluator"

  override protected[this] val docsGuideLocation =
    Some("mllib-evaluation-metrics.html#regression-model-evaluation")
  override val since: Version = Version(1, 0, 0)
}
