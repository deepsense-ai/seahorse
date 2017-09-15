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

package io.deepsense.deeplang.doperables

import scala.language.reflectiveCalls

import org.apache.spark.ml.feature.{StringIndexer => SparkStringIndexer, StringIndexerModel => SparkStringIndexerModel}

import io.deepsense.deeplang.doperables.CommonTablesGenerators.SummaryEntry
import io.deepsense.deeplang.params.Param

class StringIndexerEstimator
  extends SparkMultiColumnEstimatorWrapper[
    SparkStringIndexerModel,
    SparkStringIndexer,
    SingleStringIndexerModel,
    SingleStringIndexer,
    StringIndexerModel] {
  override def getSpecificParams: Array[Param[_]] = Array()
}

class SingleStringIndexer
  extends SparkSingleColumnEstimatorWrapper[
    SparkStringIndexerModel,
    SparkStringIndexer,
    SingleStringIndexerModel] {

  override def getSpecificParams: Array[Param[_]] = Array()
}

class SingleStringIndexerModel
  extends SparkSingleColumnModelWrapper[SparkStringIndexerModel, SparkStringIndexer] {

  override def getSpecificParams: Array[Param[_]] = Array()

  override def report: Report = {
    val summary =
      List(
        SummaryEntry(
          name = "labels",
          value = model.labels.mkString("[", ", ", "]"),
          description = "Ordered list of labels, corresponding to indices to be assigned."))

    super.report
      .withAdditionalTable(CommonTablesGenerators.modelSummary(summary))
  }
}
