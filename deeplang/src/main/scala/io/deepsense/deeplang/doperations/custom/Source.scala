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
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.{DOperation0To1, ExecutionContext}

case class Source() extends DOperation0To1[DataFrame] {

  override val id: Id = Source.id
  override val name: String = "Source"
  override val description: String = "Custom transformer source"
  override val params: Array[Param[_]] = declareParams()

  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]

  override protected def _execute(context: ExecutionContext)(): DataFrame =
    throw new IllegalStateException("should not be executed")
}

object Source {
  val id: Id = "f94b04d7-ec34-42f7-8100-93fe235c89f8"
}
