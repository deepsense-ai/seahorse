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

package io.deepsense.deeplang.doperations.custom

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}
import io.deepsense.deeplang.params.Param

case class Sink() extends DOperation1To1[DataFrame, DataFrame] {

  override val id: Id = Sink.id
  override val name: String = "Sink"
  override val description: String = "Custom transformer sink"
  override val params: Array[Param[_]] = declareParams()

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame =
    dataFrame

  override protected def _inferKnowledge(context: InferContext)(
    inputKnowledge: DKnowledge[DataFrame]): (DKnowledge[DataFrame], InferenceWarnings) = {
    (inputKnowledge, InferenceWarnings.empty)
  }
}

object Sink {
  val id: Id = "e652238f-7415-4da6-95c6-ee33808561b2"
}
