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

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.Params
import io.deepsense.deeplang.{DKnowledge, DMethod1To1, DOperable, ExecutionContext}

/**
 * Able to transform a DataFrame into another DataFrame.
 * Can have mutable parameters.
 */
abstract class Transformer extends DOperable with Params with Logging {

  /**
   * Creates a transformed DataFrame based on input DataFrame.
   */
  private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame

  /**
   * Should be implemented in subclasses.
   * For known schema of input DataFrame, infers schema of output DataFrame.
   * If it is not able to do it for some reasons, it returns None.
   */
  private[deeplang] def _transformSchema(schema: StructType): Option[StructType] = None

  /**
    * Can be implemented in a subclass, if access to infer context is required.
    * For known schema of input DataFrame, infers schema of output DataFrame.
    * If it is not able to do it for some reasons, it returns None.
    */
  private[deeplang] def _transformSchema(
      schema: StructType,
      inferContext: InferContext): Option[StructType] = _transformSchema(schema)

  def transform: DMethod1To1[Unit, DataFrame, DataFrame] = {
    new DMethod1To1[Unit, DataFrame, DataFrame] {
      override def apply(ctx: ExecutionContext)(p: Unit)(df: DataFrame): DataFrame = {
        _transform(ctx, df)
      }

      override def infer(
        ctx: InferContext)(
        p: Unit)(
        k: DKnowledge[DataFrame]): (DKnowledge[DataFrame], InferenceWarnings) = {
        val df = DataFrame.forInference(k.single.schema.flatMap(s => _transformSchema(s, ctx)))
        (DKnowledge(df), InferenceWarnings.empty)
      }
    }
  }
}
