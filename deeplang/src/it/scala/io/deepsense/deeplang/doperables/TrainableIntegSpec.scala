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

import scala.language.reflectiveCalls

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._

import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType._
import io.deepsense.deeplang.PrebuiltTypedColumns._
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.ColumnTypesPredicates._
import io.deepsense.deeplang.doperables.Trainable.TrainingParameters
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, NameColumnSelection, NameSingleColumnSelection}


class TrainableIntegSpec extends DeeplangIntegTestSupport with PrebuiltTypedColumns {

  override protected val targetColumns = buildColumns(targetName)
  override protected val featureColumns = buildColumns(featureName)

  "Trainable" should {
    "receive properly selected LabeledPoints" in {
      val trainableParameters = TrainableParameters(
        featureColumns = MultipleColumnSelection(
          Vector(NameColumnSelection(Set(featureName(nonBinaryValuedNumeric))))),
        targetColumn = NameSingleColumnSelection(targetName(nonBinaryValuedNumeric)))

      val dataFrame = makeDataFrame(nonBinaryValuedNumeric, nonBinaryValuedNumeric)

      val targetColumnValues = targetColumns(nonBinaryValuedNumeric).values
      val featureColumnValues = featureColumns(nonBinaryValuedNumeric).values
      val expectedLabeledPoints = targetColumnValues zip featureColumnValues map {
        case (target: Double, feature: Double) => LabeledPoint(target, Vectors.dense(feature))
      }

      val trainable = new Trainable {
        override protected def runTraining: RunTraining = runTrainingWithLabeledPoints

        val actualTrainingMock = mock[TrainScorable]
        val argumentCaptor = ArgumentCaptor.forClass(classOf[TrainingParameters])
        override protected def actualTraining: TrainScorable = actualTrainingMock

        override protected def labelPredicate: Predicate = ColumnTypesPredicates.isNumeric
        override protected def featurePredicate: Predicate = ColumnTypesPredicates.isNumeric

        override def report(executionContext: ExecutionContext): Report = mock[Report]
        override def save(executionContext: ExecutionContext)(path: String): Unit = ()
        override def toInferrable: DOperable = mock[DOperable]

        override protected def actualInference(
          context: InferContext)(
          parameters: TrainableParameters)(
          dataFrame: DKnowledge[DataFrame]): (DKnowledge[Scorable], InferenceWarnings) =
          mock[(DKnowledge[Scorable], InferenceWarnings)]
      }

      trainable.train(
        mock[ExecutionContext])(trainableParameters)(dataFrame)

      verify(trainable.actualTrainingMock).apply(trainable.argumentCaptor.capture())
      trainable.argumentCaptor.getValue.labeledPoints.collect().toSeq shouldBe expectedLabeledPoints
    }
  }
}
