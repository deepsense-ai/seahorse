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
import org.apache.spark.sql.types._
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

  protected def constructUntrainedModel(
    untrainedModelMock: GeneralizedLinearAlgorithm[T]): Trainable

  protected def mockUntrainedModel(): GeneralizedLinearAlgorithm[T]

  protected val featuresValues: Seq[Spread[Double]]

  protected def validateResult(
    mockTrainedModel: T, result: Scorable, targetColumnName: String): Registration

  val inputRows: Seq[Row] = Seq(
    Row(-2.0, "x", -2.22, true, "A", 1000.0),
    Row(-2.0, "y", -22.2, true, "B", 2000.0),
    Row(-2.0, "z", -2222.0, false, "A", 6000.0))

  val inputSchema: StructType = StructType(Seq(
    StructField("column1", DoubleType),
    StructField("column2", StringType),
    StructField("column3", DoubleType),
    StructField("column4", BooleanType),
    StructField("column5", StringType),
    StructField("column0", DoubleType)))

  lazy val inputDataFrame = createDataFrame(inputRows, inputSchema)

  def withMockedModel(expectedLabels: Seq[Any])(
      testCode: (Trainable, T, ExecutionContext) => Any): Unit = {

    val mockContext: ExecutionContext = mock[ExecutionContext]
    when(mockContext.fsClient).thenReturn(executionContext.fsClient)
    when(mockContext.uniqueFsFileName(isA(classOf[String]))).thenReturn(testDataDir)

    val trainedModelMock = Mockito.mock(modelType)
    val untrainedModelMock = mockUntrainedModel()

    when(untrainedModelMock.run(any[RDD[LabeledPoint]]())).thenAnswer(
      new Answer[GeneralizedLinearModel] {
        override def answer(invocationOnMock: InvocationOnMock): GeneralizedLinearModel = {
          val invocation = invocationOnMock
            .getArgumentAt(0, classOf[RDD[LabeledPoint]])
            .collect()

          invocation.map(_.label) shouldBe expectedLabels

          val allFeatures = invocation.map(_.features)

          allFeatures(0)(0) shouldBe featuresValues.head
          allFeatures(0)(1) shouldBe featuresValues(1)
          allFeatures(1)(0) shouldBe featuresValues(2)
          allFeatures(1)(1) shouldBe featuresValues(3)
          allFeatures(2)(0) shouldBe featuresValues(4)
          allFeatures(2)(1) shouldBe featuresValues(5)

          trainedModelMock
        }
      })

    testCode(
      constructUntrainedModel(untrainedModelMock),
      trainedModelMock,
      mockContext)

    executionContext.fsClient.fileExists(testDataDir) shouldBe true
  }

  regressionName should {
    "create model trained on given DataFrame" in
      withMockedModel(Seq(-2.22, -22.2, -2222.0)) {
        (untrainedModel, trainedModel, context) =>

          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column0", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("column3")))

          val result = untrainedModel.train(context)(parameters)(inputDataFrame)
          validateResult(trainedModel, result, "column3")
      }

    "throw an exception" when {
      "non-existing column was selected as target" in {
        a[ColumnDoesNotExistException] shouldBe thrownBy {
          val regression = constructUntrainedModel(mockUntrainedModel())
          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column0", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("not exists")))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
      }
      "non-existing columns was selected as features" in {
        a[ColumnsDoNotExistException] shouldBe thrownBy {
          val regression = constructUntrainedModel(mockUntrainedModel())
          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("not exists", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("column3")))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
      }
      "not all selected features were Double" in {
        a[WrongColumnTypeException] shouldBe thrownBy {
          val regression = constructUntrainedModel(mockUntrainedModel())
          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column2", "column1"))))),
            targetColumn = Some(NameSingleColumnSelection("column3")))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
      }
      "selected target was not Double" in {
        a[WrongColumnTypeException] shouldBe thrownBy {
          val regression = constructUntrainedModel(mockUntrainedModel())
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
