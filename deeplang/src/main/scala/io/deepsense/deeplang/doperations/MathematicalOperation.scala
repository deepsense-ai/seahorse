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

import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.doperables.Transformation
import io.deepsense.deeplang.doperations.transformations.MathematicalTransformation
import io.deepsense.deeplang.parameters.{AcceptAllRegexValidator, ParametersSchema, StringParameter}
import io.deepsense.deeplang.{DOperation0To1, ExecutionContext}

case class MathematicalOperation() extends DOperation0To1[Transformation] {

  override val name : String = "Mathematical Operation"

  override val id : Id = "ecb9bc36-5f7c-4a62-aa18-8db6e2d73251"

  // TODO: DS-635 This operation will fail if user provide column name with '.'

  override protected def _execute(context: ExecutionContext)(): Transformation = {
    val formula = formulaParam.value.get
    MathematicalTransformation(Some(formula))
  }

  val formulaParam = StringParameter(
    "Mathematical formula to be placed in a column named with AS directive. " +
    "For example, \"(myColumn * myColumn)\" AS myColumnSquared",
    None, required = true, validator = new AcceptAllRegexValidator)

  override val parameters = ParametersSchema("formula" -> formulaParam)
  @transient
  override lazy val tTagTO_0: ru.TypeTag[Transformation] = ru.typeTag[Transformation]
}

object MathematicalOperation {
  def apply(formula: String): MathematicalOperation = {

    val operation = new MathematicalOperation
    operation.formulaParam.value = Some(formula)
    operation
  }
}
