/**
 * Copyright 2016, deepsense.ai
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

import scala.reflect.runtime.universe.TypeTag

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.documentation.OperationDocumentation
import io.deepsense.deeplang.doperables.REvaluator

class CreateREvaluator
  extends EvaluatorAsFactory[REvaluator] with OperationDocumentation  {

  override val id: Id = "1c626513-f266-4458-8499-29cbad95bb8c"
  override val name: String = "R Evaluator"
  override val description: String = "Creates an R Evaluator"

  override lazy val tTagTO_0: TypeTag[REvaluator] = typeTag

  override val since: Version = Version(1, 3, 0)
}
