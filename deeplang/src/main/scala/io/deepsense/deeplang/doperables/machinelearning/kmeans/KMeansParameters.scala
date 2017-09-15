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

import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.types.ColumnType.ColumnType
import io.deepsense.deeplang.doperables.machinelearning.ModelParameters

case class KMeansParameters(
    numClusters: Int,
    maxIterations: Int,
    initializationMode: String,
    initializationSteps: Option[Int],
    seed: Int,
    runs: Int,
    epsilon: Double)
  extends ModelParameters {

  override def reportTableRows: Seq[(String, ColumnType, String)] = {
    val initializationStepsInfo = initializationSteps.map(_.toString).getOrElse("")
    Seq(
      ("Number of clusters", ColumnType.numeric, numClusters.toString),
      ("Max iterations", ColumnType.numeric, maxIterations.toString),
      ("Initialization mode", ColumnType.string, initializationMode.toString),
      ("Initialization steps", ColumnType.numeric, initializationStepsInfo),
      ("Seed", ColumnType.numeric, seed.toString),
      ("Runs", ColumnType.numeric, runs.toString),
      ("Epsilon", ColumnType.numeric, epsilon.toString))
  }
}
