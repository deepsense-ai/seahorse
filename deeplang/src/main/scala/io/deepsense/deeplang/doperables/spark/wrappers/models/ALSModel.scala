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

package io.deepsense.deeplang.doperables.spark.wrappers.models

import org.apache.spark.ml.recommendation.{ALS => SparkALS, ALSModel => SparkALSModel}

import io.deepsense.deeplang.doperables.CommonTablesGenerators.SummaryEntry
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasItemColumnParam, HasPredictionColumnCreatorParam, HasUserColumnParam}
import io.deepsense.deeplang.doperables.{CommonTablesGenerators, Report, SparkModelWrapper}
import io.deepsense.deeplang.params.Param

class ALSModel
  extends SparkModelWrapper[SparkALSModel, SparkALS]
  with HasItemColumnParam
  with HasPredictionColumnCreatorParam
  with HasUserColumnParam {

  override val params: Array[Param[_]] = declareParams(
    itemColumn,
    predictionColumn,
    userColumn)

  override def report: Report = {
    val summary =
      List(
        SummaryEntry(
          name = "rank",
          value = model.rank.toString,
          description = "Rank of the matrix factorization model."))

    super.report
      .withAdditionalTable(CommonTablesGenerators.modelSummary(summary))
  }
}
