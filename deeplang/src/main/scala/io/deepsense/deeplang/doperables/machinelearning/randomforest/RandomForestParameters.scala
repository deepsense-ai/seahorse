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

package io.deepsense.deeplang.doperables.machinelearning.randomforest

import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.types.ColumnType.ColumnType
import io.deepsense.deeplang.doperables.machinelearning.ModelParameters

case class RandomForestParameters(
    numTrees: Int, featureSubsetStrategy: String, impurity: String, maxDepth: Int, maxBins: Int)
  extends ModelParameters {

  override def reportTableRows: Seq[(String, ColumnType, String)] =
    Seq(
      ("Num trees", ColumnType.numeric, numTrees.toString),
      ("Feature subset strategy", ColumnType.string, featureSubsetStrategy),
      ("Impurity", ColumnType.string, impurity),
      ("Max depth", ColumnType.numeric, maxDepth.toString),
      ("Max bins", ColumnType.numeric, maxBins.toString))
}
