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

import io.deepsense.deeplang.documentation.OperationDocumentation
import io.deepsense.deeplang.doperables.Transformer
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.{DKnowledge, DOperation1To2, ExecutionContext, TypeUtils}

abstract class TransformerAsOperation[T <: Transformer]
    ()(implicit tag: TypeTag[T])
  extends DOperation1To2[DataFrame, DataFrame, T] {

  val transformer: T = TypeUtils.instanceOfType(tag)

  val params = transformer.params

  setDefault(transformer.extractParamMap().toSeq: _*)

  override protected def execute(t0: DataFrame)(context: ExecutionContext): (DataFrame, T) = {
    transformer.set(extractParamMap())
    (transformer.transform(context)(())(t0), transformer)
  }

  override protected def inferKnowledge(dfKnowledge: DKnowledge[DataFrame])(ctx: InferContext)
    : ((DKnowledge[DataFrame], DKnowledge[T]), InferenceWarnings) = {

    transformer.set(extractParamMap())
    val (outputDfKnowledge, warnings) = transformer.transform.infer(ctx)(())(dfKnowledge)
    ((outputDfKnowledge, DKnowledge(transformer)), warnings)
  }

  override lazy val tTagTI_0: TypeTag[DataFrame] = typeTag
  override lazy val tTagTO_0: TypeTag[DataFrame] = typeTag
}
