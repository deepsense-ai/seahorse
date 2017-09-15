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
import org.apache.spark.mllib.linalg.{Vector, Vectors}

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType
import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType.ExtendedColumnType
import io.deepsense.deeplang.doperables.{Report, Scorable, ScorableBaseIntegSpec, UnsupervisedPredictorModelBaseIntegSpec}
import io.deepsense.reportlib.model.{ReportContent, Table}

class TrainedKMeansClusteringIntegSpec
    extends ScorableBaseIntegSpec("TrainedKMeansClustering")
    with UnsupervisedPredictorModelBaseIntegSpec {

  private val params = KMeansParameters(2, 1, KMeans.RANDOM, None, 123, 1, 1e-4)

  override def acceptedFeatureTypes: Seq[ExtendedColumnType] = Seq(
    ExtendedColumnType.binaryValuedNumeric,
    ExtendedColumnType.nonBinaryValuedNumeric)

  override def unacceptableFeatureTypes: Seq[ExtendedColumnType] = Seq(
    ExtendedColumnType.categorical1,
    ExtendedColumnType.categorical2,
    ExtendedColumnType.categoricalMany,
    ExtendedColumnType.boolean,
    ExtendedColumnType.string,
    ExtendedColumnType.timestamp)

  override def mockTrainedModel(): PredictorSparkModel = {
    class KMeansClusteringPredictor
        extends KMeansModel(mock[Array[Vector]])
        with PredictorSparkModel {}

    mock[KMeansClusteringPredictor]
  }

  override def createScorableInstance(features: String*): Scorable =
    TrainedKMeansClustering(
      params,
      new KMeansModel(Array(Vectors.dense(Array(1.0)), Vectors.dense(Array(2.0)))),
      features)

  override def createScorableInstanceWithModel(trainedModelMock: PredictorSparkModel): Scorable =
    TrainedKMeansClustering(
      params,
      trainedModelMock.asInstanceOf[KMeansModel],
      mock[Seq[String]])

  "TrainedKMeansClustering" should {
    "create a report" in {

      val kMeansModel = new KMeansModel(
        Array(
          Vectors.dense(1.0, 2.0),
          Vectors.dense(3.0, 4.0)))

      val features = Seq("x", "y")

      val kMeansClustering = TrainedKMeansClustering(
        params, kMeansModel, features)

      kMeansClustering.report(mock[ExecutionContext]) shouldBe Report(ReportContent(
        "Report for Trained k-means Clustering",
        tables = Map(
          "Parameters" -> Table(
            "Parameters", "",
            Some(List(
              "Number of clusters",
              "Max iterations",
              "Initialization mode",
              "Initialization steps",
              "Seed",
              "Runs",
              "Epsilon")),
            List(
              ColumnType.numeric,
              ColumnType.numeric,
              ColumnType.string,
              ColumnType.numeric,
              ColumnType.numeric,
              ColumnType.numeric,
              ColumnType.numeric),
            None,
            List(
              List(
                Some("2"),
                Some("1"),
                Some(KMeans.RANDOM),
                Some(""),
                Some("123"),
                Some("1"),
                Some("1.0E-4"))
            )
          ),
          "Feature columns" -> Table(
            "Feature columns", "",
            Some(List("Feature columns")),
            List(ColumnType.string),
            None,
            List(List(Some("x")), List(Some("y")))
          ),
          "Centers of clusters" -> Table(
            "Centers of clusters", "",
            Some(List("x", "y")),
            List(ColumnType.numeric, ColumnType.numeric),
            None,
            List(List(Some("1.0"), Some("2.0")), List(Some("3.0"), Some("4.0")))
          )
        )
      ))
    }
  }
}
