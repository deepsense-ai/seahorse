/**
 * Copyright 2016, deepsense.io
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

import org.apache.spark.ml.clustering.{DistributedLDAModel, LocalLDAModel, LDA => SparkLDA, LDAModel => SparkLDAModel}

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.SparkModelWrapper
import io.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import io.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import io.deepsense.deeplang.doperables.serialization.SerializableSparkModel
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasFeaturesColumnParam, HasSeedParam}

class LDAModel extends SparkModelWrapper[SparkLDAModel, SparkLDA]
  with HasFeaturesColumnParam
  with HasSeedParam {

  val params = declareParams(
    featuresColumn,
    seed)

  override def report: Report = {
    val vocabularySize =
      SparkSummaryEntry(
        name = "vocabulary size",
        value = sparkModel.vocabSize,
        description = "The number of terms in the vocabulary.")

    val estimatedDocConcentration =
      SparkSummaryEntry(
        name = "estimated doc concentration",
        value = sparkModel.estimatedDocConcentration,
        description = "Value for `doc concentration` estimated from data.")

    super.report
      .withAdditionalTable(CommonTablesGenerators.modelSummary(
        List(
          vocabularySize,
          estimatedDocConcentration)))
  }

  override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SparkLDAModel] = {
    try {
      new SerializableSparkModel(LocalLDAModel.load(path))
    } catch {
      case e: IllegalArgumentException =>
        logger.warn(s"LocalLDAModel.load($path) failed. Trying to load DistributedLDAModel.", e)
        new SerializableSparkModel(DistributedLDAModel.load(path))
    }
  }
}
