/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.feature.{StandardScaler, StandardScalerModel}
import org.apache.spark.mllib.regression.{LabeledPoint, RidgeRegressionWithSGD}

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

      val (featureColumns, targetColumn) = parameters.columnNames(dataframe)

      val labeledPoints = dataframe.toSparkLabeledPointRDD(featureColumns, targetColumn)
      labeledPoints.cache()

      val scaler: StandardScalerModel = new StandardScaler(withStd = true, withMean = true)
        .fit(labeledPoints.map(_.features))
      val scaledLabeledPoints = labeledPoints.map(lp => {
        LabeledPoint(lp.label, scaler.transform(lp.features))
      })
      labeledPoints.unpersist()
      scaledLabeledPoints.cache()

      val trainedModel = model.get.run(scaledLabeledPoints)
      val result = TrainedRidgeRegression(
        Some(trainedModel), Some(featureColumns), Some(targetColumn), Some(scaler))
      scaledLabeledPoints.unpersist()
      saveScorable(context, result)
      result
    }

    override def infer(
        context: InferContext)(
        parameters: Trainable.Parameters)(
        dataframeKnowledge: DKnowledge[DataFrame]): DKnowledge[Scorable] = {

      DKnowledge(new TrainedRidgeRegression)
    }
  }

  override def report: Report = Report(ReportContent("Report for UntrainedRidgeRegression"))

  override def save(context: ExecutionContext)(path: String): Unit =
    throw new UnsupportedOperationException
}
