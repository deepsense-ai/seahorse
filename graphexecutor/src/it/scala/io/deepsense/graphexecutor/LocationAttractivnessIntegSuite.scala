/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.doperations.FileToDataFrame.CSV
import io.deepsense.deeplang.doperations._
import io.deepsense.deeplang.parameters.ColumnType
import io.deepsense.graph._

class LocationAttractivnessIntegSuite extends GraphExecutionIntegSuite {

  override def experimentName = "Case 2: Location Attractiveness Scoring"

  val testFile = "/tests/hotels_model_matrix_head_10.csv"

  override def requiredFiles: Map[String, String] =
    Map("/csv/hotels_model_matrix_head_10.csv" -> testFile)

  val fileReader: ReadFile = ReadFile(testFile, ReadFile.unixSeparatorValue)

  val fileToDataFrame: FileToDataFrame =
    FileToDataFrame(CSV, columnSeparator = ",", namesIncluded = true)

  // min(dist to SF, dist to LA)
  val distanceOperation: MathematicalOperation = {
    val sf = Seq(37.774929, -122.419416)
    val la = Seq(34.052234, -118.243685)

    def distTo(p: Seq[Double]): String =
      s"SQRT(POW(latitude - ${p(0)}, 2.0) + POW(longitude - ${p(1)}, 2.0))"

    MathematicalOperation(s"MINIMUM(${distTo(sf)}, ${distTo(la)}) as dist_SF_LA")
  }
  val applyDistanceOperation = ApplyTransformation()

  val logHighestPrice1: DOperation =
    MathematicalOperation("LN(highest_price + 1.0) as log_highest_price1")
  val logLowestPrice1: DOperation =
    MathematicalOperation("LN(lowest_price + 1.0) as log_lowest_price1")
  val logReviewCount1: DOperation =
    MathematicalOperation("LN(review_count + 1.0) as log_review_count1")

  val applyLogHighestPrice1 = ApplyTransformation()
  val applyLogLowestPrice1 = ApplyTransformation()
  val applyLogReviewCount1 = ApplyTransformation()

  val selectImportantFeatures = SelectImportantFeatures()
  val saveDataFrame: SaveDataFrame = SaveDataFrame("C2: LAS", "Processed data of MVP Case 2")

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

  val split: DataFrameSplitter = DataFrameSplitter(splitRatio = 0.7, seed = 1)
  val createRidgeRegression: DOperation =
    CreateRidgeRegression(regularization = 0.5, iterationsNumber = 1)

  val targetColumnName = "rating"
  val predictionColumnName = "rating_prediction"
  val trainRegressor = TrainRegressor(SelectImportantFeatures.ColumnsNames.toSet, targetColumnName)
  val scoreRegressor = ScoreRegressor()
  val evaluateRegressor = EvaluateRegression(
    targetColumnName = targetColumnName,
    predictionColumnName = predictionColumnName)

  val fileReaderNode: Node = node(fileReader)
  val fileToDataFrameNode: Node = node(fileToDataFrame)
  val distanceOperationNode: Node = node(distanceOperation)
  val applyDistanceOperationNode: Node = node(applyDistanceOperation)
  val logHighestPrice1Node: Node = node(logHighestPrice1)
  val logLowestPrice1Node: Node = node(logLowestPrice1)
  val logReviewCount1Node: Node = node(logReviewCount1)
  val applyLogHighestPrice1Node: Node = node(applyLogHighestPrice1)
  val applyLogLowestPrice1Node: Node = node(applyLogLowestPrice1)
  val applyLogReviewCount1Node: Node = node(applyLogReviewCount1)
  val selectImportantFeaturesNode: Node = node(selectImportantFeatures)
  val saveDataFrameNode: Node = node(saveDataFrame)
  val splitNode: Node = node(split)
  val createRidgeRegressionNode: Node = node(createRidgeRegression)
  val convertBooleanToDoubleNode: Node = node(convertBooleanToDouble)
  val trainRegressorNode: Node = node(trainRegressor)
  val scoreRegressorNode: Node = node(scoreRegressor)
  val evaluateRegressorNode: Node = node(evaluateRegressor)

  override val nodes = Seq(fileReaderNode,
    fileToDataFrameNode,
    distanceOperationNode,
    applyDistanceOperationNode,
    logHighestPrice1Node,
    logLowestPrice1Node,
    logReviewCount1Node,
    applyLogHighestPrice1Node,
    applyLogLowestPrice1Node,
    applyLogReviewCount1Node,
    selectImportantFeaturesNode,
    saveDataFrameNode,
    convertBooleanToDoubleNode,
    splitNode,
    createRidgeRegressionNode,
    trainRegressorNode,
    scoreRegressorNode,
    evaluateRegressorNode)

  override val edges = Seq(
    Edge(fileReaderNode, 0, fileToDataFrameNode, 0),

    Edge(fileToDataFrameNode, 0, applyDistanceOperationNode, 1),
    Edge(distanceOperationNode, 0, applyDistanceOperationNode, 0),

    Edge(applyDistanceOperationNode, 0, applyLogHighestPrice1Node, 1),
    Edge(logHighestPrice1Node, 0, applyLogHighestPrice1Node, 0),

    Edge(applyLogHighestPrice1Node, 0, applyLogLowestPrice1Node, 1),
    Edge(logLowestPrice1Node, 0, applyLogLowestPrice1Node, 0),

    Edge(applyLogLowestPrice1Node, 0, applyLogReviewCount1Node, 1),
    Edge(logReviewCount1Node, 0, applyLogReviewCount1Node, 0),

    Edge(applyLogReviewCount1Node, 0, selectImportantFeaturesNode, 0),
    Edge(selectImportantFeaturesNode, 0, saveDataFrameNode, 0),
    Edge(selectImportantFeaturesNode, 0, convertBooleanToDoubleNode, 0),

    Edge(convertBooleanToDoubleNode, 0, splitNode, 0),

    Edge(splitNode, 0, trainRegressorNode, 1),
    Edge(createRidgeRegressionNode, 0, trainRegressorNode, 0),
    Edge(trainRegressorNode, 0, scoreRegressorNode, 0),
    Edge(splitNode, 1, scoreRegressorNode, 1),
    Edge(scoreRegressorNode, 0, evaluateRegressorNode, 0)
  )
}
