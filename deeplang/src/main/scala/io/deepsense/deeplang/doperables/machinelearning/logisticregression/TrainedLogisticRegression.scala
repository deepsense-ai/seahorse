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

package io.deepsense.deeplang.doperables.machinelearning.logisticregression

import scala.concurrent.Future

import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.doperables.ColumnTypesPredicates._
import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.{DOperable, ExecutionContext, Model}

case class TrainedLogisticRegression(
    modelParameters: LogisticRegressionParameters,
    model: LogisticRegressionModel,
    featureColumns: Seq[String],
    targetColumn: String)
  extends LogisticRegression
  with Scorable
  with VectorScoring {

  def this() = this(null, null, null, null)

  override def toInferrable: DOperable = new TrainedLogisticRegression()

  private var physicalPath: Option[String] = None

  override def featurePredicate: Predicate = ColumnTypesPredicates.isNumeric

  override def transformFeatures(v: RDD[Vector]): RDD[Vector] = v

  override def predict(features: RDD[Vector]): RDD[Double] =
    model
      .clearThreshold()
      .predict(features)

  override def url: Option[String] = physicalPath

  override def report(executionContext: ExecutionContext): Report = {
    DOperableReporter("Trained Logistic Regression")
      .withParameters(
        description = "",
        ("Regularization", ColumnType.numeric,
          DoubleUtils.double2String(modelParameters.regularization)),
        ("Iterations number", ColumnType.numeric,
          DoubleUtils.double2String(modelParameters.iterationsNumber)),
        ("Tolerance", ColumnType.numeric,
          DoubleUtils.double2String(modelParameters.tolerance))
      )
      .withWeights(featureColumns, model.weights.toArray)
      .withIntercept(model.intercept)
      .withVectorScoring(this)
      .report
  }

  override def save(context: ExecutionContext)(path: String): Unit = {
    val params = TrainedLogisticRegressionDescriptor(
      model.weights,
      model.intercept,
      featureColumns,
      targetColumn)
    context.fsClient.saveObjectToFile(path, params)
    this.physicalPath = Some(path)
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
