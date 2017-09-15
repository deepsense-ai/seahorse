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

package io.deepsense.deeplang.doperables.machinelearning.kmeans

import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.{Vector => SparkVector}

import io.deepsense.deeplang.doperables.ColumnTypesPredicates.Predicate
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.{DKnowledge, DOperable, ExecutionContext}

case class UntrainedKMeansClustering(modelParameters: KMeansParameters)
  extends KMeansClustering
  with UnsupervisedTrainable {

  def this() = this(null)

  /**
   * This method should be overridden with the actual execution of training.
   * It accepts [[UnsupervisedTrainableParameters]] and returns a [[Scorable]] instance.
   */
  override protected def actualTraining: TrainScorable = (trainParameters) => {

    val untrainedClusteringModel = new KMeans()
      .setK(modelParameters.numClusters)
      .setMaxIterations(modelParameters.maxIterations)
      .setInitializationMode(modelParameters.initializationMode)
      .setInitializationSteps(modelParameters.initializationSteps.getOrElse(1))
      .setSeed(modelParameters.seed)
      .setRuns(modelParameters.runs)
      .setEpsilon(modelParameters.epsilon)

    val trainedClusteringModel = untrainedClusteringModel.run(trainParameters.featureValues)

    TrainedKMeansClustering(
      modelParameters,
      trainedClusteringModel,
      trainParameters.features)
  }

  override protected def actualInference(context: InferContext)
                                        (parameters: UnsupervisedTrainableParameters)
                                        (dataFrame: DKnowledge[DataFrame])
                                        : (DKnowledge[Scorable], InferenceWarnings) =
    (DKnowledge(new TrainedKMeansClustering()), InferenceWarnings.empty)

  /**
   * The predicate all feature columns have to meet.
   */
  override protected def featurePredicate: Predicate = ColumnTypesPredicates.isNumeric

  /**
   * Saves DOperable on FS under specified path.
   * Sets url so that it informs where it has been saved.
   */
  override def save(executionContext: ExecutionContext)(path: String): Unit = ???

  /**
   * Called on executable version of object, produces an inferrable version of this object.
   * @return Inferrable version of this DOperable.
   */
  override def toInferrable: DOperable = new UntrainedKMeansClustering()

  override def report(executionContext: ExecutionContext): Report =
    DOperableReporter("Report for Untrained k-means Clustering")
      .withParameters(modelParameters)
      .report
}
