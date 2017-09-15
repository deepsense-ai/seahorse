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

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.custom.InnerWorkflow
import io.deepsense.graph._

case class CustomTransformer(
    innerWorkflow: InnerWorkflow,
    override val params: Array[Param[_]])
  extends Transformer {

  def this() = this(InnerWorkflow.empty, Array.empty)

  override private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    ctx.innerWorkflowExecutor.execute(CommonExecutionContext(ctx), innerWorkflow, df)
  }

  override private[deeplang] def _transformSchema(
      schema: StructType, inferCtx: InferContext): Option[StructType] = {
    val initialKnowledge = GraphKnowledge(Map(
      innerWorkflow.source.id -> NodeInferenceResult(
        Vector(DKnowledge(DataFrame.forInference(schema))))
    ))

    innerWorkflow.graph.inferKnowledge(inferCtx, initialKnowledge)
      .getKnowledge(innerWorkflow.sink.id)(0).asInstanceOf[DKnowledge[DataFrame]].single.schema
  }
}
