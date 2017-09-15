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

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame

case class TrainClustering()
  extends UnsupervisedTrainer
    [Clustering with UnsupervisedTrainable, Clustering with Scorable]
  with UnsupervisedTrainableParameters
  with OldOperation {

  override val id: DOperation.Id = "754944c9-4c6b-4c70-8a93-ef0baa940c01"
  override val name = "Train Clustering"

  @transient
  override lazy val tTagTI_0: ru.TypeTag[Clustering with UnsupervisedTrainable] =
    ru.typeTag[Clustering with UnsupervisedTrainable]
  @transient
  override lazy val tTagTI_1: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[Clustering with Scorable] =
    ru.typeTag[Clustering with Scorable]
  @transient
  override lazy val tTagTO_1: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}
