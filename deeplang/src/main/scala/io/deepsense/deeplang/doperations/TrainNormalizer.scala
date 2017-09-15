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

package io.deepsense.deeplang.doperations

import scala.reflect.runtime.{universe => ru}

import org.apache.spark.mllib.feature.{StandardScaler, StandardScalerModel}

import io.deepsense.deeplang.doperables.Normalizer
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperation, DOperation1To2, ExecutionContext}

case class TrainNormalizer() extends DOperation1To2[DataFrame, DataFrame, Normalizer] {
  override val id: DOperation.Id = "9ae1a3f8-a70b-44bf-b162-e3b16dc6fded"
  override val name = "Train Normalizer"

  val selectedColumnsParameter = ColumnSelectorParameter(
    "Columns to be normalized",
    required = true,
    portIndex = 0
  )

  override val parameters: ParametersSchema = ParametersSchema(
    TrainNormalizer.SelectedColumns -> selectedColumnsParameter
  )

  override protected def _execute(
      context: ExecutionContext)(
      dataFrame: DataFrame): (DataFrame, Normalizer) = {

    val normalizer: Normalizer = createTrainedNormalizer(dataFrame)
    val transformedDataFrame = normalizer.transform.apply(context)(())(dataFrame)
    (transformedDataFrame, normalizer)
  }

  def createTrainedNormalizer(dataFrame: DataFrame): Normalizer = {
    val columnsNames: Seq[String] = dataFrame.getColumnNames(selectedColumnsParameter.value.get)
    val scaler = createTrainedScaler(dataFrame, columnsNames)
    Normalizer(columnsNames, scaler)
  }

  def createTrainedScaler(dataFrame: DataFrame, columnsNames: Seq[String]): StandardScalerModel = {
    val vectors = dataFrame.toSparkVectorRDD(columnsNames)
    vectors.cache()
    val scaler: StandardScalerModel =
      new StandardScaler(withStd = true, withMean = true).fit(vectors)
    vectors.unpersist()
    scaler
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_1: ru.TypeTag[Normalizer] = ru.typeTag[Normalizer]
}

object TrainNormalizer {
  val SelectedColumns = "selected columns"

  def apply(
      names: Set[String] = Set.empty,
      indices: Set[Int] = Set.empty): TrainNormalizer = {
    val trainNormalizer = new TrainNormalizer
    val params = trainNormalizer.parameters
    params.getColumnSelectorParameter(SelectedColumns).value =
      Some(MultipleColumnSelection(Vector(
        NameColumnSelection(names),
        IndexColumnSelection(indices))))
    trainNormalizer
  }
}
