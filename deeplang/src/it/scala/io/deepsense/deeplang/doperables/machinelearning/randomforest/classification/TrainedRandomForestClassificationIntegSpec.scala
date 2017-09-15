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

package io.deepsense.deeplang.doperables.machinelearning.randomforest.classification

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.configuration.Algo._
import org.apache.spark.mllib.tree.configuration.{Algo, Strategy}
import org.apache.spark.mllib.tree.model.{DecisionTreeModel, RandomForestModel}

import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType
import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType.ExtendedColumnType
import io.deepsense.deeplang.doperables.machinelearning.randomforest.RandomForestParameters
import io.deepsense.deeplang.doperables.{Scorable, ScorableBaseIntegSpec, SupervisedPredictorModelBaseIntegSpec}

class TrainedRandomForestClassificationIntegSpec
  extends ScorableBaseIntegSpec("TrainedRandomForestClassification")
  with SupervisedPredictorModelBaseIntegSpec {

  override def acceptedFeatureTypes: Seq[ExtendedColumnType] = Seq(
    ExtendedColumnType.binaryValuedNumeric,
    ExtendedColumnType.nonBinaryValuedNumeric,
    ExtendedColumnType.categorical2,
    ExtendedColumnType.categoricalMany)

  override def unacceptableFeatureTypes: Seq[ExtendedColumnType] = Seq(
    ExtendedColumnType.categorical1,
    ExtendedColumnType.boolean,
    ExtendedColumnType.string,
    ExtendedColumnType.timestamp)

  override def mockTrainedModel(): PredictorSparkModel = {
    class RandomForestClassificationPredictor
      extends RandomForestModel(mock[Algo], mock[Array[DecisionTreeModel]])
      with PredictorSparkModel {}

    mock[RandomForestClassificationPredictor]
  }

  override def createScorableInstance(features: String*): Scorable = {
    // This is a shameless shortcut: creating a dummy model from scratch
    // is not as simple as it looks. Training it is actually easier.
    val labeledPoints = sparkContext.parallelize(Seq(
      LabeledPoint(0.0, Vectors.dense(1)),
      LabeledPoint(1.0, Vectors.dense(2))))

    val model = RandomForest.trainClassifier(
      labeledPoints, Strategy.defaultStrategy(Algo.Classification.toString), 1, "auto", 1)

    TrainedRandomForestClassification(
      RandomForestParameters(1, "auto", "gini", 1, 1),
      model,
      features,
      predictionColumnName)
  }

  override def createScorableInstanceWithModel(trainedModelMock: PredictorSparkModel): Scorable =
    TrainedRandomForestClassification(
      RandomForestParameters(1, "auto", "gini", 1, 1),
      trainedModelMock.asInstanceOf[RandomForestModel],
      mock[Seq[String]],
      predictionColumnName)
}
