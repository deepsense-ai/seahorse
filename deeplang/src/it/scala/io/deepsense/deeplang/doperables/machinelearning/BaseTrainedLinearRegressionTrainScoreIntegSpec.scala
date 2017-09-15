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

import org.scalatest.BeforeAndAfter

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.doperables.Scorable
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.file.File
import io.deepsense.deeplang.doperations.FileToDataFrame.CSV
import io.deepsense.deeplang.doperations._
import io.deepsense.deeplang.doperations.inout.ReadFile
import io.deepsense.deeplang.{DOperable, DOperation, DeeplangIntegTestSupport}

abstract class BaseTrainedLinearRegressionTrainScoreIntegSpec
  [CreateRegressionType <: DOperation,
    UntrainedRegressionType <: DOperable,
    TrainedRegressionType <: DOperable with Scorable]
  extends DeeplangIntegTestSupport
  with Logging
  with BeforeAndAfter {

  val fileName = testsDir + "/almost_linear_function.csv"

  def regressionName: String

  def createRegressionOperation: CreateRegressionType

  private def deleteDataFile(): Unit =
    executionContext.fsClient.delete(fileName)

  override def beforeAll(): Unit = {
    super.beforeAll()
    deleteDataFile()
    createDir(testsDir)
    executionContext.fsClient.copyLocalFile(
      getClass.getResource("/csv/almost_linear_function.csv").getPath,
      fileName)
  }

  before {
    createDir("target/tests/model")
  }

  override def afterAll(): Unit = {
    super.afterAll()
    deleteDataFile()
  }

  regressionName should {
    "give satisfactory results" in {
      logger.debug("Loading file...")
      val file = loadFile()
      logger.debug("Converting to DataFrame and splitting...")
      val (trainingData, scoreData) = split(fileToDataFrame(file))
      logger.debug("Training regression...")
      val trained = trainRegression(trainingData)
      logger.debug("Scoring...")
      val scoredDataFrame = scoreDataFrame(trained, scoreData)
      logger.debug("After score:")
      forAll(scoredDataFrame.sparkDataFrame.collect()) { row =>
        val realValue = row.getDouble(3)
        val scoredValue = row.getDouble(4)
        scoredValue shouldBe realValue +- 0.5
      }
    }
  }

  def split(dataFrame: DataFrame): (DataFrame, DataFrame) = {
    val dataFrames =
      Split(0.5, 1).execute(executionContext)(Vector(dataFrame)).map(_.asInstanceOf[DataFrame])
    (dataFrames(0), dataFrames(1))
  }

  def loadFile(): File = {
    ReadFile(fileName, ReadFile.unixSeparatorValue)
      .execute(executionContext)(Vector.empty)
      .head
      .asInstanceOf[File]
  }

  def fileToDataFrame(file: File): DataFrame = {
    FileToDataFrame(CSV, ",", namesIncluded = true)
      .execute(executionContext)(Vector(file))
      .head
      .asInstanceOf[DataFrame]
  }

  def trainRegression(dataFrame: DataFrame): Scorable = {
    val untrained =
      createRegressionOperation
        .execute(executionContext)(Vector.empty)
        .head
        .asInstanceOf[UntrainedRegressionType]
    TrainRegressor(Set("x"), "f_x")
      .execute(executionContext)(Vector(untrained, dataFrame))
      .head
      .asInstanceOf[TrainedRegressionType]
  }

  def scoreDataFrame(trained: Scorable, dataFrame: DataFrame): DataFrame = {
    ScoreRegressor("prediction").execute(executionContext)(Vector(trained, dataFrame))
      .head
      .asInstanceOf[DataFrame]
  }
}
