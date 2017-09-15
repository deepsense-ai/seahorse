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
import ai.deepsense.deeplang.doperations.spark.wrappers.transformers.ConvertToNGrams

class ConvertToNGramsExample extends AbstractOperationExample[ConvertToNGrams]{
  override def dOperation: ConvertToNGrams = {
    val op = new ConvertToNGrams()
    op.transformer
      .setSingleColumn("words", "output")
      .setN(3)

    op.set(op.transformer.extractParamMap())
  }

  override def inputDataFrames: Seq[DataFrame] = {
    Seq(DataFrame.fromSparkDataFrame(sparkSQLSession.createDataFrame(Seq(
      (0, Array("Hi", "I", "heard", "about", "Spark")),
      (1, Array("I", "wish", "Java", "could", "use", "case", "classes")),
      (2, Array("Logistic", "regression", "models", "are", "neat"))
    )).toDF("label", "words")))
  }
}
