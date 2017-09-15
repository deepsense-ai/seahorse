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

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{Estimator, Transformer}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.{DKnowledge, DOperation1To2, ExecutionContext, TypeUtils}

abstract class EstimatorAsOperation[E <: Estimator[T] : TypeTag, T <: Transformer : TypeTag]
  extends DOperation1To2[DataFrame, DataFrame, T] {

  val estimator: E = TypeUtils.instanceOfType(typeTag[E])

  val params = estimator.params

  setDefault(estimator.extractParamMap().toSeq: _*)

  override protected def _execute(
      context: ExecutionContext)(
      t0: DataFrame): (DataFrame, T) = {
    val transformer = estimatorWithParams().fit(context)(())(t0)
    val transformedDataFrame = transformer.transform(context)(())(t0)
    (transformedDataFrame, transformer)
  }

  override protected def _inferKnowledge(
      context: InferContext)(
      k0: DKnowledge[DataFrame])
    : ((DKnowledge[DataFrame], DKnowledge[T]), InferenceWarnings) = {

    val (transformerKnowledge, fitWarnings) = estimatorWithParams().fit.infer(context)(())(k0)
    val (dataFrameKnowledge, transformWarnings) =
      transformerKnowledge.single.transform.infer(context)(())(k0)
    val warnings = fitWarnings ++ transformWarnings
    ((dataFrameKnowledge, transformerKnowledge), warnings)
  }

  private def estimatorWithParams() = {
    val estimatorWithParams = estimator.set(extractParamMap())
    validateDynamicParams(estimatorWithParams)
    estimatorWithParams
  }
}
