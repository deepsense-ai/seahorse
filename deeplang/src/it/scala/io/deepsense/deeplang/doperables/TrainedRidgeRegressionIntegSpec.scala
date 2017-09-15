/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.feature.StandardScalerModel
import org.apache.spark.mllib.linalg.{Vector => SparkVector, Vectors}
import org.apache.spark.mllib.regression.RidgeRegressionModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.{ColumnsDoNotExistException, WrongColumnTypeException}

class TrainedRidgeRegressionIntegSpec extends DeeplangIntegTestSupport {

  "TrainedRidgeRegression" should {
    "produce dataframe with column with '_prediction' suffix" in {
      val (scoredDataFrame, expectedDataFrame) = createScoredAndExpectedDataFrames(
        inputColumnNames = Seq("column1", "column2", "column3", "column4"),
        featureColumnNames = Seq("column1", "column4"),
        targetColumnName = "some_target",
        expectedPredictionColumnSuffix = "_prediction")

      assertDataFramesEqual(scoredDataFrame, expectedDataFrame)
    }
  }

  it should {
    "produce dataframe with column with '_prediction_2' suffix" when {
      "'_prediction' and '_prediction_1' are occupied" in {
        val (scoredDataFrame, expectedDataFrame) = createScoredAndExpectedDataFrames(
          inputColumnNames = Seq(
            "column1", "column2", "some_target_prediction", "some_target_prediction_1"),
          featureColumnNames = Seq("column1", "some_target_prediction_1"),
          targetColumnName = "some_target",
          expectedPredictionColumnSuffix = "_prediction_2")

        assertDataFramesEqual(scoredDataFrame, expectedDataFrame)
      }
    }
  }

  it should {
    "throw an exception" when {
      "expected feature columns were not found in DataFrame" in {
        intercept[ColumnsDoNotExistException] {
          createScoredAndExpectedDataFrames(
            inputColumnNames = Seq("column1", "column2", "column3", "column4"),
            featureColumnNames = Seq("column1", "non-existing"),
            targetColumnName = "some_target",
            expectedPredictionColumnSuffix = "_prediction_2")
        }
      }
      "feature columns were not Double" in {
        intercept[WrongColumnTypeException] {
          createScoredAndExpectedDataFrames(
            inputColumnNames = Seq("column1", "column2", "column3", "column4"),
            featureColumnNames = Seq("column1", "column3"),
            targetColumnName = "some_target",
            expectedPredictionColumnSuffix = "_prediction_2")
        }
      }
    }
  }

  private def createScoredAndExpectedDataFrames(
      inputColumnNames: Seq[String],
      featureColumnNames: Seq[String],
      targetColumnName: String,
      expectedPredictionColumnSuffix: String): (DataFrame, DataFrame) = {

    val inputRowsSeq: Seq[Row] = Seq(
      Row(1.5, 2.5, "a", 3.5),
      Row(1.6, 2.6, "b", 3.6),
      Row(1.7, 2.7, "c", 3.7))

    val inputSchema: StructType = StructType(List(
      StructField(inputColumnNames(0), DoubleType),
      StructField(inputColumnNames(1), DoubleType),
      StructField(inputColumnNames(2), StringType),
      StructField(inputColumnNames(3), DoubleType)))

    val inputDataframe = createDataFrame(inputRowsSeq, inputSchema)

    val inputVectors = Seq(
      Vectors.dense(1.5, 3.5),
      Vectors.dense(1.6, 3.6),
      Vectors.dense(1.7, 3.7))

    val scaledVectors = Seq(
      Vectors.dense(-0.1, 0.2),
      Vectors.dense(0.0, 0.4),
      Vectors.dense(0.1, -0.2))

    val resultDoubles = Seq(4.5, 4.6, 4.7)

    val mockScaler = createScalerMock(
        expectedInput = inputVectors, output = scaledVectors)
    val mockModel = createRegressionModelMock(
      expectedInput = scaledVectors, output = resultDoubles)

    val expectedPredictionColumnName = targetColumnName + expectedPredictionColumnSuffix
    val expectedOutputDataFrame = createExpectedOutputDataFrame(
      inputSchema, inputRowsSeq, resultDoubles, expectedPredictionColumnName)

    val regression = TrainedRidgeRegression(
      model = Some(mockModel),
      featureColumns = Some(featureColumnNames),
      targetColumn = Some(targetColumnName),
      scaler = Some(mockScaler))

    val resultDataframe = regression.score(executionContext)(())(inputDataframe)

    (resultDataframe, expectedOutputDataFrame)
  }

  private def createRegressionModelMock(
      expectedInput: Seq[SparkVector],
      output: Seq[Double]): RidgeRegressionModel = {

    val mockModel = mock[RidgeRegressionModel]
    when(mockModel.predict(any[RDD[SparkVector]]())).thenAnswer(new Answer[RDD[Double]] {
      override def answer(invocationOnMock: InvocationOnMock): RDD[Double] = {
        val receivedRDD = invocationOnMock.getArgumentAt(0, classOf[RDD[SparkVector]])
        receivedRDD.collect() shouldBe expectedInput
        sparkContext.parallelize(output)
      }
    })
    mockModel
  }

  private def createScalerMock(
      expectedInput: Seq[SparkVector],
      output: Seq[SparkVector]): StandardScalerModel = {

    val mockScaler = mock[StandardScalerModel]
    when(mockScaler.transform(any[RDD[SparkVector]]())).thenAnswer(new Answer[RDD[SparkVector]] {
      override def answer(invocationOnMock: InvocationOnMock): RDD[SparkVector] = {
        val receivedRDD = invocationOnMock.getArgumentAt(0, classOf[RDD[SparkVector]])
        receivedRDD.collect() shouldBe expectedInput
        sparkContext.parallelize(output)
      }
    })
    mockScaler
  }

  private def createExpectedOutputDataFrame(
      inputSchema: StructType,
      inputRows: Seq[Row],
      expectedRegressionResult: Seq[Double],
      expectedPredictionColumnName: String): DataFrame = {

    val expectedOutputRowsSeq = inputRows.zip(expectedRegressionResult).map {
      case (row, double) => Row.fromSeq(row.toSeq :+ double)
    }

    val expectedOutputSchema: StructType = StructType(
      inputSchema.fields :+ StructField(expectedPredictionColumnName, DoubleType))
    createDataFrame(expectedOutputRowsSeq, expectedOutputSchema)
  }

}
