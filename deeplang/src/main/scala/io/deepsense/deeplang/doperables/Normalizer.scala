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

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.feature.StandardScalerModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row

import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.reportlib.model.ReportContent

case class Normalizer(columns: Seq[String], scaler: StandardScalerModel) extends Transformation {

  def this() = this(Seq.empty, null)

  def normalize: DMethod1To1[Unit, DataFrame, DataFrame] = transform

  override val transform = new DMethod1To1[Unit, DataFrame, DataFrame] {
    override def apply(context: ExecutionContext)(p: Unit)(dataFrame: DataFrame): DataFrame = {
      val localColumns = columns
      val localScaler = scaler
      val vectors = dataFrame.selectSparkVectorRDD(localColumns, ColumnTypesPredicates.isNumeric)
      val transformed: RDD[Vector] = localScaler.transform(vectors)
      val resultRdd = transformed.zip(dataFrame.sparkDataFrame.rdd).map {
        case (vector, row) => {
          val indexToTransformedValue = vector.toArray.zip(localColumns).map {
            case (transformedValue, columnName) => row.fieldIndex(columnName) -> transformedValue
          }.toMap
          val transformedSeq = row.toSeq.zipWithIndex.map {
            case (originalValue, index) => indexToTransformedValue.getOrElse(index, originalValue)
          }
          Row(transformedSeq: _*)
        }
      }
      context.dataFrameBuilder.buildDataFrame(dataFrame.sparkDataFrame.schema, resultRdd)
    }

    override def inferFull(context: InferContext)(p: Unit)(
        dataFrameKnowledge: DKnowledge[DataFrame]): (DKnowledge[DataFrame], InferenceWarnings) = {
      (dataFrameKnowledge, InferenceWarnings.empty)
    }
  }

  override def report(executionContext: ExecutionContext): Report = {
    DOperableReporter("Report for Normalizer")
      .withCustomTable(
        name = "Statistics",
        description = "",
        ("Column", ColumnType.string, columns),
        ("Mean", ColumnType.numeric, scaler.mean.toArray.map(DoubleUtils.double2String)),
        ("Std", ColumnType.numeric, scaler.std.toArray.map(DoubleUtils.double2String))
      )
      .report
  }

  override def toInferrable: DOperable = new Normalizer()

  override def save(executionContext: ExecutionContext)(path: String): Unit =
    throw new UnsupportedOperationException("Save is not supported for Normalizer.")
}
