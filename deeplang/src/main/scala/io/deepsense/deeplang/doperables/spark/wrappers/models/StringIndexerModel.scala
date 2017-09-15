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

import org.apache.spark.ml.feature.{StringIndexer => SparkStringIndexer, StringIndexerModel => SparkStringIndexerModel}

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import io.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import io.deepsense.deeplang.doperables.serialization.SerializableSparkModel
import io.deepsense.deeplang.doperables.{MultiColumnModel, SparkSingleColumnModelWrapper, Transformer}
import io.deepsense.deeplang.params.Param

trait StringIndexerModel extends Transformer

case class MultiColumnStringIndexerModel()
  extends MultiColumnModel[
    SparkStringIndexerModel,
    SparkStringIndexer,
    SingleColumnStringIndexerModel]
  with StringIndexerModel {

  override def getSpecificParams: Array[Param[_]] = Array()

  override def report: Report = {
    val tables = models.map(model => model.report.content.tables)
    val name = s"${this.getClass.getSimpleName} with ${models.length} columns"
    tables
      .foldRight (super.report.withReportName(name))(
        (seqTables, accReport) =>
          seqTables.foldRight(accReport)((t, r) => r.withAdditionalTable(t)))
  }

  override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SparkStringIndexerModel] = {
    new SerializableSparkModel(SparkStringIndexerModel.load(path))
  }
}

class SingleColumnStringIndexerModel
  extends SparkSingleColumnModelWrapper[SparkStringIndexerModel, SparkStringIndexer]
  with StringIndexerModel {

  override def getSpecificParams: Array[Param[_]] = Array()

  override def report: Report = {
    val summary =
      List(
        SparkSummaryEntry(
          name = "labels",
          value = sparkModel.labels,
          description = "Ordered list of labels, corresponding to indices to be assigned."))

    super.report
      .withAdditionalTable(CommonTablesGenerators.modelSummary(summary))
  }

  override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SparkStringIndexerModel] = {
    new SerializableSparkModel(SparkStringIndexerModel.load(path))
  }
}

