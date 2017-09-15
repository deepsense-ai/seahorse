/**
 * Copyright 2015, CodiLime Inc.
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

import org.apache.spark.mllib.linalg.{Vector => SparkVector, Vectors}
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoriesMapping, MappingMetadataConverter}
import io.deepsense.deeplang.doperations.exceptions.{ColumnsDoNotExistException, WrongColumnTypeException}

class TrainedRandomForestClassificationIntegSpec extends DeeplangIntegTestSupport {

  private val inputVectors = Seq(
    Vectors.dense(1.5, 3.5, 0.0),
    Vectors.dense(1.6, 3.6, 1.0),
    Vectors.dense(1.7, 3.7, 2.0),
    Vectors.dense(0.0, 0.0, 0.0))

  private val targetColumnName = "some_target"

  "TrainedRandomForestClassification" should {
    "produce dataframe with target column" in {
      val (scoredDataFrame, expectedDataFrame) = createScoredAndExpectedDataFrames(
        inputColumnNames = Seq("column1", "column2", "column3", "column4", "column5"),
        featureColumnNames = Seq("column1", "column4", "column5"),
        targetColumnName = targetColumnName)

      assertDataFramesEqual(scoredDataFrame, expectedDataFrame)
    }

    "throw an exception" when {
      "expected feature columns were not found in DataFrame" in {
        intercept[ColumnsDoNotExistException] {
          createScoredAndExpectedDataFrames(
            inputColumnNames = Seq("column1", "column2", "column3", "column4", "column5"),
            featureColumnNames = Seq("column1", "non-existing"),
            targetColumnName = targetColumnName)
        }
        ()
      }

      "feature columns were neither numeric nor categorical" in {
        intercept[WrongColumnTypeException] {
          createScoredAndExpectedDataFrames(
            inputColumnNames = Seq("column1", "column2", "column3", "column4", "column5"),
            featureColumnNames = Seq("column1", "column3"),
            targetColumnName = targetColumnName)
        }
        ()
      }
    }
  }

  private def createScoredAndExpectedDataFrames(
      inputColumnNames: Seq[String],
      featureColumnNames: Seq[String],
      targetColumnName: String): (DataFrame, DataFrame) = {

    val inputRowsSeq: Seq[Row] = Seq(
      Row(1.5, 2.5, "a", 3.5, 0),
      Row(1.6, 2.6, "b", 3.6, 1),
      Row(1.7, 2.7, "c", 3.7, 2),
      Row(null, null, null, null, null))
    val inputSchema: StructType = StructType(List(
      StructField(inputColumnNames(0), DoubleType),
      StructField(inputColumnNames(1), DoubleType),
      StructField(inputColumnNames(2), StringType),
      StructField(inputColumnNames(3), DoubleType),
      StructField(inputColumnNames(4), IntegerType,
        metadata = MappingMetadataConverter.mappingToMetadata(CategoriesMapping(Seq("A", "B", "C")))
      )
    ))
    val inputDataframe = createDataFrame(inputRowsSeq, inputSchema)
    val resultDoubles = Seq(4.5, 4.6, 4.7, 4.8)

    val expectedOutputDataFrame = createExpectedOutputDataFrame(
      inputSchema, inputRowsSeq, resultDoubles, targetColumnName)

    val Classification: Scorable =
      createMockTrainedClassification(featureColumnNames, targetColumnName, resultDoubles)

    val resultDataframe = Classification.score(executionContext)(targetColumnName)(inputDataframe)

    (resultDataframe, expectedOutputDataFrame)
  }

  private def createMockTrainedClassification(
      featureColumnNames: Seq[String],
      targetColumnName: String,
      resultDoubles: Seq[Double]): Scorable = {

    val mockModel = createClassificationModelMock(
      expectedInput = inputVectors,
      output = resultDoubles)

    TrainedRandomForestClassification(mockModel, featureColumnNames, targetColumnName)
  }

  private def createClassificationModelMock(
      expectedInput: Seq[SparkVector],
      output: Seq[Double]): RandomForestModel = {

    val mockModel = mock[RandomForestModel]

    when(mockModel.predict(any[RDD[SparkVector]]())).thenAnswer(new Answer[RDD[Double]] {
      override def answer(invocationOnMock: InvocationOnMock): RDD[Double] = {
        val receivedRDD = invocationOnMock.getArgumentAt(0, classOf[RDD[SparkVector]])
        receivedRDD.collect() shouldBe expectedInput
        sparkContext.parallelize(output)
      }
    })
    mockModel
  }

  private def createExpectedOutputDataFrame(
      inputSchema: StructType,
      inputRows: Seq[Row],
      expectedClassificationResult: Seq[Double],
      expectedPredictionColumnName: String): DataFrame = {

    val expectedOutputRowsSeq = inputRows.zip(expectedClassificationResult).map {
      case (row, double) => Row.fromSeq(row.toSeq :+ double)
    }

    val expectedOutputSchema: StructType = StructType(
      inputSchema.fields :+ StructField(expectedPredictionColumnName, DoubleType))
    createDataFrame(expectedOutputRowsSeq, expectedOutputSchema)
  }
}
