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

package ai.deepsense.deeplang.doperables.spark.wrappers.estimators

import org.apache.spark.ml.recommendation.{ALS => SparkALS, ALSModel => SparkALSModel}
import org.apache.spark.sql.types.StructType

import ai.deepsense.commons.types.ColumnType
import ai.deepsense.deeplang.doperables.SparkEstimatorWrapper
import ai.deepsense.deeplang.doperables.dataframe.DataFrameColumnsGetter
import ai.deepsense.deeplang.doperables.spark.wrappers.models.ALSModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common._
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection
import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.wrappers.spark._

class ALS
  extends SparkEstimatorWrapper[SparkALSModel, SparkALS, ALSModel]
  with HasItemColumnParam
  with HasPredictionColumnCreatorParam
  with HasUserColumnParam
  with HasMaxIterationsParam
  with HasSeedParam
  with HasRegularizationParam
  with HasCheckpointIntervalParam {

  val alpha = new DoubleParamWrapper[SparkALS](
    name = "alpha",
    description = Some("The alpha parameter in the implicit preference formulation."),
    sparkParamGetter = _.alpha,
    validator = RangeValidator(0.0, Double.PositiveInfinity))
  setDefault(alpha, 1.0)

  val implicitPrefs = new BooleanParamWrapper[SparkALS](
    name = "implicit prefs",
    description = Some("Whether to use implicit preference."),
    sparkParamGetter = _.implicitPrefs)
  setDefault(implicitPrefs, false)

  val nonnegative = new BooleanParamWrapper[SparkALS](
    name = "nonnegative",
    description = Some("Whether to apply nonnegativity constraints for least squares."),
    sparkParamGetter = _.nonnegative)
  setDefault(nonnegative, true)

  val numItemBlocks = new IntParamWrapper[SparkALS](
    name = "num item blocks",
    description = Some("The number of item blocks."),
    sparkParamGetter = _.numItemBlocks,
    validator = RangeValidator(begin = 1.0, end = Int.MaxValue, step = Some(1.0)))
  setDefault(numItemBlocks, 10.0)

  val numUserBlocks = new IntParamWrapper[SparkALS](
    name = "num user blocks",
    description = Some("The number of user blocks."),
    sparkParamGetter = _.numUserBlocks,
    validator = RangeValidator(begin = 1.0, end = Int.MaxValue, step = Some(1.0)))
  setDefault(numUserBlocks, 10.0)

  val rank = new IntParamWrapper[SparkALS](
    name = "rank",
    description = Some("The rank of the matrix factorization."),
    sparkParamGetter = _.rank,
    validator = RangeValidator(begin = 1.0, end = Int.MaxValue, step = Some(1.0)))
  setDefault(rank, 10.0)

  val ratingColumn = new SingleColumnSelectorParamWrapper[SparkALS](
    name = "rating column",
    description = Some("The column for ratings."),
    sparkParamGetter = _.ratingCol,
    portIndex = 0)
  setDefault(ratingColumn, NameSingleColumnSelection("rating"))

  override val params: Array[Param[_]] = Array(
    alpha,
    checkpointInterval,
    implicitPrefs,
    maxIterations,
    nonnegative,
    numItemBlocks,
    numUserBlocks,
    rank,
    ratingColumn,
    regularizationParam,
    seed,
    itemColumn,
    predictionColumn,
    userColumn)

  override private[deeplang] def _fit_infer(maybeSchema: Option[StructType]): ALSModel = {
    maybeSchema.map {
      schema =>
        DataFrameColumnsGetter.assertExpectedColumnType(schema, $(itemColumn), ColumnType.numeric)
        DataFrameColumnsGetter.assertExpectedColumnType(schema, $(userColumn), ColumnType.numeric)
        DataFrameColumnsGetter.assertExpectedColumnType(schema, $(ratingColumn), ColumnType.numeric)
    }
    super._fit_infer(maybeSchema)
  }
}
