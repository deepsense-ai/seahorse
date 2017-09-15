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

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables._

case class CrossValidateRegressor() extends CrossValidate[Regressor] {
  override def reportName: String = RegressionReporter.CvReportName

  @transient
  override lazy val tTagTI_0: ru.TypeTag[Regressor with Trainable] =
    ru.typeTag[Regressor with Trainable]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[Regressor with Scorable] =
    ru.typeTag[Regressor with Scorable]

  override val id: DOperation.Id = "95ca5225-b8e0-45c7-8ecd-a2c9d4d6861f"

  override val name: String = "Cross-validate Regressor"
}
