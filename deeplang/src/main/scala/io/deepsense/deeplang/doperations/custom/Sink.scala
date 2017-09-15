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

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang._
import io.deepsense.deeplang.documentation.OperationDocumentation
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.Param

case class Sink() extends DOperation1To1[DataFrame, DataFrame] with OperationDocumentation {

  override val id: Id = Sink.id
  override val name: String = "Sink"
  override val description: String = "Custom transformer sink"

  override val since: Version = Version(1, 0, 0)

  override val params: Array[Param[_]] = Array()

  override protected def execute(dataFrame: DataFrame)(context: ExecutionContext): DataFrame =
    dataFrame

  override protected def inferKnowledge(
      inputKnowledge: DKnowledge[DataFrame])(context: InferContext): (DKnowledge[DataFrame], InferenceWarnings) = {
    (inputKnowledge, InferenceWarnings.empty)
  }
}

object Sink {
  val id: Id = "e652238f-7415-4da6-95c6-ee33808561b2"
}
