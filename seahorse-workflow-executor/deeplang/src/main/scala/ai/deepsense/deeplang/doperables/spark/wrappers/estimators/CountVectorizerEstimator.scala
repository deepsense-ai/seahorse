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

package ai.deepsense.deeplang.doperables.spark.wrappers.estimators

import scala.language.reflectiveCalls

import org.apache.spark.ml
import org.apache.spark.ml.feature.{CountVectorizer => SparkCountVectorizer, CountVectorizerModel => SparkCountVectorizerModel}

import ai.deepsense.deeplang.doperables.SparkSingleColumnEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.models.CountVectorizerModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common._
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.wrappers.spark.{DoubleParamWrapper, IntParamWrapper}

class CountVectorizerEstimator
  extends SparkSingleColumnEstimatorWrapper[
    SparkCountVectorizerModel,
    SparkCountVectorizer,
    CountVectorizerModel]
  with HasMinTermsFrequencyParam {

  val minDF = new DoubleParamWrapper[ml.param.Params { val minDF: ml.param.DoubleParam }](
    name = "min different documents",
    description = Some("Specifies the minimum number of different documents " +
      "a term must appear in to be included in the vocabulary."),
    sparkParamGetter = _.minDF,
    RangeValidator(0.0, Double.MaxValue))
  setDefault(minDF, 1.0)

  val vocabSize = new IntParamWrapper[ml.param.Params { val vocabSize: ml.param.IntParam }](
    name = "max vocabulary size",
    description = Some("The maximum size of the vocabulary."),
    sparkParamGetter = _.vocabSize,
    RangeValidator(0.0, Int.MaxValue, beginIncluded = false, step = Some(1.0)))
  setDefault(vocabSize, (1 << 18).toDouble)

  override protected def getSpecificParams: Array[Param[_]] = Array(vocabSize, minDF, minTF)
}
