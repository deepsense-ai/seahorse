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

package io.deepsense.models.json.workflow.examples

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.doperations._
import io.deepsense.deeplang.parameters._
import io.deepsense.graph.{Edge, Node}

object LocationAttractiveness extends WorkflowCreator {

  val testFilePath: String = "file:///home/ubuntu/workflows/input/LocationAttractiveness.csv"
  val resultFilePath: String = "file:///home/ubuntu/workflows/results/LocationAttractiveness.csv"

  val readDataFrame: ReadDataFrame = ReadDataFrame(
    testFilePath,
    (ReadDataFrame.LineSeparator.UNIX, None),
    ",",
    csvNamesIncluded = true,
    csvShouldConvertToBoolean = true,
    categoricalColumns = None)

  // min(dist to SF, dist to LA)
  val distanceOperation: CreateMathematicalTransformation = {
    val sf = Seq(37.774929, -122.419416)
    val la = Seq(34.052234, -118.243685)

    def distTo(p: Seq[Double]): String =
      s"SQRT(POW(latitude - ${p(0)}, 2.0) + POW(longitude - ${p(1)}, 2.0))"

    CreateMathematicalTransformation(s"MINIMUM(${distTo(sf)}, ${distTo(la)})", "dist_SF_LA")
  }
  val applyDistanceOperation = ApplyTransformation()

  val logHighestPrice1: DOperation =
    CreateMathematicalTransformation("LN(highest_price + 1.0)", "log_highest_price1")
  val logLowestPrice1: DOperation =
    CreateMathematicalTransformation("LN(lowest_price + 1.0)", "log_lowest_price1")
  val logReviewCount1: DOperation =
    CreateMathematicalTransformation("LN(review_count + 1.0)", "log_review_count1")

  val applyLogHighestPrice1 = ApplyTransformation()
  val applyLogLowestPrice1 = ApplyTransformation()
  val applyLogReviewCount1 = ApplyTransformation()

  val importantColumns = Seq(
    "rating",
    "review_count",
    "stars",
    "highest_price",
    "lowest_price",
    "deposit",
    "petsTRUE",
    "petsUnknown",
    "smokingTRUE",
    "smokingUnknown",
    "check_in",
    "room_count",
    "meeting_roomsUnknown",
    "parking_detailsUnknown",
    "fitness_facilitiesTRUE",
    "fitness_facilitiesUnknown",
    "accessibilityUnknown",
    "cribsUnknown",
    "twentyfour_hour_front_deskUnknown",
    "Bed_and_BreakfastsTRUE",
    "BusinessTRUE",
    "MotelTRUE",
    "FamilyTRUE",
    "B_BTRUE",
    "log_highest_price1",
    "log_lowest_price1",
    "log_review_count1",
    "dist_SF_LA")

  val selectImportantFeatures = ProjectColumns(importantColumns)
  val writeDataFrame: WriteDataFrame = WriteDataFrame(
    ",",
    true,
    resultFilePath)

  val convertBooleanToDouble: DOperation = {
    val booleanColumns = Set(
      "petsTRUE",
      "petsUnknown",
      "smokingTRUE",
      "smokingUnknown",
      "meeting_roomsUnknown",
      "parking_detailsUnknown",
      "fitness_facilitiesTRUE",
      "fitness_facilitiesUnknown",
      "accessibilityUnknown",
      "cribsUnknown",
      "twentyfour_hour_front_deskUnknown",
      "Bed_and_BreakfastsTRUE",
      "BusinessTRUE",
      "MotelTRUE",
      "FamilyTRUE",
      "B_BTRUE")
    ConvertType(ColumnType.numeric, names = booleanColumns)
  }

  val split: Split = Split(splitRatio = 0.7, seed = 1)
  val createRidgeRegression: DOperation =
    CreateRidgeRegression(regularization = 0.5, iterationsNumber = 1)

  val targetColumnName = "rating"
  val predictionColumnName = "rating_prediction"
  val trainRegressor = TrainRegressor(importantColumns.toSet, targetColumnName)
  val scoreRegressor = ScoreRegressor("rating_prediction")
  val evaluateRegressor = EvaluateRegression(
    targetColumnName = targetColumnName,
    predictionColumnName = predictionColumnName)

  val readDataFrameNode: Node = node(readDataFrame)
  val distanceOperationNode: Node = node(distanceOperation)
  val applyDistanceOperationNode: Node = node(applyDistanceOperation)
  val logHighestPrice1Node: Node = node(logHighestPrice1)
  val logLowestPrice1Node: Node = node(logLowestPrice1)
  val logReviewCount1Node: Node = node(logReviewCount1)
  val applyLogHighestPrice1Node: Node = node(applyLogHighestPrice1)
  val applyLogLowestPrice1Node: Node = node(applyLogLowestPrice1)
  val applyLogReviewCount1Node: Node = node(applyLogReviewCount1)
  val selectImportantFeaturesNode: Node = node(selectImportantFeatures)
  val writeDataFrameNode: Node = node(writeDataFrame)
  val splitNode: Node = node(split)
  val createRidgeRegressionNode: Node = node(createRidgeRegression)
  val convertBooleanToDoubleNode: Node = node(convertBooleanToDouble)
  val trainRegressorNode: Node = node(trainRegressor)
  val scoreRegressorNode: Node = node(scoreRegressor)
  val evaluateRegressorNode: Node = node(evaluateRegressor)

  override val nodes = Seq(
    readDataFrameNode,
    distanceOperationNode,
    applyDistanceOperationNode,
    logHighestPrice1Node,
    logLowestPrice1Node,
    logReviewCount1Node,
    applyLogHighestPrice1Node,
    applyLogLowestPrice1Node,
    applyLogReviewCount1Node,
    selectImportantFeaturesNode,
    writeDataFrameNode,
    convertBooleanToDoubleNode,
    splitNode,
    createRidgeRegressionNode,
    trainRegressorNode,
    scoreRegressorNode,
    evaluateRegressorNode)

  override val edges = Seq(
    Edge(readDataFrameNode, 0, applyDistanceOperationNode, 1),
    Edge(distanceOperationNode, 0, applyDistanceOperationNode, 0),

    Edge(applyDistanceOperationNode, 0, applyLogHighestPrice1Node, 1),
    Edge(logHighestPrice1Node, 0, applyLogHighestPrice1Node, 0),

    Edge(applyLogHighestPrice1Node, 0, applyLogLowestPrice1Node, 1),
    Edge(logLowestPrice1Node, 0, applyLogLowestPrice1Node, 0),

    Edge(applyLogLowestPrice1Node, 0, applyLogReviewCount1Node, 1),
    Edge(logReviewCount1Node, 0, applyLogReviewCount1Node, 0),

    Edge(applyLogReviewCount1Node, 0, selectImportantFeaturesNode, 0),
    Edge(selectImportantFeaturesNode, 0, writeDataFrameNode, 0),
    Edge(selectImportantFeaturesNode, 0, convertBooleanToDoubleNode, 0),

    Edge(convertBooleanToDoubleNode, 0, splitNode, 0),

    Edge(splitNode, 0, trainRegressorNode, 1),
    Edge(createRidgeRegressionNode, 0, trainRegressorNode, 0),
    Edge(trainRegressorNode, 0, scoreRegressorNode, 0),
    Edge(splitNode, 1, scoreRegressorNode, 1),
    Edge(scoreRegressorNode, 0, evaluateRegressorNode, 0)
  )

  override protected def experimentName: String = "LocationAttractiveness"

  def main(args: Array[String]): Unit = {
    buildWorkflow()
  }
}
