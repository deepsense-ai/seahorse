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

import org.apache.spark.ml.classification.{GBTClassificationModel => SparkGBTClassificationModel, GBTClassifier => SparkGBTClassifier}
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}

import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import ai.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.PredictorParams
import ai.deepsense.deeplang.doperables.stringindexingwrapper.StringIndexingWrapperModel
import ai.deepsense.deeplang.doperables.{LoadableWithFallback, SparkModelWrapper}
import ai.deepsense.deeplang.params.Param
import ai.deepsense.sparkutils.ML

class GBTClassificationModel(vanilaModel: VanillaGBTClassificationModel)
  extends StringIndexingWrapperModel[SparkGBTClassificationModel, SparkGBTClassifier](vanilaModel) {

  def this() = this(new VanillaGBTClassificationModel())
}

class VanillaGBTClassificationModel()
  extends SparkModelWrapper[SparkGBTClassificationModel, SparkGBTClassifier]
  with LoadableWithFallback[SparkGBTClassificationModel, SparkGBTClassifier]
  with PredictorParams
  with Logging {

  override protected def applyTransformSchema(schema: StructType): Option[StructType] = {
    val predictionColumnName = $(predictionColumn)
    Some(StructType(schema.fields :+ StructField(predictionColumnName, DoubleType)))
  }

  override val params: Array[Param[_]] =
    Array(featuresColumn, predictionColumn)

  override def report(extended: Boolean = true): Report = {
    val summary =
      List(
        SparkSummaryEntry(
          name = "number of features",
          value = sparkModel.numFeatures,
          description = "Number of features the model was trained on."))

    super.report(extended)
      .withReportName(
        s"${this.getClass.getSimpleName} with ${sparkModel.numTrees} trees")
      .withAdditionalTable(CommonTablesGenerators.modelSummary(summary))
      .withAdditionalTable(
        CommonTablesGenerators.decisionTree(
          sparkModel.treeWeights,
          sparkModel.trees),
        2)
  }

  override protected def transformerName: String = classOf[GBTClassificationModel].getSimpleName

  override def tryToLoadModel(path: String): Option[SparkGBTClassificationModel] = {
    ML.ModelLoading.GBTClassification(path)
  }
}
