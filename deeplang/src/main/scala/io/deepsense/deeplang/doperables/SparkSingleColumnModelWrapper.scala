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

import org.apache.spark.ml
import org.apache.spark.ml.param.{ParamMap => SparkParamMap, Params}
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import io.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleColumnInPlaceChoice
import io.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.{NoInPlaceChoice, YesInPlaceChoice}
import io.deepsense.deeplang.doperables.multicolumn._
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.HasInputColumn
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers

abstract class SparkSingleColumnModelWrapper[
    MD <: ml.Model[MD]{ val outputCol: ml.param.Param[String]},
    E <: ml.Estimator[MD]{ val outputCol: ml.param.Param[String]}]
  extends SparkModelWrapper[MD, E]
  with ParamsWithSparkWrappers
  with HasInputColumn
  with HasSingleInPlaceParam
  with HasSpecificParams {

  private var outputColumnValue: Option[String] = None

  override lazy val params: Array[Param[_]] =
    Array(inputColumn, singleInPlaceChoice) ++ getSpecificParams

  override private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    $(singleInPlaceChoice) match {
      case YesInPlaceChoice() =>
        SingleColumnTransformerUtils.transformSingleColumnInPlace(
          df.getColumnName($(inputColumn)),
          df,
          ctx,
          transformTo(ctx, df))
      case no: NoInPlaceChoice =>
        transformTo(ctx, df)(no.getOutputColumn)
    }
  }

  override private[deeplang] def _transformSchema(schema: StructType): Option[StructType] = {
    $(singleInPlaceChoice) match {
      case YesInPlaceChoice() =>
        val inputColumnName = DataFrameColumnsGetter.getColumnName(schema, $(inputColumn))
        val temporaryColumnName =
          DataFrameColumnsGetter.uniqueSuffixedColumnName(inputColumnName)
        val temporarySchema: Option[StructType] = transformSchemaTo(schema, temporaryColumnName)

        temporarySchema.map { schema =>
          StructType(schema.collect {
            case field if field.name == inputColumnName =>
              schema(temporaryColumnName).copy(name = inputColumnName)
            case field if field.name != temporaryColumnName =>
              field
          })
        }
      case no: NoInPlaceChoice =>
        transformSchemaTo(schema, no.getOutputColumn)
    }
  }

  override def sparkParamMap(sparkEntity: Params, schema: StructType): SparkParamMap = {
    val map = super.sparkParamMap(sparkEntity, schema)
      .put(ml.param.ParamPair(parentEstimator.sparkEstimator.outputCol, outputColumnValue.orNull))

    if (model != null) {
      map.put(ml.param.ParamPair(model.outputCol, outputColumnValue.orNull))
    } else {
      map
    }
  }

  def setSingleInPlaceParam(value: SingleColumnInPlaceChoice): this.type = {
    set(singleInPlaceChoice -> value)
  }

  private def transformTo(
    ctx: ExecutionContext,
    df: DataFrame)(outputColumnName: String): DataFrame = {
    withOutputColumnValue(outputColumnName) {
      super._transform(ctx, df)
    }
  }

  private def transformSchemaTo(
      schema: StructType,
      temporaryColumnName: String): Option[StructType] = {
    withOutputColumnValue(temporaryColumnName) {
      super._transformSchema(schema)
    }
  }

  private def withOutputColumnValue[T](columnName: String)(f: => T): T = {
    outputColumnValue = Some(columnName)
    try {
      f
    } finally {
      outputColumnValue = None
    }
  }

  override def replicate(
      extra: io.deepsense.deeplang.params.ParamMap): SparkSingleColumnModelWrapper.this.type = {
    val model = super.replicate(extractParamMap(extra)).asInstanceOf[this.type]
    model.outputColumnValue = outputColumnValue
    model
  }
}
