/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang.doperations

import org.apache.spark.rdd.RDD

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.{ClassificationReporter, ColumnTypesPredicates, Evaluator, Report}
import io.deepsense.deeplang.parameters.NameSingleColumnSelection

case class EvaluateClassification() extends Evaluator {

  override val name: String = "Evaluate Classification"

  override val id: Id = "1163bb76-ba65-4471-9632-dfb761d20dfb"

  override protected def report(predictionsAndLabels: RDD[(Double, Double)]): Report =
    ClassificationReporter.report(predictionsAndLabels)

  override protected def validateTarget: ColumnTypesPredicates.Predicate =
    ColumnTypesPredicates.isNumericOrBinaryValued
}

object EvaluateClassification {
  def apply(
      targetColumnName: String,
      predictionColumnName: String): EvaluateClassification = {
    val operation = EvaluateClassification()

    operation.targetColumnParameter.value =
      Some(NameSingleColumnSelection(targetColumnName))
    operation.predictionColumnParameter.value =
      Some(NameSingleColumnSelection(predictionColumnName))

    operation
  }
}
