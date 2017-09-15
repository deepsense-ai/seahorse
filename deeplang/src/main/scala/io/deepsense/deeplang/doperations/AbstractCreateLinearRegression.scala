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

package io.deepsense.deeplang.doperations

import org.apache.spark.mllib.optimization.GradientDescent
import org.apache.spark.mllib.regression.{GeneralizedLinearAlgorithm, GeneralizedLinearModel}

import io.deepsense.deeplang.{DOperable, DOperation0To1}

abstract class AbstractCreateLinearRegression
   [UntrainedRegressionType <: DOperable,
    ModelType <: GeneralizedLinearAlgorithm[T],
    T <: GeneralizedLinearModel]
    extends DOperation0To1[UntrainedRegressionType]
    with LinearRegressionParams {

  protected def createModel(modelConstructor: () => ModelType): ModelType = {

    val regParam = regularizationParameter.value
    val numberOfIterations = iterationsNumberParameter.value
    val miniBatchFraction = miniBatchFractionParameter.value

    val model = modelConstructor()

    model.asInstanceOf[GeneralizedLinearAlgorithm[T]]
      .setIntercept(true)
      .setValidateData(false)
      .optimizer.asInstanceOf[GradientDescent]
      .setStepSize(1.0)
      .setRegParam(regParam)
      .setNumIterations(numberOfIterations.toInt)
      .setMiniBatchFraction(miniBatchFraction)

    model.asInstanceOf[ModelType]
  }

}
