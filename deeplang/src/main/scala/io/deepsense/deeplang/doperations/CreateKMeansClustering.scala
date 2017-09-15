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

import scala.collection.immutable.ListMap
import scala.reflect.runtime.{universe => ru}

import org.apache.spark.mllib.clustering.KMeans

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.machinelearning.kmeans.{KMeansParameters, UntrainedKMeansClustering}
import io.deepsense.deeplang.parameters.{ChoiceParameter, NumericParameter, ParametersSchema, RangeValidator}
import io.deepsense.deeplang.{DOperation0To1, _}

case class CreateKMeansClustering()
    extends DOperation0To1[UntrainedKMeansClustering]
    with KMeansParams
    with OldOperation {

  override val name = "Create k-means Clustering"

  override val id: Id = "1550a57b-86d5-4f49-96eb-329a5bdbaaa7"

  override protected def _execute(context: ExecutionContext)(): UntrainedKMeansClustering = {
    UntrainedKMeansClustering(modelParameters)
  }

  @transient
  override lazy val tTagTO_0: ru.TypeTag[UntrainedKMeansClustering] =
    ru.typeTag[UntrainedKMeansClustering]
}

object CreateKMeansClustering {
  def apply(
      numClusters: Int,
      maxIterations: Int,
      initializationMode: String,
      initializationSteps: Option[Int],
      seed: Int,
      runs: Int = 1,
      epsilon: Double = 1e-4): CreateKMeansClustering = {

    val createClustering = CreateKMeansClustering()
    createClustering.numClustersParameter.value = numClusters
    createClustering.maxIterationsParameter.value = maxIterations
    createClustering.initializationModeParameter.value = initializationMode
    createClustering.initializationStepsParameter.value = initializationSteps.getOrElse(1)
    createClustering.seedParameter.value = seed
    createClustering.runsParameter.value = runs
    createClustering.epsilonParameter.value = epsilon

    createClustering
  }
}

trait KMeansParams {

  val initializationModeOptions = List(KMeans.K_MEANS_PARALLEL, KMeans.RANDOM)

  val numClustersParameter = NumericParameter(
    description = "Number of clusters",
    default = Some(2.0),
    validator = RangeValidator(begin = 1.0, end = 1000000.0, step = Some(1.0)))

  val maxIterationsParameter = NumericParameter(
    description = "Maximum number of iterations",
    default = Some(100.0),
    validator = RangeValidator(begin = 1.0, end = 1000000.0, step = Some(1.0)))

  val initializationStepsParameter = NumericParameter(
    description = "Number of steps for the k-means|| initialization mode",
    default = Some(1.0),
    validator = RangeValidator(begin = 1.0, end = 1000000.0, step = Some(1.0)))

  val initializationModeParameter = ChoiceParameter(
    description = "Criterion used for information gain calculation",
    default = Some(initializationModeOptions(0)),
    options = ListMap(
      KMeans.K_MEANS_PARALLEL -> ParametersSchema(
        "initialization steps" -> initializationStepsParameter
      ),
      KMeans.RANDOM -> ParametersSchema()
    )
  )

  val seedParameter = NumericParameter(
    description = "The random seed for cluster initialization",
    default = Some(1.0),
    // TODO Fix RangeValidator, because now it can't handle Int.MinValue and Int.MaxValue
    RangeValidator(Int.MinValue / 2, Int.MaxValue / 2, true, true, Some(1.0)),
    _value = None
  )

  val runsParameter = NumericParameter(
    description = "Number of runs of the algorithm to execute in parallel. " +
      "We initialize the algorithm this many times with random starting conditions, " +
      "then return the best clustering found.",
    default = Some(1.0),
    validator = RangeValidator(begin = 1.0, end = 1000000.0, step = Some(1.0)))

  val epsilonParameter = NumericParameter(
    description = "The distance threshold within which we consider centers to have converged",
    default = Some(1e-4),
    validator = RangeValidator(begin = 0.0, end = 1000000.0)
  )

  val parameters = ParametersSchema(
    "number of clusters" -> numClustersParameter,
    "max iterations" -> maxIterationsParameter,
    "initialization mode" -> initializationModeParameter,
    "seed" -> seedParameter,
    "number of runs" -> runsParameter,
    "epsilon" -> epsilonParameter
  )

  def modelParameters: KMeansParameters = {
    val numClusters = numClustersParameter.value
    val maxIterations = maxIterationsParameter.value
    val initializationMode = initializationModeParameter.value
    val seed = seedParameter.value
    val numRuns = runsParameter.value
    val epsilon = epsilonParameter.value
    val initializationSteps = initializationMode match {
      case KMeans.K_MEANS_PARALLEL => Some(initializationStepsParameter.value.toInt)
      case KMeans.RANDOM => None
    }

    KMeansParameters(
      numClusters.toInt,
      maxIterations.toInt,
      initializationMode,
      initializationSteps,
      seed.toInt,
      numRuns.toInt,
      epsilon)
  }
}
