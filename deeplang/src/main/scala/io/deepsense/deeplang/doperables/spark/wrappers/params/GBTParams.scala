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

package io.deepsense.deeplang.doperables.spark.wrappers.params

import scala.language.reflectiveCalls

import org.apache.spark.ml

import io.deepsense.deeplang.doperables.spark.wrappers.params.common._
import io.deepsense.deeplang.params.Params
import io.deepsense.deeplang.params.validators.RangeValidator
import io.deepsense.deeplang.params.wrappers.spark.{DoubleParamWrapper, IntParamWrapper}

trait GBTParams extends Params
  with PredictorParams
  with HasLabelColumnParam
  with HasMaxIterationsParam
  with HasSeedParam
  with HasStepSizeParam {

  val maxBins = new IntParamWrapper[ml.param.Params { val maxBins: ml.param.IntParam }](
    name = "max bins",
    description = "Maximum number of bins used for discretizing continuous features " +
      "and for choosing how to split on features at each node. " +
      "More bins give higher granularity. " +
      "Must be >= 2 and >= number of categories in any categorical feature.",
    sparkParamGetter = _.maxBins,
    RangeValidator(2.0, Int.MaxValue, step = Some(1.0)))
  setDefault(maxBins, 32.0)

  val maxDepth = new IntParamWrapper[ml.param.Params { val maxDepth: ml.param.IntParam }](
    name = "max depth",
    description = "Maximum depth of the tree. " +
      "E.g., depth 0 means 1 leaf node; depth 1 means 1 internal node + 2 leaf nodes.",
    sparkParamGetter = _.maxDepth,
    RangeValidator(0, 30, step = Some(1.0)))
  setDefault(maxDepth, 5.0)

  val minInfoGain =
    new DoubleParamWrapper[ml.param.Params { val minInfoGain: ml.param.DoubleParam }](
      name = "min information gain",
      description = "Minimum information gain for a split to be considered at a tree node.",
      sparkParamGetter = _.minInfoGain,
      RangeValidator(0.0, Double.MaxValue))
  setDefault(minInfoGain, 0.0)

  val minInstancesPerNode =
    new IntParamWrapper[ml.param.Params { val minInstancesPerNode: ml.param.IntParam }](
      name = "min instances per node",
      description = "Minimum number of instances each child must have after split. " +
        "If a split causes the left or right child to have fewer than minInstancesPerNode, " +
        "the split will be discarded as invalid.",
      sparkParamGetter = _.minInstancesPerNode,
      RangeValidator(1.0, Int.MaxValue, step = Some(1.0)))
  setDefault(minInstancesPerNode, 1.0)

  val subsamplingRate =
    new DoubleParamWrapper[ml.param.Params { val subsamplingRate: ml.param.DoubleParam }](
      name = "subsampling rate",
      description =
        "Fraction of the training data used for learning each decision tree.",
      sparkParamGetter = _.subsamplingRate,
      RangeValidator(0.0, 1.0, beginIncluded = false))
  setDefault(subsamplingRate, 1.0)

}
