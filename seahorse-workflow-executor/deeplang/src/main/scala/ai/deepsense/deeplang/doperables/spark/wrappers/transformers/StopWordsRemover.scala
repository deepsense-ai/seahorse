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

package ai.deepsense.deeplang.doperables.spark.wrappers.transformers

import org.apache.spark.ml.feature.{StopWordsRemover => SparkStopWordsRemover}
import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.doperables.SparkTransformerAsMultiColumnTransformer
import ai.deepsense.deeplang.inference.exceptions.SparkTransformSchemaException
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.wrappers.spark.BooleanParamWrapper

class StopWordsRemover extends SparkTransformerAsMultiColumnTransformer[SparkStopWordsRemover] {

  val caseSensitive = new BooleanParamWrapper[SparkStopWordsRemover](
    name = "case sensitive",
    description = Some("Whether to do a case sensitive comparison over the stop words."),
    sparkParamGetter = _.caseSensitive)
  setDefault(caseSensitive, false)

  override protected def getSpecificParams: Array[Param[_]] = Array(caseSensitive)

  // TODO: This override will not be necessary after fixing StopWordsRemover.transformSchema
  //       in Apache Spark code
  override def transformSingleColumnSchema(
      inputColumn: String,
      outputColumn: String,
      schema: StructType): Option[StructType] = {
    try {
      val inputFields = schema.fieldNames
      require(!inputFields.contains(outputColumn),
        s"Output column $outputColumn already exists.")
    } catch {
      case e: Exception => throw new SparkTransformSchemaException(e)
    }
    super.transformSingleColumnSchema(inputColumn, outputColumn, schema)
  }
}
