/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.sdk.example

import scala.reflect.runtime.{universe => ru}

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.params.{Param, StringParam}
import io.deepsense.deeplang.refl.Register

@Register
class IdentityOperation extends DOperation1To1[DataFrame, DataFrame] {
  override protected def execute(t0: DataFrame)(context: ExecutionContext): DataFrame = t0

  def comment = StringParam("comment", "Comment describing what the DataFrame represents.")
  setDefault(comment, "")
  def setComment(value: String): this.type = set(comment, value)
  def getComment: String = $(comment)

  // This should be different for different operations and should be in valid format.
  // UUID can be generated here: https://www.uuidgenerator.net/
  override val id: Id = "9de532e0-2e69-427a-a7ee-600c9b80bf04"
  override val name: String = "Identity"
  override val description: String =
    "Returns the same data frame that was passed to it. Useful for educational purposes only."

  override def params: Array[Param[_]] = Array(comment)

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]

  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}
