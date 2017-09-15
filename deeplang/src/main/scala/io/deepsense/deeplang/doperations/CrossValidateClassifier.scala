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
import io.deepsense.deeplang.doperables._

case class CrossValidateClassifier() extends CrossValidate[Classifier] {
  override def reportName: String = ClassificationReporter.CvReportName

  @transient
  override lazy val tTagTI_0: ru.TypeTag[Classifier with Trainable] =
    ru.typeTag[Classifier with Trainable]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[Classifier with Scorable] =
    ru.typeTag[Classifier with Scorable]

  override val id: DOperation.Id = "0a3f1185-1284-4609-a363-fcd692dccb25"

  override val name: String = "Cross-validate Classifier"
}
