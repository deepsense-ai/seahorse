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

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.spark.wrappers.estimators.Word2Vec

class Word2VecExample extends AbstractOperationExample[Word2Vec] {
  override def dOperation: Word2Vec = {
    val op = new Word2Vec()
    op.estimator
      .setInputColumn("words")
      .setNoInPlace("vectors")
      .setMinCount(2)
      .setVectorSize(5)
    op.set(op.estimator.extractParamMap())
  }

  override def inputDataFrames: Seq[DataFrame] = {
    val data = Seq(
      "Lorem ipsum at dolor".split(" "),
      "Nullam gravida non ipsum".split(" "),
      "Etiam at nunc lacinia".split(" ")
    ).map(Tuple1(_))
    Seq(DataFrame.fromSparkDataFrame(sparkSQLSession.createDataFrame(data).toDF("words")))
  }
}
