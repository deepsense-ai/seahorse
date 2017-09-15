/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LabeledPoint, RidgeRegressionModel, RidgeRegressionWithSGD}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperations.exceptions.{WrongColumnTypeException, ColumnsDoNotExistException, ColumnDoesNotExistException}
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, NameColumnSelection, NameSingleColumnSelection}

class UntrainedRidgeRegressionIntegSpec extends DeeplangIntegTestSupport {

  "UntrainedRidgeRegression" should {

    val inputRows: Seq[Row] = Seq(
      Row(-1.11, "x", -2.22, 3.33),
      Row(-11.1, "y", -22.2, 33.3),
      Row(-1111.0, "z", -2222.0, 3333.0))

    val inputSchema: StructType = StructType(Seq(
      StructField("column1", DoubleType),
      StructField("column2", StringType),
      StructField("column3", DoubleType),
      StructField("column0", DoubleType))
    )

    lazy val inputDataFrame = createDataFrame(inputRows, inputSchema)

    val mockUntrainedModel = mock[RidgeRegressionWithSGD]

    "create model trained on given dataframe" in {

      val mockTrainedModel = mock[RidgeRegressionModel]

      when(mockUntrainedModel.run(any[RDD[LabeledPoint]]())).thenAnswer(
        new Answer[RidgeRegressionModel] {
          override def answer(invocationOnMock: InvocationOnMock) = {
            val receivedRDD = invocationOnMock.getArgumentAt(0, classOf[RDD[LabeledPoint]])
            receivedRDD.collect() shouldBe Seq(
              LabeledPoint(-2.22, Vectors.dense(-1.11, 3.33)),
              LabeledPoint(-22.2, Vectors.dense(-11.1, 33.3)),
              LabeledPoint(-2222, Vectors.dense(-1111, 3333))
            )
            mockTrainedModel
          }
        })

      val regression = UntrainedRidgeRegression(Some(mockUntrainedModel))
      val parameters = Trainable.Parameters(
        featureColumns = MultipleColumnSelection(
          Vector(NameColumnSelection(Set("column0", "column1")))),
        targetColumn = NameSingleColumnSelection("column3"))

      val result = regression.train(executionContext)(parameters)(inputDataFrame)
      val castedResult = result.asInstanceOf[TrainedRidgeRegression]
      castedResult.model shouldBe Some(mockTrainedModel)
      castedResult.featureColumns shouldBe Some(Seq("column1", "column0"))
      castedResult.targetColumn shouldBe Some("column3")
    }

    "throw an exception" when {
      "non-existing column was selected as target" in {
        intercept[ColumnDoesNotExistException] {
          val regression = UntrainedRidgeRegression(Some(mockUntrainedModel))
          val parameters = Trainable.Parameters(
            featureColumns = MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column0", "column1")))),
            targetColumn = NameSingleColumnSelection("not exists"))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
      }
      "non-existing columns was selected as features" in {
        intercept[ColumnsDoNotExistException] {
          val regression = UntrainedRidgeRegression(Some(mockUntrainedModel))
          val parameters = Trainable.Parameters(
            featureColumns = MultipleColumnSelection(
              Vector(NameColumnSelection(Set("not exists", "column1")))),
            targetColumn = NameSingleColumnSelection("column3"))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
      }
      "not all selected features were Double" in {
        intercept[WrongColumnTypeException] {
          val regression = UntrainedRidgeRegression(Some(mockUntrainedModel))
          val parameters = Trainable.Parameters(
            featureColumns = MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column2", "column1")))),
            targetColumn = NameSingleColumnSelection("column3"))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
      }
      "selected target was not Double" in {
        intercept[WrongColumnTypeException] {
          val regression = UntrainedRidgeRegression(Some(mockUntrainedModel))
          val parameters = Trainable.Parameters(
            featureColumns = MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column0", "column1")))),
            targetColumn = NameSingleColumnSelection("column2"))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
      }
    }
  }
}
