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
import org.apache.spark.ml.classification.{GBTClassificationModel => SparkGBTClassificationModel}
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.Transformer
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import io.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.PredictorParams
import io.deepsense.deeplang.params.{Param, ParamMap}
import io.deepsense.deeplang.utils.SparkCustomTransformerWrapper

class GBTClassificationModel(transformer: SparkCustomTransformerWrapper)
  extends Transformer
  with PredictorParams with Logging {

  def this() = this(null)

  override private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    transformer._transform(ctx, df)
  }

  override private[deeplang] def _transformSchema(schema: StructType): Option[StructType] = {
    val predictionColumnName = $(predictionColumn)
    Some(StructType(schema.fields :+ StructField(predictionColumnName, DoubleType)))
  }

  override def replicate(extra: ParamMap): GBTClassificationModel.this.type = {
    val transformerCopy = Option(transformer).map(_.replicate(extra)).orNull
    val that = new GBTClassificationModel(transformerCopy).asInstanceOf[this.type]
    copyValues(that, extra)
  }

  override val params: Array[Param[_]] =
    declareParams(featuresColumn, predictionColumn)

  override def report: Report = {
    val model = transformer.transformer.asInstanceOf[PipelineModel].stages
      .collect{ case model: SparkGBTClassificationModel => model}.head
    val summary =
      List(
        SparkSummaryEntry(
          name = "number of features",
          value = model.numFeatures,
          description = "Number of features the model was trained on."))

    super.report
      .withReportName(s"${this.getClass.getSimpleName} with ${model.numTrees} trees")
      .withAdditionalTable(CommonTablesGenerators.modelSummary(summary))
      .withAdditionalTable(CommonTablesGenerators.decisionTree(model.treeWeights, model.trees), 2)
  }
}
