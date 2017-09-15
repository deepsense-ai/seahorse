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

package ai.deepsense.deeplang.doperables.wrappers

import org.apache.spark.ml.param.{Param, ParamMap}
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql
import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.Transformer
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.params.wrappers.deeplang.ParamWrapper
import ai.deepsense.sparkutils.ML

class TransformerWrapper(
    executionContext: ExecutionContext,
    transformer: Transformer)
  extends ML.Model[TransformerWrapper] {

  override def copy(extra: ParamMap): TransformerWrapper = {
    val params = ParamTransformer.transform(extra)
    val transformerCopy = transformer.replicate().set(params: _*)
    new TransformerWrapper(executionContext, transformerCopy)
  }

  override def transformDF(dataset: sql.DataFrame): sql.DataFrame = {
    transformer._transform(executionContext, DataFrame.fromSparkDataFrame(dataset.toDF()))
      .sparkDataFrame
  }

  override def transformSchema(schema: StructType): StructType = {
    transformer._transformSchema(schema).get
  }

  override lazy val params: Array[Param[_]] = {
    transformer.params.map(new ParamWrapper(uid, _))
  }

  override val uid: String = Identifiable.randomUID("TransformerWrapper")
}
