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

import org.apache.spark.mllib.feature.StandardScalerModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.RidgeRegressionModel
import org.apache.spark.rdd.RDD

import io.deepsense.deeplang.doperables.ColumnTypesPredicates.Predicate
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.machinelearning.{TrainedLinearRegression, LinearRegressionParameters}
import io.deepsense.deeplang.{DOperable, ExecutionContext}

case class TrainedRidgeRegression(
    modelParameters: LinearRegressionParameters,
    model: RidgeRegressionModel,
    featureColumns: Seq[String],
    targetColumn: String,
    scaler: StandardScalerModel)
  extends RidgeRegression
  with TrainedLinearRegression
  with Scorable
  with HasTargetColumn {

  def this() = this(null, null, null, null, null)

  def toInferrable: DOperable = new TrainedRidgeRegression()

  override protected def featurePredicate: Predicate = ColumnTypesPredicates.isNumeric

  override def transformFeatures(v: RDD[Vector]): RDD[Vector] = scaler.transform(v)

  override def predict(features: RDD[Vector]): RDD[Double] = model.predict(features)

  override def report(executionContext: ExecutionContext): Report = {
    generateTrainedRegressionReport(
      modelParameters, model, featureColumns, targetColumn)
  }

  override def save(executionContext: ExecutionContext)(path: String): Unit = ???
}
