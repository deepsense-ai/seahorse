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

package io.deepsense.deeplang.doperables.machinelearning

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression._
import org.apache.spark.rdd.RDD
import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalactic.EqualityPolicy.Spread

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{Trainable, TrainableBaseIntegSpec, TrainableParameters}
import io.deepsense.deeplang.{DeeplangIntegTestSupport, ExecutionContext}

trait GeneralizedLinearModelTrainableBaseIntegSpec[T <: GeneralizedLinearModel]
  extends DeeplangIntegTestSupport {

  self: TrainableBaseIntegSpec =>

  protected def createTrainableInstanceWithModel(
    untrainedModel: GeneralizedLinearAlgorithm[T]): Trainable

  protected def mockUntrainedModel(): GeneralizedLinearAlgorithm[T]

  protected val expectedFeaturesInvocation: Seq[Spread[Double]] =
    Seq(0.0 +- 0.00001, 1.0 +- 0.00001, 2.0 +- 0.00001, 3.0 +- 0.00001)

  trainableName should {
    "train the Spark model using provided features and labels" in {
      val parameters = mock[TrainableParameters]
      when(parameters.columnNames(any())).thenReturn((null, null))

      val dataFrame = mock[DataFrame]
      when(dataFrame.selectAsSparkLabeledPointRDD(any(), any(), any(), any()))
        .thenReturn(
            sparkContext.parallelize(Seq(0, 1, 2, 3)).map {
              i => LabeledPoint(77, Vectors.dense(i))
            }
        )

      val untrainedModelMock = mockUntrainedModel()
      val trainable = createTrainableInstanceWithModel(untrainedModelMock)
      trainable.train(mock[ExecutionContext])(parameters)(dataFrame)

      val argumentCaptor = ArgumentCaptor.forClass(classOf[RDD[LabeledPoint]])

      verify(untrainedModelMock).run(argumentCaptor.capture())
      val invocation = argumentCaptor.getValue
      invocation.collect().map(_.label).toSeq shouldBe Seq(77, 77, 77, 77)
      invocation.collect().map(_.features(0)).zipWithIndex foreach {
        case (f, i) => f shouldBe expectedFeaturesInvocation(i)
      }
    }
  }
}
