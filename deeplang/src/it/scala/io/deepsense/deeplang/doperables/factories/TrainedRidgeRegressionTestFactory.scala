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

package io.deepsense.deeplang.doperables.factories

import org.apache.spark.mllib.feature.StandardScalerModel
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.RidgeRegressionModel

import io.deepsense.deeplang.doperables.machinelearning.ridgeregression.TrainedRidgeRegression
import io.deepsense.deeplang.doperations.RidgeRegressionParameters

trait TrainedRidgeRegressionTestFactory {

  val testTrainedRidgeRegression = TrainedRidgeRegression(
    RidgeRegressionParameters(0.1, 11, 0.3),
    new RidgeRegressionModel(Vectors.dense(1.0, 2.3, 3.5, 99.8), 101.4),
    Seq("column1", "column2", "column3"),
    "result",
    new StandardScalerModel(Vectors.dense(1.2, 3.4), Vectors.dense(4.5, 6.7), true, true))
}

object TrainedRidgeRegressionTestFactory extends TrainedRidgeRegressionTestFactory
