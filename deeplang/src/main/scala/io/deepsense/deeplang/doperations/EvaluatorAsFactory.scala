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
import io.deepsense.deeplang.documentation.OperationDocumentation
import io.deepsense.deeplang.doperables.Evaluator
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.Param

abstract class EvaluatorAsFactory[T <: Evaluator]
  (implicit typeTag: TypeTag[T])
  extends DOperation0To1[T] {

  val evaluator: T = TypeUtils.instanceOfType(typeTag)
  override lazy val tTagTO_0: TypeTag[T] = typeTag[T]
  override val params: Array[Param[_]] = evaluator.params

  setDefault(evaluator.extractParamMap().toSeq: _*)

  override protected def _execute(context: ExecutionContext)(): T =
    updatedEvaluator

  override def inferKnowledge(
      context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]])
        : (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    (Vector(DKnowledge[DOperable](updatedEvaluator)), InferenceWarnings.empty)
  }

  private def updatedEvaluator: T = evaluator.set(extractParamMap())
}
