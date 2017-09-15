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

package io.deepsense.deeplang.doperables.machinelearning.randomforest.regression

import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.rdd.RDD

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.machinelearning.randomforest.RandomForestParameters
import io.deepsense.deeplang.{DOperable, ExecutionContext}

case class TrainedRandomForestRegression(
    modelParameters: RandomForestParameters,
    model: RandomForestModel,
    featureColumns: Seq[String],
    targetColumn: String)
  extends RandomForestRegressor
  with Scorable
  with VectorScoring
  with DOperableSaver {

  def this() = this(null, null, null, null)

  override def toInferrable: DOperable = new TrainedRandomForestRegression()

  override def url: Option[String] = None

  override def transformFeatures(v: RDD[Vector]): RDD[Vector] = v

  override def vectors(dataFrame: DataFrame): RDD[Vector] =
    dataFrame.selectSparkVectorRDD(featureColumns, ColumnTypesPredicates.isNumericOrCategorical)

  override def predict(vectors: RDD[Vector]): RDD[Double] = model.predict(vectors)

  override def report(executionContext: ExecutionContext): Report = {
    DOperableReporter("Trained Random Forest Regression")
      .withParameters(
        description = model.toString,
        ("Num trees", ColumnType.numeric, modelParameters.numTrees.toString),
        ("Feature subset strategy", ColumnType.string, modelParameters.featureSubsetStrategy),
        ("Impurity", ColumnType.string, modelParameters.impurity),
        ("Max depth", ColumnType.numeric, modelParameters.maxDepth.toString),
        ("Max bins", ColumnType.numeric, modelParameters.maxBins.toString)
      )
      .withVectorScoring(this)
      .report
  }

  override def save(context: ExecutionContext)(path: String): Unit = ???
}
