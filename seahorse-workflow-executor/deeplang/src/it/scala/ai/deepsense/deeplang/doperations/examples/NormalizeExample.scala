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
import ai.deepsense.deeplang.doperations.spark.wrappers.transformers.Normalize

class NormalizeExample extends AbstractOperationExample[Normalize] {
  override def dOperation: Normalize = {
    val op = new Normalize()
    op.transformer
      .setSingleColumn("features", "normalized")
    op.set(op.transformer.extractParamMap())
  }

  override def inputDataFrames: Seq[DataFrame] = {
    val data = Seq(
      Vectors.sparse(3, Seq((0, -2.0), (1, 2.3))).toDense,
      Vectors.dense(0.0, 0.0, 0.0),
      Vectors.dense(0.6, -1.1, -3.0),
      Vectors.sparse(3, Seq((1, 0.91), (2, 3.2))).toDense,
      Vectors.sparse(3, Seq((0, 5.7), (1, 0.72), (2, 2.7))).toDense,
      Vectors.sparse(3, Seq()).toDense).map(Tuple1(_))
    Seq(DataFrame.fromSparkDataFrame(sparkSQLSession.createDataFrame(data).toDF("features")))
  }
}
