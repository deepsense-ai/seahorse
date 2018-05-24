/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import org.apache.spark.ml.classification.{RandomForestClassificationModel => SparkRandomForestClassificationModel, RandomForestClassifier => SparkRandomForestClassifier}
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}

import ai.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import ai.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.ProbabilisticClassifierParams
import ai.deepsense.deeplang.doperables.stringindexingwrapper.StringIndexingWrapperModel
import ai.deepsense.deeplang.doperables.{LoadableWithFallback, SparkModelWrapper}
import ai.deepsense.deeplang.params.Param
import ai.deepsense.sparkutils.ML

class RandomForestClassificationModel(
    vanillaModel: VanillaRandomForestClassificationModel)
  extends StringIndexingWrapperModel[
    SparkRandomForestClassificationModel,
    SparkRandomForestClassifier](vanillaModel) {

  def this() = this(new VanillaRandomForestClassificationModel())
}

class VanillaRandomForestClassificationModel
  extends SparkModelWrapper[
    SparkRandomForestClassificationModel,
    SparkRandomForestClassifier]
  with LoadableWithFallback[
    SparkRandomForestClassificationModel,
    SparkRandomForestClassifier]
  with ProbabilisticClassifierParams {

  override protected def applyTransformSchema(schema: StructType): Option[StructType] = {
    val predictionColumnName = $(predictionColumn)
    val probabilityColumnName = $(probabilityColumn)
    val rawPredictionColumnName = $(rawPredictionColumn)
    Some(StructType(schema.fields ++ Seq(
      StructField(predictionColumnName, DoubleType),
      StructField(probabilityColumnName, new ai.deepsense.sparkutils.Linalg.VectorUDT),
      StructField(rawPredictionColumnName, new ai.deepsense.sparkutils.Linalg.VectorUDT)
    )))
  }

  override val params: Array[Param[_]] = Array(
    featuresColumn,
    predictionColumn,
    probabilityColumn,
    rawPredictionColumn) // thresholds

  override def report(extended: Boolean = true): Report = {
    val treeWeight = SparkSummaryEntry(
      name = "tree weights",
      value = sparkModel.treeWeights,
      description = "Weights for each tree."
    )

    super.report(extended)
      .withAdditionalTable(CommonTablesGenerators.modelSummary(List(treeWeight)))
  }

  override protected def transformerName: String =
    classOf[RandomForestClassificationModel].getSimpleName

  override def tryToLoadModel(path: String): Option[SparkRandomForestClassificationModel] = {
    ML.ModelLoading.randomForestClassification(path)
  }
}
