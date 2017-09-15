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

import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD

import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.types.ColumnType.ColumnType
import io.deepsense.deeplang.doperables.ColumnTypesPredicates.Predicate
import io.deepsense.deeplang.doperables.{ColumnTypesPredicates, DOperableReporter, Report, Scorable}
import io.deepsense.deeplang.{DOperable, ExecutionContext}

case class TrainedKMeansClustering(
    modelParameters: KMeansParameters,
    model: KMeansModel,
    featureColumns: Seq[String])
  extends KMeansClustering
  with Scorable {

  def this() = this(null, null, null)

  override def transformFeatures(v: RDD[Vector]): RDD[Vector] = v

  override protected def featurePredicate: Predicate = ColumnTypesPredicates.isNumeric

  override def predict(features: RDD[Vector]): RDD[Double] = {
    model.predict(features).map((value: Int) => value.toDouble)
  }

  override def report(executionContext: ExecutionContext): Report =
    DOperableReporter("Report for Trained k-means Clustering")
      .withParameters(modelParameters)
      .withUnsupervisedScorable(this)
      .withCustomTable(
        name = "Centers of clusters",
        description = "",
        centersOfClustersReportRows: _*
      )
      .report

  private def centersOfClustersReportRows: Seq[(String, ColumnType, Seq[String])] = {
    val numDimensions = model.clusterCenters.head.size
    for(i <- 0 to numDimensions - 1) yield (
      featureColumns(i),
      ColumnType.numeric,
      model.clusterCenters.toSeq.map(center => center(i).toString))
  }

  /**
   * Saves DOperable on FS under specified path.
   * Sets url so that it informs where it has been saved.
   */
  override def save(executionContext: ExecutionContext)(path: String): Unit = ???

  /**
   * Called on executable version of object, produces an inferrable version of this object.
   * @return Inferrable version of this DOperable.
   */
  override def toInferrable: DOperable = new TrainedKMeansClustering()
}
