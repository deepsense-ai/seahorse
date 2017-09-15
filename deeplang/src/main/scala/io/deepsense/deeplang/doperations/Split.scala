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

import scala.reflect.runtime.{universe => ru}

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.HasSeedParam
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.validators.RangeValidator
import io.deepsense.deeplang.params.{NumericParam, Params}

case class Split()
  extends DOperation1To2[DataFrame, DataFrame, DataFrame]
  with Params
  with HasSeedParam {

  override val name: String = "Split"
  override val id: DOperation.Id = "d273c42f-b840-4402-ba6b-18282cc68de3"
  override val description: String =
    "Splits a DataFrame into two DataFrames"

  val splitRatio = NumericParam(
    name = "split ratio",
    description = "Percentage of rows that should end up in the first output DataFrame.",
    validator = RangeValidator(0.0, 1.0, beginIncluded = true, endIncluded = true))
  setDefault(splitRatio, 0.5)

  def getSplitRatio: Double = $(splitRatio)
  def setSplitRatio(value: Double): this.type = set(splitRatio, value)

  def getSeed: Int = $(seed).toInt
  def setSeed(value: Int): this.type = set(seed, value.toDouble)

  val params = declareParams(splitRatio, seed)

  override protected def _execute(context: ExecutionContext)
                                 (df: DataFrame): (DataFrame, DataFrame) = {
    val Array(f1: RDD[Row], f2: RDD[Row]) = split(df, getSplitRatio, getSeed)
    val schema = df.sparkDataFrame.schema
    val dataFrame1 = context.dataFrameBuilder.buildDataFrame(schema, f1)
    val dataFrame2 = context.dataFrameBuilder.buildDataFrame(schema, f2)
    (dataFrame1, dataFrame2)
  }

  def split(df: DataFrame, range: Double, seed: Long): Array[RDD[Row]] = {
    df.sparkDataFrame.rdd.randomSplit(Array(range, 1.0 - range), seed)
  }

  override protected def _inferFullKnowledge(context: InferContext)
      (knowledge: DKnowledge[DataFrame]):
      ((DKnowledge[DataFrame], DKnowledge[DataFrame]), InferenceWarnings) = {
    ((knowledge, knowledge), InferenceWarnings.empty)
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_1: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}
