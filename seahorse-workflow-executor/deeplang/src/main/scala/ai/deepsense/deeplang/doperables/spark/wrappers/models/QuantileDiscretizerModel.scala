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

import scala.language.reflectiveCalls

import org.apache.spark.ml.feature.{Bucketizer => SparkQuantileDiscretizerModel, QuantileDiscretizer => SparkQuantileDiscretizer}

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.SparkSingleColumnModelWrapper
import ai.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import ai.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import ai.deepsense.deeplang.doperables.serialization.SerializableSparkModel
import ai.deepsense.deeplang.params.Param

class QuantileDiscretizerModel
  extends SparkSingleColumnModelWrapper[SparkQuantileDiscretizerModel, SparkQuantileDiscretizer] {

  /*
   * Parameter `splits` is not wrapped because it violates a design assumption, that every parameter
   * of a model is also a parameter of a corresponding estimator.
   */
  override protected def getSpecificParams: Array[Param[_]] = Array()

  override def report(extended: Boolean = true): Report = {
    val summary =
      List(
        SparkSummaryEntry(
          name = "splits",
          value = sparkModel.getSplits,
          description = "Split points for mapping continuous features into buckets."))

    super.report(extended)
      .withAdditionalTable(CommonTablesGenerators.modelSummary(summary))
  }

  override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SparkQuantileDiscretizerModel] = {
    new SerializableSparkModel(SparkQuantileDiscretizerModel.load(path))
  }

}
