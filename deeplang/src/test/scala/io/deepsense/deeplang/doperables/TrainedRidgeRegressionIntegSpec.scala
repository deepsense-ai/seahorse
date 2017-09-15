/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.linalg.{Vector => SparkVector, Vectors}
import org.apache.spark.mllib.regression.LinearRegressionModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.mockito.Mockito.when
import org.mockito.Matchers.any
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import io.deepsense.deeplang.{ExecutionContext, SparkIntegTestSupport}

class TrainedRidgeRegressionIntegSpec extends SparkIntegTestSupport {

  "TrainedRidgeRegression" should "produce dataframe with column with '_prediction' suffix" in {
    testRegression(
      inputColumnNames = Seq("column1", "column2", "column3", "column4"),
      targetColumnName = "some_target",
      expectedPredictionColumnSuffix = "_prediction")
  }

  it should "produce dataframe with column with '_prediction_2' suffix " +
    "if '_prediction' and '_prediction_1' are occupied" in {

    testRegression(
      inputColumnNames = Seq(
        "column1", "some_target_prediction_1", "column3", "some_target_prediction"),
      targetColumnName = "some_target",
      expectedPredictionColumnSuffix = "_prediction_2")
  }

  private def testRegression(
      inputColumnNames: Seq[String],
      targetColumnName: String,
      expectedPredictionColumnSuffix: String): Unit = {

    val mockModel = mock[LinearRegressionModel]

    val inputVectorsSeq = Seq(
      Vectors.dense(1.5, 3.5),
      Vectors.dense(1.6, 3.6),
      Vectors.dense(1.7, 3.7))

    val resultDoublesSeq = Seq(4.5, 4.6, 4.7)
    val resultDoublesRDD = sparkContext.parallelize(resultDoublesSeq)

    when(mockModel.predict(any[RDD[SparkVector]]())).thenAnswer(new Answer[RDD[Double]] {
      override def answer(invocationOnMock: InvocationOnMock): RDD[Double] = {
        val receivedRDD = invocationOnMock.getArgumentAt(0, classOf[RDD[SparkVector]])
        receivedRDD.collect() shouldBe inputVectorsSeq
        resultDoublesRDD
      }
    })

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

    val expectedOutputRowsSeq = inputRowsSeq.zip(resultDoublesSeq).map { case (row, double) =>
      Row.fromSeq(row.toSeq :+ double) }

    val expectedPredictionColumnName = targetColumnName + expectedPredictionColumnSuffix
    val expectedOutputSchema: StructType = StructType(
      inputSchema.fields :+ StructField(expectedPredictionColumnName, DoubleType))
    val expectedOutputDataframe = createDataFrame(expectedOutputRowsSeq, expectedOutputSchema)

    val regression = TrainedRidgeRegression(
      model = Some(mockModel),
      featureColumns = Some(Seq(inputColumnNames(0), inputColumnNames(3))),
      targetColumn = Some(targetColumnName))

    val resultDataframe = regression.score(executionContext)(())(inputDataframe)

    assertDataFramesEqual(expectedOutputDataframe, resultDataframe)
  }
}
