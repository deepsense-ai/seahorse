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

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.doperations.exceptions.{ColumnsDoNotExistException, WrongColumnTypeException}
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, NameColumnSelection, NameSingleColumnSelection}
import io.deepsense.deeplang.{DeeplangIntegTestSupport, ExecutionContext, PrebuiltTypedColumns}

abstract class UnsupervisedTrainableBaseIntegSpec(
    trainableName: String)
  extends DeeplangIntegTestSupport
  with PrebuiltTypedColumns {

  import PrebuiltTypedColumns.ExtendedColumnType._
  import PrebuiltTypedColumns._

  def createTrainableInstance: UnsupervisedTrainable

  def acceptedFeatureTypes: Seq[ExtendedColumnType]
  def unacceptableFeatureTypes: Seq[ExtendedColumnType]

  override protected val featureColumns = buildColumns(featureName)

  trainableName should {
    "throw" when {

      ExtendedColumnType.values.filter(unacceptableFeatureTypes.contains) foreach { columnType =>
        s"feature column is of unacceptable type $columnType" in {
          val trainableParameters =
            makeTrainableParameters(binaryValuedNumeric, Set(columnType))

          // Accepted feature as an addition to the DF
          val dataFrame = makeDataFrameOfFeatures(columnType, acceptedFeatureTypes.head)

          a[WrongColumnTypeException] shouldBe thrownBy {
            createTrainableInstance.train(mock[ExecutionContext])(trainableParameters)(dataFrame)
          }
        }
      }

      "feature column does not exist" in {
        val trainableParameters = UnsupervisedTrainableParameters(
          MultipleColumnSelection(
            // Existing feature as an addition to the selection
            Vector(NameColumnSelection(Set("non-existent", featureName(nonBinaryValuedNumeric))))),
          predictionName(nonBinaryValuedNumeric))

        val dataFrame = makeDataFrameOfFeatures(nonBinaryValuedNumeric)

        a[ColumnsDoNotExistException] shouldBe thrownBy {
          createTrainableInstance.train(mock[ExecutionContext])(trainableParameters)(dataFrame)
        }
      }
    }

    "train a scorable model" when {

      ExtendedColumnType.values.filter(acceptedFeatureTypes.contains) foreach { columnType =>
        s"feature column is of acceptable type $columnType and prediction is numeric" in {
          val trainableParameters =
            makeTrainableParameters(binaryValuedNumeric, Set(columnType))

          val dataFrame = makeDataFrameOfFeatures(columnType)

          val scorable =
            createTrainableInstance.train(mock[ExecutionContext])(trainableParameters)(dataFrame)

          verifyUnsupervisedScorable(scorable, Seq(featureName(columnType)))
        }
      }
    }
  }

  protected def makeTrainableParameters(
      prediction: ExtendedColumnType,
      features: Set[ExtendedColumnType]): UnsupervisedTrainableParameters = {
    UnsupervisedTrainableParameters(
      MultipleColumnSelection(
        Vector(NameColumnSelection(features.map(featureName)))),
      predictionName(prediction))
  }

  def verifyUnsupervisedScorable(
      scorable: Scorable,
      features: Seq[String]): Unit = {

    scorable.isInstanceOf[Scorable] shouldBe true
    scorable.featureColumns shouldBe features
  }

}
