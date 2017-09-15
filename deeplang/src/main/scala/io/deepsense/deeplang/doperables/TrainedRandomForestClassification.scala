/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.rdd.RDD

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.reportlib.model.{ReportContent, Table}

case class TrainedRandomForestClassification(
    model: RandomForestModel,
    featureColumns: Seq[String],
    targetColumn: String)
  extends RandomForestClassifier
  with Scorable
  with VectorScoring
  with DOperableSaver {

  def this() = this(null, null, null)

  override def toInferrable: DOperable = new TrainedRandomForestClassification()

  override def url: Option[String] = None

  override def transformFeatures(v: RDD[Vector]): RDD[Vector] = v

  override def vectors(dataFrame: DataFrame): RDD[Vector] =
    dataFrame.toSparkVectorRDDWithCategoricals(featureColumns)

  override def predict(vectors: RDD[Vector]): RDD[Double] = model.predict(vectors)

  override def report(executionContext: ExecutionContext): Report = {
    val featureColumnsColumn = featureColumns.toList.map(Some.apply)
    val targetColumnColumn = List(Some(targetColumn))
    val rows = featureColumnsColumn.zipAll(targetColumnColumn, Some(""), Some(""))
      .map{ case (a, b) => List(a, b) }

    val table = Table(
      "Trained Random Forest Classification",
      model.toString,
      Some(List("Feature columns", "Target column")),
      None,
      rows)

    Report(ReportContent("Report for TrainedRandomForestClassification", List(table)))
  }

  override def save(context: ExecutionContext)(path: String): Unit = ???
}
