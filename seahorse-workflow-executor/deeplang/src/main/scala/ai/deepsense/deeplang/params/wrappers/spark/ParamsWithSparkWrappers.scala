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

package ai.deepsense.deeplang.params.wrappers.spark

import org.apache.spark.ml
import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.params.Params

trait ParamsWithSparkWrappers extends Params {
  lazy val sparkParamWrappers: Array[SparkParamWrapper[_, _, _]] = params.collect {
    case wrapper: SparkParamWrapper[_, _, _] => wrapper +: wrapper.nestedWrappers
  }.flatten

  protected def validateSparkEstimatorParams(
      sparkEntity: ml.param.Params,
      maybeSchema: Option[StructType]): Unit = {
    maybeSchema.foreach(schema => sparkParamMap(sparkEntity, schema))
  }

  /**
    * This method extracts Spark parameters from SparkParamWrappers that are:
    * - declared directly in class which mixes this trait in
    * - declared in values of parameters (i.e. ChoiceParam, MultipleChoiceParam)
    */
  def sparkParamMap(sparkEntity: ml.param.Params, schema: StructType): ml.param.ParamMap = {

    val directParamMap = ml.param.ParamMap(
      sparkParamWrappers.flatMap(wrapper =>
        getOrDefaultOption(wrapper).map(value => {
          val convertedValue = wrapper.convertAny(value)(schema)
          ml.param.ParamPair(
            wrapper.sparkParam(sparkEntity).asInstanceOf[ml.param.Param[Any]], convertedValue)
        })
      ): _*)

    val paramsNestedInParamValues = params.flatMap(param => {
      get(param) match {
        case Some(nestedParams: ParamsWithSparkWrappers) =>
          Some(nestedParams.sparkParamMap(sparkEntity, schema))
        case _ => None
      }
    }).foldLeft(ml.param.ParamMap())((map1, map2) => map1 ++ map2)

    directParamMap ++ paramsNestedInParamValues
  }

}
