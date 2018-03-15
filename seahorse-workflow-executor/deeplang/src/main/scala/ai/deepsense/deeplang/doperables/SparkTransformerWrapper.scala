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

import scala.reflect.runtime.universe._

import org.apache.spark.ml
import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.inference.exceptions.SparkTransformSchemaException
import ai.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers
import ai.deepsense.deeplang.{ExecutionContext, TypeUtils}

/**
 * This class creates a Deeplang Transformer from a Spark ML Transformer.
 * We assume that every Spark Transformer has a no-arg constructor.
 *
 * @tparam T Wrapped Spark transformer type
 */
abstract class SparkTransformerWrapper[T <: ml.Transformer](implicit tag: TypeTag[T])
  extends Transformer
  with ParamsWithSparkWrappers {

  lazy val sparkTransformer: T = TypeUtils.instanceOfType(tag)

  override protected def applyTransform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    val paramMap = sparkParamMap(sparkTransformer, df.sparkDataFrame.schema)
    DataFrame.fromSparkDataFrame(
      sparkTransformer.transform(df.sparkDataFrame, paramMap))
  }

  override protected def applyTransformSchema(schema: StructType): Option[StructType] = {
    val paramMap = sparkParamMap(sparkTransformer, schema)
    val transformerForInference = sparkTransformer.copy(paramMap)

    try {
      Some(transformerForInference.transformSchema(schema))
    } catch {
      case e: Exception => throw SparkTransformSchemaException(e)
    }
  }
}
