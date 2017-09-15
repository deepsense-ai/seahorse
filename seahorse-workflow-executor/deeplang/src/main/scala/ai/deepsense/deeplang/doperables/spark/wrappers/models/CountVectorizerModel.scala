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

package ai.deepsense.deeplang.doperables.spark.wrappers.models

import org.apache.spark.ml.feature.{CountVectorizer => SparkCountVectorizer, CountVectorizerModel => SparkCountVectorizerModel}

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.SparkSingleColumnModelWrapper
import ai.deepsense.deeplang.doperables.serialization.SerializableSparkModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.HasMinTermsFrequencyParam
import ai.deepsense.deeplang.params.Param

class CountVectorizerModel
  extends SparkSingleColumnModelWrapper[SparkCountVectorizerModel, SparkCountVectorizer]
  with HasMinTermsFrequencyParam {

  override protected def getSpecificParams: Array[Param[_]] = Array(minTF)

  override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SparkCountVectorizerModel] = {
    new SerializableSparkModel(SparkCountVectorizerModel.load(path))
  }
}
