/**
 * Copyright 2016, deepsense.ai
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

package io.deepsense.deeplang.doperations.examples

import io.deepsense.sparkutils.Linalg.Vectors

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.spark.wrappers.estimators.ChiSqSelector
import io.deepsense.deeplang.params.selections.NameSingleColumnSelection

class ChiSqSelectorExample extends AbstractOperationExample[ChiSqSelector] {
  override def dOperation: ChiSqSelector = {
    val op = new ChiSqSelector()
    op.estimator
      .setFeaturesColumn(NameSingleColumnSelection("features"))
      .setLabelColumn(NameSingleColumnSelection("label"))
      .setOutputColumn("selected_features")
      .setNumTopFeatures(1)
    op.set(op.estimator.extractParamMap())
  }

  override def inputDataFrames: Seq[DataFrame] = {
    val data = Seq(
      (Vectors.dense(0.0, 0.0, 18.0, 1.0), 1.0),
      (Vectors.dense(0.0, 1.0, 12.0, 0.0), 0.0),
      (Vectors.dense(1.0, 0.0, 15.0, 0.1), 0.0)
    )
    Seq(DataFrame.fromSparkDataFrame(sparkSQLSession.createDataFrame(data).toDF("features", "label")))
  }
}
