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

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{ArrayType, StringType, StructField, StructType}

import ai.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import ai.deepsense.deeplang.doperations.spark.wrappers.estimators.CountVectorizer

class CountVectorizerExample extends AbstractOperationExample[CountVectorizer]{
  override def dOperation: CountVectorizer = {
    val op = new CountVectorizer()
    op.estimator
      .setInputColumn("lines")
      .setNoInPlace("lines_out")
      .setMinTF(3)
    op.set(op.estimator.extractParamMap())
  }

  override def inputDataFrames: Seq[DataFrame] = {
    val rows = Seq(
      Row("a a a b b c c c d ".split(" ").toSeq),
      Row("c c c c c c".split(" ").toSeq),
      Row("a".split(" ").toSeq),
      Row("e e e e e".split(" ").toSeq))
    val rdd = sparkContext.parallelize(rows)
    val schema = StructType(Seq(StructField("lines", ArrayType(StringType, containsNull = true))))
    Seq(DataFrameBuilder(sparkSQLSession).buildDataFrame(schema, rdd))
  }
}
