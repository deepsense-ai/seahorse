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

import org.apache.spark.mllib.classification.SVMModel
import org.apache.spark.mllib.linalg.Vectors

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType
import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType.ExtendedColumnType
import io.deepsense.deeplang.doperables.machinelearning.svm.SupportVectorMachineParameters
import io.deepsense.deeplang.doperables.{Report, Scorable, ScorableBaseIntegSpec}
import io.deepsense.deeplang.parameters.RegularizationType
import io.deepsense.reportlib.model.{Table, ReportContent}

class TrainedSupportVectorMachineClassifierIntegSpec
  extends { override val scorableName: String = "TrainedSupportVectorMachineClassifier" }
  with ScorableBaseIntegSpec {

  private val params = SupportVectorMachineParameters(RegularizationType.L1, 3, 0.1, 0.2)

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

  override def createScorableInstance(features: String*): Scorable =
    TrainedSupportVectorMachineClassifier(
      params,
      new SVMModel(Vectors.dense(2.3), 10.0), features, "bogus target")

  "TrainedSupportVectorMachineClassifier" should {
    "create a report" in {
      val svmClassifier = TrainedSupportVectorMachineClassifier(
        params,
        new SVMModel(Vectors.dense(2.3, 4.4, 1.0), 10.0), Seq("X", "Y", "Z"), "TargetColumn")

      svmClassifier.report(mock[ExecutionContext]) shouldBe Report(ReportContent(
        "Report for Trained SVM Classification",
        tables = Map(
          "Model weights" -> Table(
            "Model weights", "",
            Some(List("Column", "Weight")),
            List(ColumnType.string, ColumnType.numeric),
            None,
            values = List(
              List(Some("X"), Some("2.3")),
              List(Some("Y"), Some("4.4")),
              List(Some("Z"), Some("1")))
          ),
          "Intercept" -> Table(
            "Intercept", "",
            None,
            List(ColumnType.numeric),
            None,
            List(List(Some("10.0")))
          ),
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
              List(Some("L1"), Some("0.1"), Some("3"), Some("0.2"))
            )
          ),
          "Target column" -> Table(
            "Target column", "",
            Some(List("Target column")),
            List(ColumnType.string),
            None,
            List(List(Some("TargetColumn")))
          ),
          "Feature columns" -> Table(
            "Feature columns", "",
            Some(List("Feature columns")),
            List(ColumnType.string),
            None,
            List(List(Some("X")), List(Some("Y")), List(Some("Z")))
          )
        )
      ))
    }
  }
}
