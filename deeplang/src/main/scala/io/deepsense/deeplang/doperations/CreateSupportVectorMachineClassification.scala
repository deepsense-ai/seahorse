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
import io.deepsense.deeplang.doperables.machinelearning.svm.classification.UntrainedSupportVectorMachineClassifier
import io.deepsense.deeplang.parameters.RegularizationType.RegularizationType
import io.deepsense.deeplang.{DOperation0To1, ExecutionContext}


case class CreateSupportVectorMachineClassification()
  extends DOperation0To1[UntrainedSupportVectorMachineClassifier]
  with SupportVectorMachineInterfaceParameters {

  override protected def _execute(
      context: ExecutionContext)(): UntrainedSupportVectorMachineClassifier = {
    UntrainedSupportVectorMachineClassifier(modelParameters)
  }

  @transient
  override lazy val tTagTO_0: ru.TypeTag[UntrainedSupportVectorMachineClassifier] =
    ru.typeTag[UntrainedSupportVectorMachineClassifier]

  override val id: Id = "7a17e3d8-f991-4a80-82c5-d06b2d065879"
  override val name: String = "Create SVM Classification"
}

object CreateSupportVectorMachineClassification extends CreateSupportVectorMachineClassification {
  def apply(
      regularization: RegularizationType,
      numIterations: Int,
      regParam: Double,
      miniBatchFraction: Double): CreateSupportVectorMachineClassification = {

    val create = CreateSupportVectorMachineClassification()
    create.setParameters(regularization, numIterations, regParam, miniBatchFraction)
    create
  }
}
