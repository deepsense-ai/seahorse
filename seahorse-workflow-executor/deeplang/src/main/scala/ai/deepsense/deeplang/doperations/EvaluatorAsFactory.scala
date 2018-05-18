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
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.Evaluator
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.Param

abstract class EvaluatorAsFactory[T <: Evaluator]
  (implicit typeTag: TypeTag[T])
  extends DOperation0To1[T] {

  val evaluator: T = TypeUtils.instanceOfType(typeTag)
  override lazy val tTagTO_0: TypeTag[T] = typeTag[T]
  override val specificParams: Array[Param[_]] = evaluator.params

  setDefault(evaluator.extractParamMap().toSeq: _*)

  override protected def execute()(context: ExecutionContext): T =
    updatedEvaluator

  override def inferKnowledge()(context: InferContext): (DKnowledge[T], InferenceWarnings) = {
    (DKnowledge[T](updatedEvaluator), InferenceWarnings.empty)
  }

  private def updatedEvaluator: T = evaluator.set(extractParamMap())
}
