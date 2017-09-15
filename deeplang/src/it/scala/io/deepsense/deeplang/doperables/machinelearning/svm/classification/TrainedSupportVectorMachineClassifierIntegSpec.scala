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

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType
import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType.ExtendedColumnType
import io.deepsense.deeplang.doperables.{Scorable, ScorableBaseIntegSpec}

class TrainedSupportVectorMachineClassifierIntegSpec
  extends { override val scorableName: String = "TrainedSupportVectorMachineClassifier" }
  with ScorableBaseIntegSpec {

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
      new SVMModel(Vectors.dense(2.3), 10.0), features, "bogus target")

  "TrainedSupportVectorMachineClassifier" should {
    "create a report" in {
      val svmClassifier = TrainedSupportVectorMachineClassifier(
        new SVMModel(Vectors.dense(2.3, 4.4, 1.0), 10.0), Seq("X", "Y", "Z"), "TargetColumn")

      val reportTables = svmClassifier.report(mock[ExecutionContext]).content.tables

      val weights = reportTables("Computed weights")
      weights.values shouldBe List(
        List(Some("X"), Some("2.3")),
        List(Some("Y"), Some("4.4")),
        List(Some("Z"), Some("1.0")))

      val intercept = reportTables("Intercept")
      intercept.values shouldBe List(
        List(Some("10.0"))
      )

      val target = reportTables("Target column")
      target.values shouldBe List(
        List(Some("TargetColumn"))
      )
    }
  }
}
