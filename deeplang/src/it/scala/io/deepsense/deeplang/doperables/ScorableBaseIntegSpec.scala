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

import scala.util.Success

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.{ColumnsDoNotExistException, WrongColumnTypeException}
import io.deepsense.deeplang.{DeeplangIntegTestSupport, ExecutionContext, PrebuiltTypedColumns}

abstract class ScorableBaseIntegSpec extends DeeplangIntegTestSupport with PrebuiltTypedColumns {

  import PrebuiltTypedColumns.ExtendedColumnType._
  import PrebuiltTypedColumns._

  val scorableName: String

  def createScorableInstance(features: String*): Scorable
  def acceptedFeatureTypes: Seq[ExtendedColumnType]

  def unacceptableFeatureTypes: Seq[ExtendedColumnType]

  override protected val targetColumns = null
  override protected val featureColumns = buildColumns(featureName)

  val targetColumnName: String = "target column"

  scorableName should {
    "throw" when {
      ExtendedColumnType.values.filter(unacceptableFeatureTypes.contains) foreach { columnType =>
        s"feature column is of unacceptable type $columnType" in {
          // Accepted feature as an addition to the DF
          val dataFrame = makeDataFrameOfFeatures(columnType, acceptedFeatureTypes.head)

          a[WrongColumnTypeException] shouldBe thrownBy {
            createScorableInstance(featureName(columnType)).score(
              mock[ExecutionContext])(binaryValuedNumeric.toString)(dataFrame)
          }
        }
      }

      "feature column does not exist" in {
        val dataFrame = makeDataFrameOfFeatures(binaryValuedNumeric)

        a[ColumnsDoNotExistException] shouldBe thrownBy {
          // Existing feature as an addition to the selection
          createScorableInstance("non-existent", featureName(binaryValuedNumeric)).score(
            mock[ExecutionContext])(binaryValuedNumeric.toString)(dataFrame)
        }
      }
    }

    "make predictions" when {
      ExtendedColumnType.values.filter(acceptedFeatureTypes.contains) foreach { columnType =>
        s"feature column is of acceptable type $columnType" in {
          val dataFrame = makeDataFrameOfFeatures(columnType)

          val dataFrameWithPredictions =
            createScorableInstance(featureName(columnType)).score(
              executionContext)(targetColumnName)(dataFrame)

          validatePredictions(columnType, dataFrameWithPredictions)
        }
      }
    }
  }

  def validatePredictions(
      columnType: ExtendedColumnType, dataFrameWithPredictions: DataFrame): Unit = {

    dataFrameWithPredictions
      .selectDoubleRDD(targetColumnName, f => Success())
      .collect()
      .toSeq
  }
}
