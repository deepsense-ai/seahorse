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
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.machinelearning.gradientboostedtrees.GradientBoostedTreesParameters
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.{DKnowledge, DMethod1To1, DOperable, ExecutionContext}
import io.deepsense.reportlib.model.{ReportContent, Table}

case class UntrainedGradientBoostedTreesRegression(
    modelParameters: GradientBoostedTreesParameters)
  extends GradientBoostedTreesRegressor
  with Trainable
  with CategoricalFeaturesExtractor {

  def this() = this(null)

  override def toInferrable: DOperable = new UntrainedGradientBoostedTreesRegression()

  override val train = new DMethod1To1[Trainable.Parameters, DataFrame, Scorable] {
    override def apply(context: ExecutionContext)
                      (parameters: Trainable.Parameters)
                      (dataFrame: DataFrame): Scorable = {

      val (featureColumns, targetColumn) = parameters.columnNames(dataFrame)

      val labeledPoints = dataFrame.selectAsSparkLabeledPointRDD(
        targetColumn,
        featureColumns,
        labelPredicate = ColumnTypesPredicates.isNumeric,
        featurePredicate = ColumnTypesPredicates.isNumericOrCategorical)

      labeledPoints.cache()

      val treeStrategy = new Strategy(
        algo = Algo.Regression,
        impurity = createImpurityObject(modelParameters.impurity),
        maxDepth = modelParameters.maxDepth,
        numClasses = 2,
        maxBins = modelParameters.maxBins,
        categoricalFeaturesInfo = extractCategoricalFeatures(dataFrame, featureColumns))

      val boostingStrategy = BoostingStrategy(
        treeStrategy,
        loss = createLossObject(modelParameters.loss),
        numIterations = modelParameters.numIterations)

      val trainedModel = SparkGradientBoostedTrees.train(labeledPoints, boostingStrategy)

      val result = TrainedGradientBoostedTreesRegression(
        modelParameters, trainedModel, featureColumns, targetColumn)

      labeledPoints.unpersist()
      result
    }

    override def infer(context: InferContext)
                      (parameters: Trainable.Parameters)
                      (dataframeKnowledge: DKnowledge[DataFrame])
      : (DKnowledge[Scorable], InferenceWarnings) = {

      (DKnowledge(new TrainedGradientBoostedTreesRegression()), InferenceWarnings.empty)
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
  }

  override def report(executionContext: ExecutionContext): Report = {

    val parametersList = List(
      "Num iterations",
      "Loss",
      "Impurity",
      "Max depth",
      "Max bins")

    val parametersTypes = List(
      ColumnType.numeric,
      ColumnType.numeric,
      ColumnType.numeric,
      ColumnType.numeric,
      ColumnType.numeric)

    val rows = List(
      List(
        Some(modelParameters.numIterations.toString),
        Some(modelParameters.loss),
        Some(modelParameters.impurity),
        Some(modelParameters.maxDepth.toString),
        Some(modelParameters.maxBins.toString)
      )
    )

    val parametersTable = Table(
      "Parameters",
      "",
      Some(parametersList),
      Some(parametersTypes),
      None,
      rows)

    Report(ReportContent("Report for UntrainedGradientBoostedTrees", List(parametersTable)))
  }

  override def save(context: ExecutionContext)(path: String): Unit = ???
}
