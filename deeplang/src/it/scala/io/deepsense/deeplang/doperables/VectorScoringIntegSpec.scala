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
import scala.util.Success

import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.mockito.AdditionalAnswers._
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._

import io.deepsense.deeplang.PrebuiltTypedColumns.ExtendedColumnType._
import io.deepsense.deeplang.PrebuiltTypedColumns._
import io.deepsense.deeplang.doperables.ColumnTypesPredicates._
import io.deepsense.deeplang.{DOperable, DeeplangIntegTestSupport, ExecutionContext, PrebuiltTypedColumns}

class VectorScoringIntegSpec extends DeeplangIntegTestSupport with PrebuiltTypedColumns {

  val targetColumnName: String = "target column"

  override protected val targetColumns = null
  override protected val featureColumns = buildColumns(featureName)

  "Scorable with VectorScoring" should {
    "call predict with appropriate features" in {

      val dataFrame = makeDataFrameOfFeatures(binaryValuedNumeric)

      val expectedInvocation = featureColumns(binaryValuedNumeric).values map {
        case (d: Double) => Vectors.dense(d)
      }

      val scorable = new Scorable with VectorScoring {
        override val featureColumns: Seq[String] = Seq(featureName(binaryValuedNumeric))
        override val targetColumn: String = targetColumnName

        val transformFeaturesMock = mock[RDD[Vector] => RDD[Vector]]
        val transformFeaturesArg = ArgumentCaptor.forClass(classOf[RDD[Vector]])
        override def transformFeatures(v: RDD[Vector]) = transformFeaturesMock(v)

        val predictMock = mock[RDD[Vector] => RDD[Double]]
        val predictArg = ArgumentCaptor.forClass(classOf[RDD[Vector]])
        override def predict(features: RDD[Vector]) = predictMock(features)

        override protected def featurePredicate: Predicate = f => Success()
        override def report(executionContext: ExecutionContext): Report = mock[Report]
        override def save(executionContext: ExecutionContext)(path: String): Unit = ()
        override def toInferrable: DOperable = mock[DOperable]
      }

      doAnswer(returnsFirstArg())
        .when(scorable.transformFeaturesMock)
        .apply(scorable.transformFeaturesArg.capture())

      scorable.score(executionContext)(targetColumnName)(dataFrame)

      scorable.transformFeaturesArg.getValue.collect().toSeq shouldBe expectedInvocation

      verify(scorable.predictMock).apply(scorable.predictArg.capture())
      scorable.predictArg.getValue.collect().toSeq shouldBe expectedInvocation
    }
  }
}
