/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.factories

import org.apache.spark.mllib.feature.StandardScalerModel
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.RidgeRegressionModel

import io.deepsense.deeplang.doperables.TrainedRidgeRegression

trait TrainedRidgeRegressionTestFactory {

  val testTrainedRidgeRegression = TrainedRidgeRegression(
    Some(new RidgeRegressionModel(Vectors.dense(1.0, 2.3, 3.5, 99.8), 101.4)),
    Some(Seq("column1", "column2", "column3")),
    Some("result"),
    Some(new StandardScalerModel(Vectors.dense(1.2, 3.4), Vectors.dense(4.5, 6.7), true, true)))
}

object TrainedRidgeRegressionTestFactory extends TrainedRidgeRegressionTestFactory
