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

package ai.deepsense.deeplang.doperables.wrappers

import org.apache.spark.ml.evaluation
import org.apache.spark.ml.param.{Param, ParamMap}
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.Evaluator
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.params.wrappers.deeplang.ParamWrapper
import ai.deepsense.sparkutils.ML

class EvaluatorWrapper(
    context: ExecutionContext,
    evaluator: Evaluator)
  extends ML.Evaluator {

  override def evaluateDF(dataset: sql.DataFrame): Double = {
    evaluator.evaluate(context)(())(DataFrame.fromSparkDataFrame(dataset.toDF())).value
  }

  override def copy(extra: ParamMap): evaluation.Evaluator = {
    val params = ParamTransformer.transform(extra)
    val evaluatorCopy = evaluator.replicate().set(params: _*)
    new EvaluatorWrapper(context, evaluatorCopy)
  }

  override lazy val params: Array[Param[_]] = {
    evaluator.params.map(new ParamWrapper(uid, _))
  }

  override def isLargerBetter: Boolean = evaluator.isLargerBetter

  override val uid: String = Identifiable.randomUID("EvaluatorWrapper")
}
