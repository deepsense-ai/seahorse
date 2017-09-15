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

import org.apache.spark.mllib.linalg.{Vector => SparkVector}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito._

import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.{DeeplangIntegTestSupport, ExecutionContext}

trait PredictorModelBaseIntegSpec[PredictionType] extends DeeplangIntegTestSupport {

  self: ScorableBaseIntegSpec =>

  trait PredictorSparkModel {
    def predict(features: RDD[SparkVector]): RDD[PredictionType]
  }

  def mockTrainedModel(): PredictorSparkModel

  def createScorableInstanceWithModel(trainedModelMock: PredictorSparkModel): Scorable

  val featuresValues: Seq[Seq[Double]]
  val predictionValues: Seq[Any]

  scorableName should {
    "construct prediction using provided features" in {

      val dataFrame = mock[DataFrame]
      val features = mock[RDD[SparkVector]]
      doReturn(features).when(dataFrame).selectSparkVectorRDD(any(), any())

      // we're using real DF here, because mocking it (rdd, map, zip) is annoying
      // and the code becomes less readable
      val dataForScoring = sqlContext.createDataFrame(
        sparkContext.parallelize(featuresValues.map(Row.fromSeq)),
        StructType(Seq(StructField("feature", DoubleType))))

      doReturn(dataForScoring).when(dataFrame).sparkDataFrame

      val trainedModelMock = mockTrainedModel()

      // again, real RDD object, for simplicity
      val predictions = sparkContext.parallelize(predictionValues)
      doReturn(predictions).when(trainedModelMock).predict(features)

      val context = mock[ExecutionContext]
      val dataFrameBuilder = mock[DataFrameBuilder]
      doReturn(dataFrameBuilder).when(context).dataFrameBuilder

      // the tested call happens here
      val scorable = createScorableInstanceWithModel(trainedModelMock)
      scorable.score(context)(predictionColumnName)(dataFrame)

      // now, we check what was called with what arguments

      // this is the most important part: prediction is performed on features that were provided
      val predictInvocation = ArgumentCaptor.forClass(classOf[RDD[SparkVector]])
      verify(trainedModelMock).predict(predictInvocation.capture())
      predictInvocation.getValue shouldBe features

      // this is mostly by the way: we check if the produced DF is what we expect
      val buildInvocationData = ArgumentCaptor.forClass(classOf[RDD[Row]])
      val buildInvocationSchema = ArgumentCaptor.forClass(classOf[StructType])

      verify(dataFrameBuilder).buildDataFrame(
        buildInvocationSchema.capture(),
        buildInvocationData.capture())

      val outputData = buildInvocationData.getValue.collect().toSeq.map(_.toSeq)
      val expectedOutputData = (featuresValues zip predictionValues).map {
        case (f: Seq[Any], p) => f :+ p
      }
      outputData shouldBe expectedOutputData

      val expectedSchema = StructType(
        Seq(StructField("feature", DoubleType), StructField(predictionColumnName, DoubleType)))
      buildInvocationSchema.getValue shouldBe expectedSchema
    }
  }
}
