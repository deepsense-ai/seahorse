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

import org.apache.spark.mllib.regression._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.mockito.Matchers._
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalactic.EqualityPolicy.Spread
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.doperations.exceptions.{ColumnDoesNotExistException, ColumnsDoNotExistException, WrongColumnTypeException}
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, NameColumnSelection, NameSingleColumnSelection}
import io.deepsense.deeplang.{DeeplangIntegTestSupport, ExecutionContext}

abstract class UntrainedRegressionIntegSpec[T <: GeneralizedLinearModel]
  extends DeeplangIntegTestSupport
  with BeforeAndAfter {

  protected def regressionName: String
  protected val testDataDir: String
  protected def modelType: Class[T]
  protected def constructUntrainedModel: Trainable
  protected val mockUntrainedModel: GeneralizedLinearAlgorithm[T]
  protected val featuresValues: Seq[Spread[Double]]
  protected def validateResult(mockTrainedModel: T, result: Scorable): Registration

  regressionName should {

    val inputRows: Seq[Row] = Seq(
      Row(-2.0, "x", -2.22, 1000.0),
      Row(-2.0, "y", -22.2, 2000.0),
      Row(-2.0, "z", -2222.0, 6000.0))

    val inputSchema: StructType = StructType(Seq(
      StructField("column1", DoubleType),
      StructField("column2", StringType),
      StructField("column3", DoubleType),
      StructField("column0", DoubleType))
    )

    lazy val inputDataFrame = createDataFrame(inputRows, inputSchema)

    "create model trained on given dataframe" in {
      val fsClient = executionContext.fsClient
      val mockContext: ExecutionContext = mock[ExecutionContext]
      when(mockContext.fsClient).thenReturn(fsClient)
      when(mockContext.uniqueFsFileName(isA(classOf[String]))).thenReturn(testDataDir)

      val mockTrainedModel = Mockito.mock(modelType)

      when(mockUntrainedModel.run(any[RDD[LabeledPoint]]())).thenAnswer(
        new Answer[GeneralizedLinearModel] {
          override def answer(invocationOnMock: InvocationOnMock): GeneralizedLinearModel = {
            val receivedRDD = invocationOnMock.getArgumentAt(0, classOf[RDD[LabeledPoint]])
            val collected = receivedRDD.collect()
            val allLabels = collected.map(_.label)
            allLabels shouldBe Seq(-2.22, -22.2, -2222.0)
            val allFeatures = collected.map(_.features)
            allFeatures(0)(0) shouldBe featuresValues(0)
            allFeatures(0)(1) shouldBe featuresValues(1)
            allFeatures(1)(0) shouldBe featuresValues(2)
            allFeatures(1)(1) shouldBe featuresValues(3)
            allFeatures(2)(0) shouldBe featuresValues(4)
            allFeatures(2)(1) shouldBe featuresValues(5)

            mockTrainedModel
          }
        })

      val regression = constructUntrainedModel
      val parameters = Trainable.Parameters(
        featureColumns = Some(MultipleColumnSelection(
          Vector(NameColumnSelection(Set("column0", "column1"))))),
        targetColumn = Some(NameSingleColumnSelection("column3")))

      val result = regression.train(mockContext)(parameters)(inputDataFrame)
      validateResult(mockTrainedModel, result)

      fsClient.fileExists(testDataDir) shouldBe true
    }

    "throw an exception" when {
      "non-existing column was selected as target" in {
        intercept[ColumnDoesNotExistException] {
          val regression = constructUntrainedModel
          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column0", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("not exists")))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
      }
      "non-existing columns was selected as features" in {
        intercept[ColumnsDoNotExistException] {
          val regression = constructUntrainedModel
          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("not exists", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("column3")))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
      }
      "not all selected features were Double" in {
        intercept[WrongColumnTypeException] {
          val regression = constructUntrainedModel
          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column2", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("column3")))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
      }
      "selected target was not Double" in {
        intercept[WrongColumnTypeException] {
          val regression = constructUntrainedModel
          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column0", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("column2")))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
      }
    }
  }

  after {
    fileSystemClient.delete(testsDir)
  }

  before {
    fileSystemClient.delete(testsDir)
    createDir(testsDir)
  }
}
