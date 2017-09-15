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

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.clustering.KMeans

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType
import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType.ExtendedColumnType
import io.deepsense.deeplang.doperables.machinelearning.kmeans.{KMeansParameters, UntrainedKMeansClustering}
import io.deepsense.reportlib.model.{ReportContent, Table}

class UntrainedKMeansClusteringIntegSpec
    extends UnsupervisedTrainableBaseIntegSpec("UntrainedKMeansClustering") {

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

  override def createTrainableInstance: UnsupervisedTrainable =
    UntrainedKMeansClustering(KMeansParameters(2, 1, KMeans.RANDOM, None, 123, 1, 1e-4))

  "UntrainedKMeansClustering" should {
    "create a report" in {
      val untrainedKMeansClustering = UntrainedKMeansClustering(
        KMeansParameters(2, 100, KMeans.RANDOM, None, 123, 1, 1e-4))

      untrainedKMeansClustering.report(mock[ExecutionContext]) shouldBe Report(ReportContent(
        "Report for Untrained k-means Clustering",
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
                Some("100"),
                Some(KMeans.RANDOM),
                Some(""),
                Some("123"),
                Some("1"),
                Some("1.0E-4"))
            )
          )
        )
      ))
    }
  }
}
