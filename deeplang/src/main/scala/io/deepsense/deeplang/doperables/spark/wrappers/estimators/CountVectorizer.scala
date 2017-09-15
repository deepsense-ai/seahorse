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

package io.deepsense.deeplang.doperables.spark.wrappers.estimators

import org.apache.spark.ml
import org.apache.spark.ml.feature.{CountVectorizer => SparkCountVectorizer, CountVectorizerModel => SparkCountVectorizerModel}

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.spark.wrappers.models.CountVectorizerModel
import io.deepsense.deeplang.doperables.spark.wrappers.params.common._
import io.deepsense.deeplang.doperables.{Report, SparkEstimatorWrapper}
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.validators.RangeValidator
import io.deepsense.deeplang.params.wrappers.spark.{IntParamWrapper, DoubleParamWrapper}

class CountVectorizer
  extends SparkEstimatorWrapper[
    SparkCountVectorizerModel,
    SparkCountVectorizer,
    CountVectorizerModel]
  with HasMinTermsFrequencyParam {

  val minDF = new DoubleParamWrapper[ml.param.Params { val minDF: ml.param.DoubleParam }](
    name = "min different documents",
    description = "Specifies the minimum number of different documents " +
      "a term must appear in to be included in the vocabulary",
    sparkParamGetter = _.minDF,
    RangeValidator(0.0, Double.MaxValue))
  setDefault(minDF, 1.0)

  val vocabSize = new IntParamWrapper[ml.param.Params { val vocabSize: ml.param.IntParam }](
    name = "max vocabulary size",
    description = "Max size of the vocabulary.",
    sparkParamGetter = _.vocabSize,
    RangeValidator(0.0, Double.MaxValue, beginIncluded = false))
  setDefault(vocabSize, (1 << 18).toDouble)

  override def report(executionContext: ExecutionContext): Report = Report()

  override val params: Array[Param[_]] =
    declareParams(inputColumn, minDF, minTF, outputColumn, vocabSize)
}
