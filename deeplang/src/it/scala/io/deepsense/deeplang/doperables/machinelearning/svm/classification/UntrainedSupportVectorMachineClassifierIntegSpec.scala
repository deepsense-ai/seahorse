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

package io.deepsense.deeplang.doperables.machinelearning.svm.classification

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.machinelearning.svm.SupportVectorMachineParameters
import io.deepsense.deeplang.parameters.RegularizationType
import io.deepsense.reportlib.model.{Table, ReportContent}

class UntrainedSupportVectorMachineClassifierIntegSpec
  extends { override val trainableName: String = "UntrainedSupportVectorMachineClassifier" }
  with TrainableBaseIntegSpec {

  import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType
  import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType.ExtendedColumnType

  override type ConcreteTrainable = UntrainedSupportVectorMachineClassifier

  override def acceptedFeatureTypes: Seq[ExtendedColumnType] = Seq(
    ExtendedColumnType.binaryValuedNumeric,
    ExtendedColumnType.nonBinaryValuedNumeric)

  override def unacceptableFeatureTypes: Seq[ExtendedColumnType] = Seq(
    ExtendedColumnType.categorical1,
    ExtendedColumnType.categorical2,
    ExtendedColumnType.categoricalMany,
    ExtendedColumnType.boolean,
    ExtendedColumnType.string,
    ExtendedColumnType.timestamp)

  override def acceptedTargetTypes: Seq[ExtendedColumnType] = Seq(
    ExtendedColumnType.binaryValuedNumeric,
    ExtendedColumnType.boolean,
    ExtendedColumnType.categorical1,
    ExtendedColumnType.categorical2)

  override def unacceptableTargetTypes: Seq[ExtendedColumnType] = Seq(
    // this is omitted because it's a runtime problem, not schema problem
    //ExtendedColumnType.nonBinaryValuedNumeric
    ExtendedColumnType.string,
    ExtendedColumnType.timestamp,
    ExtendedColumnType.categoricalMany)

  override def createTrainableInstance: Trainable =
    UntrainedSupportVectorMachineClassifier(
      SupportVectorMachineParameters(RegularizationType.NONE, 10, 0.1, 1.0))

  override def verifyScorable(scorable: Scorable, target: String, features: Seq[String]): Unit = {
    scorable.isInstanceOf[TrainedSupportVectorMachineClassifier] shouldBe true

    val trainedSVM = scorable.asInstanceOf[TrainedSupportVectorMachineClassifier]
    trainedSVM.featureColumns shouldBe features
    trainedSVM.targetColumn shouldBe target
  }

  "UntrainedSupportVectorMachineClassifier" should {
    "create a report" in {
      val svmUntrainedClassifier = UntrainedSupportVectorMachineClassifier(
        SupportVectorMachineParameters(RegularizationType.NONE, 10, 0.1, 1.0))

      svmUntrainedClassifier.report(mock[ExecutionContext]) shouldBe Report(ReportContent(
        "Report for Untrained SVM Classification",
        tables = Map(
          "Parameters" -> Table(
            "Parameters", "",
            Some(List(
              "Regularization type",
              "Regularization parameter",
              "Number of iterations",
              "Mini batch fraction")),
            List(ColumnType.string, ColumnType.numeric, ColumnType.numeric, ColumnType.numeric),
            None,
            List(
              List(Some("None"), Some("0.1"), Some("10"), Some("1"))
            )
          )
        )
      ))
    }
  }
}
