/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import ai.deepsense.deeplang.doperables.GetFromVectorTransformer
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.GetFromVector

class GetFromVectorExample extends AbstractOperationExample[GetFromVector] {
  override def dOperation: GetFromVector = {
    val op = new GetFromVector()
    val transformer = new GetFromVectorTransformer()
    op.transformer.setIndex(1)
    op.transformer.setSingleColumn("features", "second_feature")
    op.set(op.transformer.extractParamMap())
  }

  override def inputDataFrames: Seq[DataFrame] = {
    val data = Seq(
      Vectors.sparse(3, Seq((0, -2.0), (1, 2.3))),
      Vectors.dense(0.01, 0.2, 3.0),
      null,
      Vectors.sparse(3, Seq((1, 0.91), (2, 3.2))),
      Vectors.sparse(3, Seq((0, 5.7), (2, 2.7))),
      Vectors.sparse(3, Seq()).toDense).map(Tuple1(_))
    Seq(DataFrame.fromSparkDataFrame(sparkSQLSession.createDataFrame(data).toDF("features")))
  }
}
