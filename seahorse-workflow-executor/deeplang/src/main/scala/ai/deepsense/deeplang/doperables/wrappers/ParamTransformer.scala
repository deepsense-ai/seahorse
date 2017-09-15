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

import org.apache.spark.ml.param.ParamMap

import ai.deepsense.deeplang.params.wrappers.deeplang.ParamWrapper
import ai.deepsense.deeplang.params.{Param, ParamPair}

private[wrappers] object ParamTransformer {

  /**
    * Transforms spark ParamMap to a seq of deeplang ParamPair.
    * Transformation is needed when operating on deeplang Estimators, Transformers, Evaluators
    * wrapped to work as Spark entities.
    */
  def transform(sparkParamMap: ParamMap): Seq[ParamPair[_]] = {
    sparkParamMap.toSeq.map{ case sparkParamPair =>
      val param: Param[Any] = sparkParamPair.param.asInstanceOf[ParamWrapper[Any]].param
      ParamPair(param, sparkParamPair.value)
    }
  }
}
