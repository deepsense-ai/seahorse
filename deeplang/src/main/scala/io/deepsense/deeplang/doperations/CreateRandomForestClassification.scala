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

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.UntrainedRandomForestClassification

case class CreateRandomForestClassification()
    extends { override val impurityOptions = Seq("gini", "entropy") }
    with DOperation0To1[UntrainedRandomForestClassification]
    with RandomForestParams {

  override val name = "Create Random Forest Classification"

  override val id: Id = "9498dfdc-368f-469b-b7de-c83933251ab3"

  override protected def _execute(
      context: ExecutionContext)(): UntrainedRandomForestClassification = {
    UntrainedRandomForestClassification(modelParameters)
  }

  @transient
  override lazy val tTagTO_0: ru.TypeTag[UntrainedRandomForestClassification] =
    ru.typeTag[UntrainedRandomForestClassification]
}

object CreateRandomForestClassification {

  def apply(
      numTrees: Int,
      featureSubsetStrategy: String,
      impurity: String,
      maxDepth: Int,
      maxBins: Int): CreateRandomForestClassification = {
    val createRandomForest = CreateRandomForestClassification()
    createRandomForest.setParameters(numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)
    createRandomForest
  }
}
