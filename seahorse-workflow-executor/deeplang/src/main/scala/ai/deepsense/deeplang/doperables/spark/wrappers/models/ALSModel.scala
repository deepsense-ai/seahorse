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

import org.apache.spark.ml.recommendation.{ALS => SparkALS, ALSModel => SparkALSModel}

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.SparkModelWrapper
import ai.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import ai.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import ai.deepsense.deeplang.doperables.serialization.SerializableSparkModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasItemColumnParam, HasPredictionColumnCreatorParam, HasUserColumnParam}
import ai.deepsense.deeplang.params.Param

class ALSModel
  extends SparkModelWrapper[SparkALSModel, SparkALS]
  with HasItemColumnParam
  with HasPredictionColumnCreatorParam
  with HasUserColumnParam {

  override val params: Array[Param[_]] = Array(
    itemColumn,
    predictionColumn,
    userColumn)

  override def report(extended: Boolean = true): Report = {
    val summary =
      List(
        SparkSummaryEntry(
          name = "rank",
          value = sparkModel.rank,
          description = "Rank of the matrix factorization model."))

    super.report(extended)
      .withAdditionalTable(CommonTablesGenerators.modelSummary(summary))
  }

  override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SparkALSModel] = {
    new SerializableSparkModel(SparkALSModel.load(path))
  }
}
