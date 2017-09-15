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

import org.apache.spark.ml
import org.apache.spark.ml.feature.{Bucketizer => SparkQuantileDiscretizerModel, QuantileDiscretizer => SparkQuantileDiscretizer}

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.SparkSingleColumnModelWrapper
import io.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import io.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.wrappers.spark.DoubleArrayParamWrapper

class QuantileDiscretizerModel
  extends SparkSingleColumnModelWrapper[SparkQuantileDiscretizerModel, SparkQuantileDiscretizer] {

  val splits = new DoubleArrayParamWrapper[
      ml.param.Params { val splits: ml.param.DoubleArrayParam }](
    name = "splits",
    description = "Split points for mapping continuous features into buckets.",
    sparkParamGetter = _.splits)
  setDefault(splits, Array(Double.NegativeInfinity, 0, Double.PositiveInfinity))

  override protected def getSpecificParams: Array[Param[_]] = Array(splits)

  override def report: Report = {
    val summary =
      List(
        SparkSummaryEntry(
          name = "splits",
          value = model.splits,
          description = "Split points for mapping continuous features into buckets."))

    super.report
      .withAdditionalTable(CommonTablesGenerators.modelSummary(summary))
  }

  override protected def loadModel(
    ctx: ExecutionContext,
    path: String): SparkQuantileDiscretizerModel = {
    SparkQuantileDiscretizerModel.load(path)
  }

}
