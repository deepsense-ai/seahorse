/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.classification.{LogisticRegressionModel, LogisticRegressionWithLBFGS}

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DKnowledge, DMethod1To1, ExecutionContext, InferContext}
import io.deepsense.reportlib.model.ReportContent

case class UntrainedLogisticRegression(
    model: Option[LogisticRegressionWithLBFGS])
  extends LogisticRegression
  with Trainable {

  def this() = this(None)

  override val train = new DMethod1To1[Trainable.Parameters, DataFrame, Scorable] {
    override def apply(
        context: ExecutionContext)(
        parameters: Trainable.Parameters)(
        dataFrame: DataFrame): Scorable = {

      val featureColumns = dataFrame.getColumnNames(parameters.featureColumns)
      val labelColumn = dataFrame.getColumnName(parameters.targetColumn)
      val labeledPoints = dataFrame.toSparkLabeledPointRDD(featureColumns, labelColumn)
      labeledPoints.cache()
      val trainedModel: LogisticRegressionModel = model.get.run(labeledPoints)
      val result = TrainedLogisticRegression(
        Some(trainedModel),
        Some(featureColumns),
        Some(labelColumn))
      saveScorable(context, result)
      result
    }

    override def infer(
      context: InferContext)(
      parameters: Trainable.Parameters)(
      dataframeKnowledge: DKnowledge[DataFrame]): DKnowledge[Scorable] = {

      DKnowledge(new TrainedLogisticRegression())
    }
  }

  override def report: Report = Report(ReportContent("Report for UntrainedLogisticRegression"))

  override def save(context: ExecutionContext)(path: String): Unit =
    throw new UnsupportedOperationException

}
