/**
 * Copyright 2015, CodiLime Inc.
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

import scala.concurrent.Future

import org.apache.spark.mllib.feature.StandardScalerModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.{GeneralizedLinearModel, RidgeRegressionModel}
import org.apache.spark.rdd.RDD

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DOperable, DMethod1To1, ExecutionContext, Model}
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

  def toInferrable: DOperable = new TrainedRidgeRegression()

  private var physicalPath: Option[String] = None

  override def url: Option[String] = physicalPath

  def preparedModel: GeneralizedLinearModel = model.get

  def transformFeatures(v: RDD[Vector]): RDD[Vector] = scaler.get.transform(v)

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
  override def deploy(f: Model => Future[String]): Future[String] = {
    val model = new Model(false, modelIntercept, modelWeights.toArray.toSeq,
      scalerMean.toArray.toSeq, scaleStd.toArray.toSeq)
      f(model)
  }
}
