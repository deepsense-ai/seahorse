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

package io.deepsense.deeplang.doperables

import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.Params
import io.deepsense.deeplang.{DKnowledge, DMethod1To1, DOperable, ExecutionContext}

/**
 * Can create a Transformer based on a DataFrame.
 */
abstract class Estimator extends DOperable with Params {

  /**
   * Creates a Transformer based on a DataFrame.
   */
  private[deeplang] def _fit(df: DataFrame): Transformer

  /**
   * Creates an instance of Transformer for inference.
   * @param schema the schema for inference, or None if it's unknown.
   */
  private[deeplang] def _fit_infer(schema: Option[StructType]): Transformer

  def fit: DMethod1To1[Unit, DataFrame, Transformer] = {
    new DMethod1To1[Unit, DataFrame, Transformer] {
      override def apply(ctx: ExecutionContext)(p: Unit)(df: DataFrame): Transformer = {
        _fit(df)
      }

      override def infer(ctx: InferContext)(p: Unit)(k: DKnowledge[DataFrame])
      : (DKnowledge[Transformer], InferenceWarnings) = {
        val transformer = _fit_infer(k.single.schema)
        (DKnowledge(transformer), InferenceWarnings.empty)
      }
    }
  }
}
