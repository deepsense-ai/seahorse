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
import ai.deepsense.deeplang.doperations.spark.wrappers.estimators.VectorIndexer

class VectorIndexerExample extends AbstractOperationExample[VectorIndexer] {
  override def dOperation: VectorIndexer = {
    val op = new VectorIndexer()
    op.estimator
      .setMaxCategories(3)
      .setInputColumn("vectors")
      .setNoInPlace("indexed")
    op.set(op.estimator.extractParamMap())
  }

  override def inputDataFrames: Seq[DataFrame] = {
    val data = Seq(
      Vectors.dense(1.0, 1.0, 0.0, 1.0),
      Vectors.dense(0.0, 1.0, 1.0, 1.0),
      Vectors.dense(-1.0, 1.0, 2.0, 0.0)).map(Tuple1(_))
    Seq(DataFrame.fromSparkDataFrame(sparkSQLSession.createDataFrame(data).toDF("vectors")))
  }
}
