/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.doperables

import scala.concurrent.Future

import org.apache.spark.mllib.feature.StandardScalerModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.RidgeRegressionModel

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DMethod1To1, ExecutionContext}
import io.deepsense.deploymodelservice.{CreateResult, Model}
import io.deepsense.reportlib.model.ReportContent

case class TrainedRidgeRegression(
    model: Option[RidgeRegressionModel],
    featureColumns: Option[Seq[String]],
    targetColumn: Option[String],
    scaler: Option[StandardScalerModel])
  extends RidgeRegression
  with Scorable
  with RegressionScoring
  with DOperableSaver {

  def this() = this(None, None, None, None)

  private var physicalPath: Option[String] = None

  override def url: Option[String] = physicalPath

  override val score = new DMethod1To1[Unit, DataFrame, DataFrame] {

    override def apply(context: ExecutionContext)(p: Unit)(dataframe: DataFrame): DataFrame =
      scoreRegression(context)(
        dataframe,
        featureColumns.get,
        targetColumn.get,
        TrainedRidgeRegression.labelColumnSuffix,
        scaler.get.transform,
        model.get)
  }

  override def report: Report = Report(ReportContent("Report for TrainedRidgeRegression.\n" +
    s"Feature columns: ${featureColumns.get.mkString(", ")}\n" +
    s"Target column: ${targetColumn.get}\n" +
    s"Model: $model"))

  override def save(context: ExecutionContext)(path: String): Unit = {
    val params = TrainedRidgeRegressionDescriptor(
      model.get.weights,
      model.get.intercept,
      featureColumns.get,
      targetColumn.get,
      scaler.get.std,
      scaler.get.mean)
    context.hdfsClient.saveObjectToFile(path, params)
    this.physicalPath = Some(path)
  }
}

object TrainedRidgeRegression {
  val labelColumnSuffix = "prediction"

  def loadFromHdfs(context: ExecutionContext)(path: String): TrainedRidgeRegression = {
    val params: TrainedRidgeRegressionDescriptor =
      context.hdfsClient.readFileAsObject[TrainedRidgeRegressionDescriptor](path)
    TrainedRidgeRegression(
      Some(new RidgeRegressionModel(params.modelWeights, params.modelIntercept)),
      Some(params.featureColumns),
      Some(params.targetColumn),
      Some(new StandardScalerModel(params.scaleStd, params.scalerMean, true, true))
    )
  }
}

case class TrainedRidgeRegressionDescriptor(
  modelWeights: Vector,
  modelIntercept: Double,
  featureColumns: Seq[String],
  targetColumn: String,
  scaleStd: Vector,
  scalerMean: Vector) extends Deployable {
  override def deploy(f: Model => Future[CreateResult]): Future[CreateResult] = {
    val model = new Model(false, modelIntercept, modelWeights.toArray.toSeq,
      scalerMean.toArray.toSeq, scaleStd.toArray.toSeq)
      f(model)
  }
}
