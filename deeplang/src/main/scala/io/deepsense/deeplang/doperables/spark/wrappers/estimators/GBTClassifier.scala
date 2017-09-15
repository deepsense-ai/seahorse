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

package io.deepsense.deeplang.doperables.spark.wrappers.estimators

import scala.reflect.runtime.universe._

import org.apache.spark.ml
import org.apache.spark.ml.classification.{GBTClassificationModel => SparkGBTClassificationModel, GBTClassifier => SparkGBTClassifier}
import org.apache.spark.sql.types.StructType

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.spark.wrappers.models.GBTClassificationModel
import io.deepsense.deeplang.doperables.spark.wrappers.params.GBTParams
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.choice.Choice
import io.deepsense.deeplang.params.wrappers.spark.ChoiceParamWrapper
import io.deepsense.deeplang.utils.WithStringIndexing
import io.deepsense.deeplang.{doperables, ExecutionContext, TypeUtils}

class GBTClassifier()
  extends SimpleSparkEstimatorWrapper[SparkGBTClassificationModel]
  with GBTParams
  with Logging
  with WithStringIndexing[SparkGBTClassificationModel] {

  import GBTClassifier._

  setDefault(maxIterations, 20.0)

  setDefault(stepSize, 0.1)

  val estimator = TypeUtils.instanceOfType(typeTag[SparkGBTClassifier])

  override def sparkEstimator: ml.Estimator[SparkGBTClassificationModel] = estimator

  override private[deeplang] def _fit_infer(
      maybeSchema: Option[StructType]): doperables.Transformer = {
    validateParameters(maybeSchema)
    new GBTClassificationModel()
  }

  private def validateParameters(maybeSchema: Option[StructType]): Unit = {
    maybeSchema.foreach(schema => sparkParamMap(estimator, schema))
  }

  override private[deeplang] def _fit(
      ctx: ExecutionContext,
      dataFrame: DataFrame): doperables.Transformer = {
    val labelColumnName = dataFrame.getColumnName($(labelColumn))
    val predictionColumnName: String = $(predictionColumn)
    val transformer =
      fitWithStringIndexing(ctx, dataFrame, this, labelColumnName, predictionColumnName)
    new GBTClassificationModel(transformer)
  }

  val impurity = new ChoiceParamWrapper[SparkGBTClassifier, Impurity](
    name = "impurity",
    description = "Criterion used for information gain calculation.",
    sparkParamGetter = _.impurity)
  setDefault(impurity, Gini())

  val lossType = new ChoiceParamWrapper[SparkGBTClassifier, LossType](
    name = "loss function",
    description = "Loss function which GBT tries to minimize.",
    sparkParamGetter = _.lossType)
  setDefault(lossType, Logistic())

  override val params: Array[Param[_]] = declareParams(
    featuresColumn,
    impurity,
    labelColumn,
    lossType,
    maxBins,
    maxDepth,
    maxIterations,
    minInfoGain,
    minInstancesPerNode,
    predictionColumn,
    seed,
    stepSize,
    subsamplingRate)
}

object GBTClassifier {

  sealed abstract class Impurity(override val name: String) extends Choice {

    override val params: Array[Param[_]] = declareParams()

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[Entropy],
      classOf[Gini]
    )
  }
  case class Entropy() extends Impurity("entropy")
  case class Gini() extends Impurity("gini")

  sealed abstract class LossType(override val name: String) extends Choice {

    override val params: Array[Param[_]] = declareParams()

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[Logistic]
    )
  }
  case class Logistic() extends LossType("logistic")

}
