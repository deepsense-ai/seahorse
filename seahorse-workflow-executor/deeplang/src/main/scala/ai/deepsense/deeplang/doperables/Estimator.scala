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

package ai.deepsense.deeplang.doperables

import scala.reflect.runtime.universe.TypeTag

import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.Params
import ai.deepsense.deeplang.{DKnowledge, DMethod1To1, DOperable, ExecutionContext}
import ai.deepsense.reportlib.model.ReportType

/**
 * Can create a Transformer of type T based on a DataFrame.
 */
abstract class Estimator[+T <: Transformer]
    ()(implicit typeTag: TypeTag[T])
  extends DOperable
  with Params {

  def convertInputNumericToVector: Boolean = false
  def convertOutputVectorToDouble: Boolean = false

  /**
   * Creates a Transformer based on a DataFrame.
   */
  private[deeplang] def _fit(ctx: ExecutionContext, df: DataFrame): T

  /**
   * Creates an instance of Transformer for inference.
    *
    * @param schema the schema for inference, or None if it's unknown.
   */
  private[deeplang] def _fit_infer(schema: Option[StructType]): T

  def fit: DMethod1To1[Unit, DataFrame, T] = {
    new DMethod1To1[Unit, DataFrame, T] {
      override def apply(ctx: ExecutionContext)(p: Unit)(df: DataFrame): T = {
        _fit(ctx, df)
      }

      override def infer(ctx: InferContext)(p: Unit)(k: DKnowledge[DataFrame])
      : (DKnowledge[T], InferenceWarnings) = {
        val transformer = _fit_infer(k.single.schema)
        (DKnowledge(transformer), InferenceWarnings.empty)
      }
    }
  }

  override def report(extended: Boolean = true): Report =
    super.report(extended)
      .withReportName(s"$estimatorName Report")
      .withReportType(ReportType.Estimator)
      .withAdditionalTable(CommonTablesGenerators.params(extractParamMap()))

  protected def estimatorName: String = this.getClass.getSimpleName
}
