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

import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.classification.{RandomForestClassificationModel => SparkRandomForestClassificationModel, RandomForestClassifier => SparkRandomForestClassifier}
import org.apache.spark.mllib.linalg.VectorUDT
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}

import io.deepsense.deeplang.doperables.SparkModelWrapper
import io.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import io.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasLabelColumnParam, ProbabilisticClassifierParams}
import io.deepsense.deeplang.doperables.stringindexingwrapper.StringIndexingWrapperModel
import io.deepsense.deeplang.params.Param

class RandomForestClassificationModel(
    pipelineModel: PipelineModel,
    vanillaModel: VanillaRandomForestClassificationModel)
  extends StringIndexingWrapperModel(pipelineModel, vanillaModel) {

  def this() = this(null, new VanillaRandomForestClassificationModel())
}

class VanillaRandomForestClassificationModel
  extends SparkModelWrapper[
    SparkRandomForestClassificationModel,
    SparkRandomForestClassifier]
    with ProbabilisticClassifierParams {

  override private[deeplang] def _transformSchema(schema: StructType): Option[StructType] = {
    val predictionColumnName = $(predictionColumn)
    val probabilityColumnName = $(probabilityColumn)
    val rawPredictionColumnName = $(rawPredictionColumn)
    Some(StructType(schema.fields ++ Seq(
      StructField(predictionColumnName, DoubleType),
      StructField(probabilityColumnName, new VectorUDT),
      StructField(rawPredictionColumnName, new VectorUDT)
    )))
  }

  override val params: Array[Param[_]] = declareParams(
    featuresColumn,
    predictionColumn,
    probabilityColumn,
    rawPredictionColumn) // thresholds

  override def report: Report = {
    val treeWeight = SparkSummaryEntry(
      name = "tree weights",
      value = model.treeWeights,
      description = "Weights for each tree."
    )

    super.report
      .withAdditionalTable(CommonTablesGenerators.modelSummary(List(treeWeight)))
  }
}
