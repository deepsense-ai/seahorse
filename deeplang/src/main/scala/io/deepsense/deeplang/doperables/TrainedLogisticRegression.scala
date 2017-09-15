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

import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.GeneralizedLinearModel
import org.apache.spark.rdd.RDD

import io.deepsense.deeplang.{DOperable, ExecutionContext, Model}
import io.deepsense.reportlib.model.{ReportContent, Table}

case class TrainedLogisticRegression(
    model: Option[LogisticRegressionModel],
    featureColumns: Option[Seq[String]],
    targetColumn: Option[String])
  extends LogisticRegression
  with Scorable
  with RegressionScoring {

  def this() = this(None, None, None)

  override def toInferrable: DOperable = new TrainedLogisticRegression()

  private var physicalPath: Option[String] = None

  def preparedModel: GeneralizedLinearModel = model.get.clearThreshold()

  override def transformFeatures(v: RDD[Vector]): RDD[Vector] = v

  override def predict(vectors: RDD[Vector]): RDD[Double] = preparedModel.predict(vectors)

  override def url: Option[String] = physicalPath

  override def report(executionContext: ExecutionContext): Report = {
    val featureColumnsColumn = featureColumns.get.toList.map(Some.apply)
    val targetColumnColumn = List(targetColumn)
    val rows = featureColumnsColumn.zipAll(targetColumnColumn, Some(""), Some(""))
      .map{ case (a, b) => List(a, b) }

    val table = Table(
      "Trained Logistic Regression", "", Some(List("Feature columns", "Target column")), None, rows)

    Report(ReportContent("Report for TrainedLogisticRegression", List(table)))
  }

  override def save(context: ExecutionContext)(path: String): Unit = {
    val params = TrainedLogisticRegressionDescriptor(
      model.get.weights,
      model.get.intercept,
      featureColumns.get,
      targetColumn.get)
    context.fsClient.saveObjectToFile(path, params)
    this.physicalPath = Some(path)
  }
}

object TrainedLogisticRegression {
  def loadFromFs(context: ExecutionContext)(path: String): TrainedLogisticRegression = {
    val params: TrainedLogisticRegressionDescriptor =
      context.fsClient.readFileAsObject[TrainedLogisticRegressionDescriptor](path)
    TrainedLogisticRegression(
      Some(new LogisticRegressionModel(params.modelWeights, params.modelIntercept)),
      Some(params.featureColumns),
      Some(params.targetColumn))
  }
}

case class TrainedLogisticRegressionDescriptor(
  modelWeights: Vector,
  modelIntercept: Double,
  featureColumns: Seq[String],
  targetColumn: String) extends Deployable {

  override def deploy(f: Model => Future[String]): Future[String] =
    throw new UnsupportedOperationException()
}
