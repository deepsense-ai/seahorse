/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations

import scala.reflect.runtime.universe.TypeTag

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.PythonTransformer

case class PythonTransformation() extends TransformerAsOperation[PythonTransformer] with OperationDocumentation {

  override val id: Id = "a721fe2a-5d7f-44b3-a1e7-aade16252ead"
  override val name: String = "Python Transformation"
  override val description: String = "Creates a custom Python transformation"

  override lazy val tTagTO_1: TypeTag[PythonTransformer] = typeTag

  override val since: Version = Version(1, 0, 0)
}

object PythonTransformation {
  val InputPortNumber: Int = 0
  val OutputPortNumber: Int = 0
}
