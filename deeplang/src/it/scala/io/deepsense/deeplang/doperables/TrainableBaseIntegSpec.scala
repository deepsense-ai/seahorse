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

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperations.exceptions.{ColumnDoesNotExistException, ColumnsDoNotExistException, WrongColumnTypeException}
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, NameColumnSelection, NameSingleColumnSelection}

abstract class TrainableBaseIntegSpec(val trainableName: String)
  extends DeeplangIntegTestSupport with PrebuiltTypedColumns {

  import PrebuiltTypedColumns.ExtendedColumnType._
  import PrebuiltTypedColumns._

  def createTrainableInstance: Trainable

  def acceptedTargetTypes: Seq[ExtendedColumnType]
  def acceptedFeatureTypes: Seq[ExtendedColumnType]

  def unacceptableTargetTypes: Seq[ExtendedColumnType]
  def unacceptableFeatureTypes: Seq[ExtendedColumnType]

  override protected val targetColumns = buildColumns(targetName)
  override protected val featureColumns = buildColumns(featureName)

  trainableName should {
    "throw" when {

      ExtendedColumnType.values.filter(unacceptableFeatureTypes.contains) foreach { columnType =>
        s"feature column is of unacceptable type $columnType" in {
          val trainableParameters =
            makeTrainableParameters(binaryValuedNumeric, Set(columnType))

          // Accepted feature as an addition to the DF
          val dataFrame = makeDataFrame(binaryValuedNumeric, columnType, acceptedFeatureTypes.head)

          a[WrongColumnTypeException] shouldBe thrownBy {
            createTrainableInstance.train(
              mock[ExecutionContext])(trainableParameters)(dataFrame)
          }
        }
      }

      ExtendedColumnType.values.filter(unacceptableTargetTypes.contains) foreach { columnType =>
        s"target column is of unacceptable type $columnType" in {
          val trainableParameters =
            makeTrainableParameters(columnType, Set(nonBinaryValuedNumeric))

          val dataFrame = makeDataFrame(columnType, nonBinaryValuedNumeric)

          a[WrongColumnTypeException] shouldBe thrownBy {
            createTrainableInstance.train(
              mock[ExecutionContext])(trainableParameters)(dataFrame)
          }
        }
      }

      "feature column does not exist" in {
        val trainableParameters = TrainableParameters(
          MultipleColumnSelection(
            // Existing feature as an addition to the selection
            Vector(NameColumnSelection(Set("non-existent", featureName(nonBinaryValuedNumeric))))),
          NameSingleColumnSelection(targetName(nonBinaryValuedNumeric)))

        val dataFrame = makeDataFrame(nonBinaryValuedNumeric, nonBinaryValuedNumeric)

        a[ColumnsDoNotExistException] shouldBe thrownBy {
          createTrainableInstance.train(
            mock[ExecutionContext])(trainableParameters)(dataFrame)
        }
      }

      "target column does not exist" in {
        val trainableParameters = TrainableParameters(
          MultipleColumnSelection(
            Vector(NameColumnSelection(Set(targetName(nonBinaryValuedNumeric))))),
          NameSingleColumnSelection("non-existent"))

        val dataFrame = makeDataFrame(nonBinaryValuedNumeric, nonBinaryValuedNumeric)

        a[ColumnDoesNotExistException] shouldBe thrownBy {
          createTrainableInstance.train(
            mock[ExecutionContext])(trainableParameters)(dataFrame)
        }
      }
    }

    "train a scorable model" when {

      ExtendedColumnType.values.filter(acceptedFeatureTypes.contains) foreach { columnType =>
        s"feature column is of acceptable type $columnType and target is numeric" in {
          val trainableParameters =
            makeTrainableParameters(binaryValuedNumeric, Set(columnType))

          val dataFrame = makeDataFrame(binaryValuedNumeric, columnType)

          val scorable = createTrainableInstance.train(
            mock[ExecutionContext])(trainableParameters)(dataFrame)

          verifyScorable(scorable,
            targetName(binaryValuedNumeric), Seq(featureName(columnType)))
        }
      }

      ExtendedColumnType.values.filter(acceptedTargetTypes.contains) foreach { columnType =>
        s"target column is of acceptable type $columnType and feature is numeric" in {
          val trainableParameters =
            makeTrainableParameters(columnType, Set(nonBinaryValuedNumeric))

          val dataFrame = makeDataFrame(columnType, nonBinaryValuedNumeric)

          val scorable = createTrainableInstance.train(
            mock[ExecutionContext])(trainableParameters)(dataFrame)

          verifyScorable(scorable,
            targetName(columnType), Seq(featureName(nonBinaryValuedNumeric)))
        }
      }
    }
  }

  protected def makeTrainableParameters(
      target: ExtendedColumnType, features: Set[ExtendedColumnType]): TrainableParameters = {
    TrainableParameters(
      MultipleColumnSelection(
        Vector(NameColumnSelection(features.map(featureName)))),
      NameSingleColumnSelection(targetName(target))
    )
  }

  // Theoretically, not every Scorable has VectorScoring trait, but for now, that's the case.
  // If it changes, this might need little work.
  def verifyScorable(scorable: Scorable, target: String, features: Seq[String]): Unit = {
    scorable.isInstanceOf[VectorScoring] shouldBe true

    val trained = scorable.asInstanceOf[VectorScoring]
    trained.featureColumns shouldBe features
    trained.targetColumn shouldBe target
  }
}
