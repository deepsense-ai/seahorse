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

package io.deepsense.deeplang.doperables.machinelearning.svm.classification

import org.apache.spark.mllib.classification.SVMModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.doperables.ColumnTypesPredicates._
import io.deepsense.deeplang.doperables.{ColumnTypesPredicates, Report, Scorable, VectorScoring}
import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.reportlib.model.{ReportContent, Table}


case class TrainedSupportVectorMachineClassifier(
    model: SVMModel,
    featureColumns: Seq[String],
    targetColumn: String)
  extends SupportVectorMachineClassifier
  with Scorable
  with VectorScoring {

  def this() = this(null, null, null)

  override def report(executionContext: ExecutionContext): Report = {
    val featureColumnsColumn = featureColumns.toList.map(Some.apply)

    val weightsRows =
      featureColumnsColumn zip model.weights.toArray map {
        case (featureOpt, weight) => List(featureOpt, Some(weight.toString))
      }

    val weightsTable = Table(
      "Computed weights",
      "",
      Some(List("Feature", "Weight")),
      List(ColumnType.string, ColumnType.numeric),
      None, weightsRows)

    val targetTable = Table(
      name = "Target column",
      description = "",
      columnNames = None,
      columnTypes = List(ColumnType.string),
      rowNames = None,
      values = List(List(Some(targetColumn))))

    val interceptTable = Table(
      name = "Intercept",
      description = "",
      columnNames = None,
      columnTypes = List(ColumnType.numeric),
      rowNames = None,
      values = List(List(Some(model.intercept.toString))))

    Report(ReportContent(
      "Report for Trained SVM Classification",
      List(weightsTable, targetTable, interceptTable)))
  }

  override def toInferrable: DOperable = new TrainedSupportVectorMachineClassifier()

  override def featurePredicate: Predicate = ColumnTypesPredicates.isNumeric

  override def transformFeatures(v: RDD[Vector]): RDD[Vector] = v

  override def predict(features: RDD[Vector]): RDD[Double] = model.predict(features)

  override def save(executionContext: ExecutionContext)(path: String): Unit = ???
}
