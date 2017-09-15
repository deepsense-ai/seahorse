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

import org.apache.spark.ml.clustering.{KMeans => SparkKMeans, KMeansModel => SparkKMeansModel}

import ai.deepsense.deeplang.doperables.SparkEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators.KMeans.{KMeansInitMode, ParallelInitMode}
import ai.deepsense.deeplang.doperables.spark.wrappers.models.KMeansModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common._
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.choice.Choice
import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.wrappers.spark.{ChoiceParamWrapper, IntParamWrapper}

class KMeans
  extends SparkEstimatorWrapper[SparkKMeansModel, SparkKMeans, KMeansModel]
  with PredictorParams
  with HasNumberOfClustersParam
  with HasMaxIterationsParam
  with HasSeedParam
  with HasTolerance {

  override lazy val maxIterationsDefault = 20.0
  override lazy val toleranceDefault = 1E-4

  val initMode = new ChoiceParamWrapper[SparkKMeans, KMeansInitMode](
    "init mode",
    Some("The initialization algorithm mode. This can be either \"random\" to choose random " +
      "points as initial cluster centers, or \"k-means||\" to use a parallel variant of k-means++."),
    _.initMode)
  setDefault(initMode, ParallelInitMode())

  val initSteps = new IntParamWrapper[SparkKMeans](
    "init steps",
    Some("The number of steps for the k-means|| initialization mode. It will be ignored when other " +
      "initialization modes are chosen."),
    _.initSteps,
    validator = RangeValidator(begin = 1.0, end = Int.MaxValue, step = Some(1.0)))
  setDefault(initSteps, 5.0)

  override val params: Array[Param[_]] = Array(
    k,
    maxIterations,
    seed,
    tolerance,
    initMode,
    initSteps,
    featuresColumn,
    predictionColumn)
}

object KMeans {

  sealed trait KMeansInitMode extends Choice {
    override val params: Array[Param[_]] = Array()
    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[RandomInitMode],
      classOf[ParallelInitMode]
    )
  }

  case class RandomInitMode() extends KMeansInitMode {
    override val name = "random"
  }

  case class ParallelInitMode() extends KMeansInitMode {
    override val name = "k-means||"
  }
}
