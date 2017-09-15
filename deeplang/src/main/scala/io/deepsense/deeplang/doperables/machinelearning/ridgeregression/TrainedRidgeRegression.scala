/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang.doperables.machinelearning.ridgeregression

import scala.concurrent.Future

import org.apache.spark.mllib.feature.StandardScalerModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.{GeneralizedLinearModel, RidgeRegressionModel}
import org.apache.spark.rdd.RDD

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.doperables.ColumnTypesPredicates.Predicate
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.RidgeRegressionParameters
import io.deepsense.deeplang.{DOperable, ExecutionContext, Model}

case class TrainedRidgeRegression(
    modelParameters: RidgeRegressionParameters,
    model: RidgeRegressionModel,
    featureColumns: Seq[String],
    targetColumn: String,
    scaler: StandardScalerModel)
  extends RidgeRegression
  with Scorable
  with VectorScoring
  with DOperableSaver {

  def this() = this(null, null, null, null, null)

  def toInferrable: DOperable = new TrainedRidgeRegression()

  private var physicalPath: Option[String] = None

  override def url: Option[String] = physicalPath

  def preparedModel: GeneralizedLinearModel = model

  override protected def featurePredicate: Predicate = ColumnTypesPredicates.isNumeric

  override def transformFeatures(v: RDD[Vector]): RDD[Vector] = scaler.transform(v)

  override def predict(features: RDD[Vector]): RDD[Double] = preparedModel.predict(features)

  override def report(executionContext: ExecutionContext): Report = {
    DOperableReporter("Report for TrainedRidgeRegression")
      .withParameters(
        description = "",
        ("Regularization parameter",
          ColumnType.numeric, modelParameters.regularizationParameter.toString),
        ("Iterations number", ColumnType.numeric, modelParameters.numberOfIterations.toString),
        ("Mini batch fraction", ColumnType.numeric, modelParameters.miniBatchFraction.toString)
      )
      .withWeights(featureColumns, model.weights.toArray)
      .withIntercept(model.intercept)
      .withVectorScoring(this)
      .report
  }

  override def save(context: ExecutionContext)(path: String): Unit = {
    val params = TrainedRidgeRegressionDescriptor(
      model.weights,
      model.intercept,
      featureColumns,
      targetColumn,
      scaler.std,
      scaler.mean)
    context.fsClient.saveObjectToFile(path, params)
    this.physicalPath = Some(path)
  }
}

object TrainedRidgeRegression {
  def loadFromHdfs(modelParameters: RidgeRegressionParameters,
       context: ExecutionContext)(path: String): TrainedRidgeRegression = {
    val params: TrainedRidgeRegressionDescriptor =
      context.fsClient.readFileAsObject[TrainedRidgeRegressionDescriptor](path)
    TrainedRidgeRegression(
      modelParameters,
      new RidgeRegressionModel(params.modelWeights, params.modelIntercept),
      params.featureColumns,
      params.targetColumn,
      new StandardScalerModel(params.scaleStd, params.scalerMean, true, true)
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
