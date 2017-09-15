/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.regression.RidgeRegressionWithSGD

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DKnowledge, DMethod1To1, ExecutionContext, InferContext}
import io.deepsense.reportlib.model.ReportContent

case class UntrainedRidgeRegression(
    model: Option[RidgeRegressionWithSGD])
  extends RidgeRegression with Trainable {

  def this() = this(None)

  override val train = new DMethod1To1[Trainable.Parameters, DataFrame, Scorable] {
    override def apply(
        context: ExecutionContext)(
        parameters: Trainable.Parameters)(
        dataframe: DataFrame): Scorable = {

      val featureColumns = dataframe.getColumnNames(parameters.featureColumns)
      val labelColumn = dataframe.getColumnName(parameters.targetColumn)

      val labeledPoints = dataframe.toSparkLabeledPointRDD(featureColumns, labelColumn)
      val trainedModel = model.get.run(labeledPoints)
      TrainedRidgeRegression(Some(trainedModel), Some(featureColumns), Some(labelColumn))
    }

    override def infer(
        context: InferContext)(
        parameters: Trainable.Parameters)(
        dataframeKnowledge: DKnowledge[DataFrame]): DKnowledge[Scorable] = {

      DKnowledge(new TrainedRidgeRegression)
    }
  }

  override def report: Report = Report(ReportContent("Report for UntrainedRidgeRegression"))
}
