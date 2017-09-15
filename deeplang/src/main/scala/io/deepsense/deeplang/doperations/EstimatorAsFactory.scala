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

package io.deepsense.deeplang.doperations

import scala.reflect.runtime.universe.TypeTag

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.{Transformer, Estimator}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.Param

abstract class EstimatorAsFactory[E <: Estimator[Transformer]]
  (implicit typeTagE: TypeTag[E])
  extends DOperation0To1[E] {

  val estimator: E = TypeUtils.instanceOfType(typeTagE)
  override lazy val tTagTO_0: TypeTag[E] = typeTag[E]
  override val params: Array[Param[_]] = estimator.params

  setDefault(estimator.extractParamMap().toSeq: _*)

  override protected def _execute(context: ExecutionContext)(): E =
    updatedEstimator

  override def inferKnowledge(
      context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]])
        : (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    (Vector(DKnowledge[DOperable](updatedEstimator)), InferenceWarnings.empty)
  }

  private def updatedEstimator: E = estimator.set(extractParamMap())
}
