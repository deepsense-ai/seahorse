/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import scala.language.reflectiveCalls

import org.apache.spark.ml
import org.apache.spark.ml.classification.{NaiveBayes => SparkNaiveBayes, NaiveBayesModel => SparkNaiveBayesModel}

import ai.deepsense.deeplang.doperables.SparkEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators.NaiveBayes.{ModelType, Multinomial}
import ai.deepsense.deeplang.doperables.spark.wrappers.models.NaiveBayesModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common._
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.choice.Choice
import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.wrappers.spark.{ChoiceParamWrapper, DoubleParamWrapper}

class NaiveBayes
  extends SparkEstimatorWrapper[
    SparkNaiveBayesModel,
    SparkNaiveBayes,
    NaiveBayesModel]
  with ProbabilisticClassifierParams
  with HasLabelColumnParam {

  val smoothing = new DoubleParamWrapper[ml.param.Params { val smoothing: ml.param.DoubleParam }](
    name = "smoothing",
    description = Some("The smoothing parameter."),
    sparkParamGetter = _.smoothing,
    validator = RangeValidator(begin = 0.0, end = Double.MaxValue))
  setDefault(smoothing, 1.0)

  val modelType =
    new ChoiceParamWrapper[ml.param.Params { val modelType: ml.param.Param[String] }, ModelType](
      name = "modelType",
      description = Some("The model type."),
      sparkParamGetter = _.modelType)
  setDefault(modelType, Multinomial())


  override val params: Array[Param[_]] = Array(
    smoothing,
    modelType,
    labelColumn,
    featuresColumn,
    probabilityColumn,
    rawPredictionColumn,
    predictionColumn)
}

object NaiveBayes {

  sealed abstract class ModelType(override val name: String) extends Choice {

    override val params: Array[Param[_]] = Array()

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[Multinomial],
      classOf[Bernoulli]
    )
  }

  case class Multinomial() extends ModelType("multinomial")

  case class Bernoulli() extends ModelType("bernoulli")
}
