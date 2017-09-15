/**
 * Copyright 2015, CodiLime Inc.
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
import io.deepsense.deeplang.doperables.{Classifier, Scorable, Trainable}

case class TrainClassifier() extends Trainer[Classifier with Trainable, Classifier with Scorable] {
  override val id: DOperation.Id = "892cf942-fe24-11e4-a322-1697f925ec7b"
  override val name = "Train Classifier"
  @transient
  override lazy val tTagTI_0: ru.TypeTag[Classifier with Trainable] = ru.typeTag[Classifier with Trainable]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[Classifier with Scorable] = ru.typeTag[Classifier with Scorable]
  @transient
  override lazy val tTagTI_1: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}
