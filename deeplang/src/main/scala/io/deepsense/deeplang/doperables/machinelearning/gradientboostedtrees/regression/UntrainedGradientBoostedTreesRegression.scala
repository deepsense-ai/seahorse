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

package io.deepsense.deeplang.doperables.machinelearning.gradientboostedtrees.regression

import org.apache.spark.mllib.tree.configuration.{Algo, BoostingStrategy, Strategy}
import org.apache.spark.mllib.tree.impurity.{Impurity, Variance}
import org.apache.spark.mllib.tree.loss.{AbsoluteError, Loss, SquaredError}
import org.apache.spark.mllib.tree.{GradientBoostedTrees => SparkGradientBoostedTrees}

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.doperables.ColumnTypesPredicates.Predicate
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.machinelearning.gradientboostedtrees.GradientBoostedTreesParameters
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.{DKnowledge, DOperable, ExecutionContext}

case class UntrainedGradientBoostedTreesRegression(
    modelParameters: GradientBoostedTreesParameters)
  extends GradientBoostedTreesRegressor
  with Trainable
  with CategoricalFeaturesExtractor {

  def this() = this(null)


  override protected def runTraining: RunTraining = runTrainingWithLabeledPoints

  override protected def actualTraining: TrainScorable = (trainParameters) => {
    val treeStrategy = new Strategy(
      algo = Algo.Regression,
      impurity = createImpurityObject(modelParameters.impurity),
      maxDepth = modelParameters.maxDepth,
      numClasses = 2,
      maxBins = modelParameters.maxBins,
      categoricalFeaturesInfo =
        extractCategoricalFeatures(trainParameters.dataFrame, trainParameters.features))

    val boostingStrategy = BoostingStrategy(
      treeStrategy,
      loss = createLossObject(modelParameters.loss),
      numIterations = modelParameters.numIterations)

    val trainedModel =
      SparkGradientBoostedTrees.train(trainParameters.labeledPoints, boostingStrategy)

    TrainedGradientBoostedTreesRegression(
      modelParameters, trainedModel, trainParameters.features, trainParameters.target)
  }

  private def createLossObject(lossCode: String): Loss = {
    lossCode match {
      case "squared" => SquaredError
      case "absolute" => AbsoluteError
    }
  }

  private def createImpurityObject(impurityCode: String): Impurity = {
    impurityCode match {
      case "variance" => Variance
    }
  }

  override protected def actualInference(
      context: InferContext)(
      parameters: TrainableParameters)(
      dataFrame: DKnowledge[DataFrame]): (DKnowledge[Scorable], InferenceWarnings) =
    (DKnowledge(new TrainedGradientBoostedTreesRegression()), InferenceWarnings.empty)


  override def toInferrable: DOperable = new UntrainedGradientBoostedTreesRegression()

  override def report(executionContext: ExecutionContext): Report = {
    DOperableReporter("Untrained Gradient Boosted Trees Regression")
      .withParameters(modelParameters)
      .report
  }

  override def save(context: ExecutionContext)(path: String): Unit = ???

  override protected def labelPredicate: Predicate =
    ColumnTypesPredicates.isNumeric

  override protected def featurePredicate: Predicate =
    ColumnTypesPredicates.isNumericOrNonTrivialCategorical
}
