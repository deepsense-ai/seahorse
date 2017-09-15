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

package ai.deepsense.deeplang.doperations.spark.wrappers.evaluators

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.documentation.SparkOperationDocumentation
import ai.deepsense.deeplang.doperables.spark.wrappers.evaluators.MulticlassClassificationEvaluator
import ai.deepsense.deeplang.doperations.EvaluatorAsFactory

class CreateMulticlassClassificationEvaluator
  extends EvaluatorAsFactory[MulticlassClassificationEvaluator]
  with SparkOperationDocumentation {

  override val id: Id = "3129848c-8a1c-449e-b006-340fec5b42ae"
  override val name: String = "Multiclass Classification Evaluator"
  override val description: String = "Creates a multiclass classification evaluator. " +
    "Multiclass classification evaluator does not assume any label class is special, " +
    "thus it cannot be used for calculation of metrics specific for binary classification " +
    "(where this assumption is taken into account)."

  override protected[this] val docsGuideLocation =
    Some("mllib-evaluation-metrics.html#multiclass-classification")
  override val since: Version = Version(1, 0, 0)
}
