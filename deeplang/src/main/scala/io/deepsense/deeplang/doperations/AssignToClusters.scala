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
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{Clustering, Scorable}

case class AssignToClusters() extends Scorer[Clustering with Scorable] with OldOperation {
  override val id: DOperation.Id = "bce36c12-df3b-44d0-9a67-8ae213cc9d10"
  override val name = "Assign to Clusters"
  @transient
  override lazy val tTagTI_0: ru.TypeTag[Clustering with Scorable] =
    ru.typeTag[Clustering with Scorable]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTI_1: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

object AssignToClusters {
  def apply(predictionColumnName: String): AssignToClusters = {
    val clustering = new AssignToClusters
    clustering.predictionColumnParam.value = predictionColumnName
    clustering
  }
}
