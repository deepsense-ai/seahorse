/**
 * Copyright 2015, deepsense.ai
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

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.spark.wrappers.estimators.StringIndexer
import io.deepsense.deeplang.doperations.spark.wrappers.transformers.OneHotEncode

class OneHotEncodeExample extends AbstractOperationExample[OneHotEncode]{
  override def dOperation: OneHotEncode = {
    val op = new OneHotEncode()
    op.transformer
      .setSingleColumn("labels", "encoded")
    op.set(op.transformer.extractParamMap())
  }

  override def inputDataFrames: Seq[DataFrame] = {
    val data = "a a b c a b a a c".split(" ").map(Tuple1(_))
    val rawDataFrame =
      DataFrame.fromSparkDataFrame(sparkSQLSession.createDataFrame(data).toDF("features"))
    val x = new StringIndexer()
      .setSingleColumn("features", "labels")
      .executeUntyped(Vector(rawDataFrame))(executionContext)
    Seq(x.head.asInstanceOf[DataFrame])
  }
}
