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

package ai.deepsense.deeplang.doperations

import scala.reflect.runtime.universe.TypeTag

import ai.deepsense.deeplang._
import ai.deepsense.deeplang.doperables.{Estimator, Transformer}
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.Param

abstract class EstimatorAsFactory[E <: Estimator[Transformer]]
  (implicit typeTagE: TypeTag[E])
  extends DOperation0To1[E] {

  val estimator: E = TypeUtils.instanceOfType(typeTagE)
  override lazy val tTagTO_0: TypeTag[E] = typeTag[E]
  override val specificParams: Array[Param[_]] = estimator.params

  setDefault(estimator.extractParamMap().toSeq: _*)

  override protected def execute()(context: ExecutionContext): E =
    updatedEstimator

  override def inferKnowledge()(context: InferContext): (DKnowledge[E], InferenceWarnings) = {
    (DKnowledge[E](updatedEstimator), InferenceWarnings.empty)
  }

  private def updatedEstimator: E = estimator.set(extractParamMap())
}
