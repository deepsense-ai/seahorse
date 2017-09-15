/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations.examples

import ai.deepsense.sparkutils.Linalg.Vectors

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.spark.wrappers.estimators.MinMaxScaler

class MinMaxScalerExample extends AbstractOperationExample[MinMaxScaler] {
  override def dOperation: MinMaxScaler = {
    val op = new MinMaxScaler()
    op.estimator
      .setInputColumn("features")
      .setNoInPlace("scaled")
      .setMax(5)
      .setMin(-5)
    op.set(op.estimator.extractParamMap())
  }

  override def inputDataFrames: Seq[DataFrame] = {
    val data = Array(
      Vectors.dense(1, 0, Long.MinValue),
      Vectors.dense(2, 0, 0),
      Vectors.sparse(3, Array(0, 2), Array(3, Long.MaxValue)).toDense,
      Vectors.sparse(3, Array(0), Array(1.5)).toDense
    ).map(Tuple1(_))
    Seq(DataFrame.fromSparkDataFrame(sparkSQLSession.createDataFrame(data).toDF("features")))
  }
}
