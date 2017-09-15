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

import org.apache.spark.ml.clustering.{LDA => SparkLDA, LDAModel => SparkLDAModel}

import io.deepsense.deeplang.doperables.SparkModelWrapper
import io.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import io.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasFeaturesColumn, HasSeedParam}

class LDAModel extends SparkModelWrapper[SparkLDAModel, SparkLDA]
  with HasFeaturesColumn
  with HasSeedParam {

  val params = declareParams(
    featuresColumn,
    seed)

  override def report: Report = {
    val vocabularySize =
      SparkSummaryEntry(
        name = "vocabulary size",
        value = model.vocabSize,
        description = "The number of terms in the vocabulary.")

    val estimatedDocConcentration =
      SparkSummaryEntry(
        name = "estimated doc concentration",
        value = model.estimatedDocConcentration,
        description = "Value for `doc concentration` estimated from data.")

    super.report
      .withAdditionalTable(CommonTablesGenerators.modelSummary(
        List(
          vocabularySize,
          estimatedDocConcentration)))
  }
}
