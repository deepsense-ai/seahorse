/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.{DKnowledge, DOperable, DOperation, ExecutionContext}
import ai.deepsense.deeplang.DOperation._
import ai.deepsense.deeplang.doperations.exceptions.UnknownOperationExecutionException
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}

import scala.reflect.runtime.{universe => ru}


class UnknownOperation extends DOperation {

  override val inArity = 0
  override val outArity = 0

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector()

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector()


  override val id: Id = "08752b37-3f90-4b8d-8555-e911e2de5662"
  override val name: String = "Unknown Operation"
  override val description: String = "Operation that could not be recognized by Seahorse"

  override val specificParams: Array[ai.deepsense.deeplang.params.Param[_]] = Array()

  override def executeUntyped(arguments: Vector[DOperable])(context: ExecutionContext): Vector[DOperable] = {
    throw new UnknownOperationExecutionException
  }

  override def inferKnowledgeUntyped(knowledge: Vector[DKnowledge[DOperable]])(
      context: InferContext): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    throw new UnknownOperationExecutionException
  }
}
