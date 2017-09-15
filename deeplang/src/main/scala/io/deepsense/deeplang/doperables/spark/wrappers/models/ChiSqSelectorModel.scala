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

import org.apache.spark.ml.feature.{ChiSqSelector => SparkChiSqSelector, ChiSqSelectorModel => SparkChiSqSelectorModel}

import io.deepsense.deeplang.doperables.SparkModelWrapper
import io.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import io.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasFeaturesColumnParam, HasLabelColumnParam, HasOutputColumn}
import io.deepsense.deeplang.params.Param

class ChiSqSelectorModel
  extends SparkModelWrapper[SparkChiSqSelectorModel, SparkChiSqSelector]
  with HasFeaturesColumnParam
  with HasOutputColumn
  with HasLabelColumnParam {

  override val params: Array[Param[_]] = Array(
    featuresColumn,
    outputColumn,
    labelColumn
  )

  override def report: Report = {
    val summary =
      List(
        SparkSummaryEntry(
          name = "selected features",
          value = model.selectedFeatures,
          description = "List of indices to select."))

    super.report
      .withAdditionalTable(CommonTablesGenerators.modelSummary(summary))
  }
}
