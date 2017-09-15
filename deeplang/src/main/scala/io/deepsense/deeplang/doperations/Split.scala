/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.deeplang.doperations

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{NumericParameter, ParametersSchema, RangeValidator}
import io.deepsense.deeplang.{DOperation, DOperation1To2, ExecutionContext}

case class Split() extends DOperation1To2[DataFrame, DataFrame, DataFrame] {
  override val name: String = "Split"
  override val id: DOperation.Id = "d273c42f-b840-4402-ba6b-18282cc68de3"

  import Split._
  override protected def _execute(context: ExecutionContext)
                                 (df: DataFrame): (DataFrame, DataFrame) = {
    val range: Double = parameters.getNumericParameter(SplitRatioParam).value.get
    val seed: Long = parameters.getNumericParameter(SeedParam).value.get.toLong
    val Array(f1: RDD[Row], f2: RDD[Row]) = split(df, range, seed)
    val schema = df.sparkDataFrame.schema
    val dataFrame1 = context.dataFrameBuilder.buildDataFrame(schema, f1)
    val dataFrame2 = context.dataFrameBuilder.buildDataFrame(schema, f2)
    (dataFrame1, dataFrame2)
  }

  def split(df: DataFrame, range: Double, seed: Long): Array[RDD[Row]] = {
    df.sparkDataFrame.rdd.randomSplit(Array(range, 1.0 - range), seed)
  }

  override val parameters: ParametersSchema = ParametersSchema(
    SplitRatioParam ->
      NumericParameter("Proportion of splitting",
        default = Some(0.5),
        required = true,
        RangeValidator(0.0, 1.0, true, true)
      ),
    SeedParam ->
      NumericParameter("Seed value",
        default = Some(1.0),
        required = true,
        // TODO Fix RangeValidator, because now it can't handle Int.MinValue and Int.MaxValue
        RangeValidator(Int.MinValue / 2, Int.MaxValue / 2, true, true, Some(1.0))
      )
  )
}

object Split {
  val SplitRatioParam = "split ratio"
  val SeedParam = "seed"

  def apply(splitRatio: Double, seed: Long): Split = {
    val splitter = new Split
    splitter.parameters.getNumericParameter(SplitRatioParam).value = Some(splitRatio)
    splitter.parameters.getNumericParameter(SeedParam).value = Some(seed)
    splitter
  }
}
