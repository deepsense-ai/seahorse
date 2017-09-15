/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.inference

import java.util.{Scanner}
import java.io.File

import com.google.inject.Guice
import io.deepsense.deeplang.DOperable.AbstractMetadata
import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.dataframe.{CommonColumnMetadata, DataFrameMetadata}
import io.deepsense.deeplang.doperations._
import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}
import io.deepsense.deeplang.parameters.ColumnType
import io.deepsense.entitystorage.{EntityStorageClientTestInMemoryImpl, EntityStorageClient}
import io.deepsense.experimentmanager.deeplang.DeepLangModule
import io.deepsense.experimentmanager.rest.json.AbstractMetadataJsonProtocol
import io.deepsense.graph.{Graph, Node, Edge}
import io.deepsense.graphexecutor.SimpleGraphExecutionIntegSuiteEntities
import io.deepsense.models.entities.{DataObjectReference, CreateEntityRequest}
import io.deepsense.models.experiments.Experiment
import org.scalatest._
import org.scalatest.concurrent.Eventually
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Await

import io.deepsense.models.entities.Entity

import spray.json._

class LocationAttractivnessInferenceIntegSpec extends WordSpec
    with Eventually
    with Matchers
    with OptionValues
    with Inside
    with Inspectors
    with BeforeAndAfterAll
    with MockitoSugar
    with AbstractMetadataJsonProtocol {

  "ExperimentManager" should {
    s"execute experiment $experimentName" in {

      val injector = Guice.createInjector(new DeepLangModule)

      val columnNames = getColumnNames(testFile)
      val metadata = getDataFrameMetadata(columnNames)

      val (entityStorageClient, entityId) = storeEntity(metadata)

      val loadDataFrame = LoadDataFrame(entityId.toString)
      val loadDataFrameNode = node(loadDataFrame)

      val experiment = Experiment(
        Experiment.Id.randomId,
        tenantId,
        experimentName,
        Graph(nodes(loadDataFrameNode).toSet, edges(loadDataFrameNode).toSet))

      val context = new InferContext(injector.getInstance(classOf[DOperableCatalog]), true)
      context.tenantId = tenantId
      context.entityStorageClient = entityStorageClient

      val graphKnowledge = experiment.graph.inferKnowledge(context)

      val loadDataFrameInferenceResult = graphKnowledge.getResult(loadDataFrameNode.id)
      loadDataFrameInferenceResult.knowledge(0).types.head.inferredMetadata.get shouldBe metadata
      loadDataFrameInferenceResult.errors shouldBe Vector()
      loadDataFrameInferenceResult.warnings shouldBe InferenceWarnings.empty

      // TODO checks on further operations, when proper inference will be implemented
    }
  }

  private def getColumnNames(fileName: String): Array[String] = {
    val scanner = new Scanner(new File(testFile))
    val firstLine = scanner.nextLine
    firstLine.split(",")
  }

  private def getDataFrameMetadata(columnNames: Array[String]): AbstractMetadata = {
    DataFrameMetadata(
      isExact = true,
      isColumnCountExact = true,
      Map(columnNames.zipWithIndex.map(_ match {
        case (columnName, index) =>
          (columnName ->
            CommonColumnMetadata(
              columnName,
              Some(index),
              Some(ColumnType.numeric)))
      }) :_*)
    )
  }

  private def storeEntity(metadata: AbstractMetadata): (EntityStorageClient, Entity.Id) = {
    val client = new EntityStorageClientTestInMemoryImpl()

    val createEntityRequest = CreateEntityRequest(
      tenantId = tenantId,
      name = "LocationAttractivness",
      description = "Location Attractivness data frame",
      dClass = "DataFrame",
      dataReference = Some(DataObjectReference("file://" + testFile, metadata.toJson.toString)),
      report = null,
      saved = false)

    import scala.concurrent.duration._
    implicit val timeout = 5.seconds

    val future = client.createEntity(createEntityRequest)
    val entityId = Await.result(future, timeout)

    (client, entityId)
  }

  def executionTimeLimitSeconds = 240L

  def experimentName = "MVP Case 2: Location Attractiveness Inference"

  def esFactoryName = SimpleGraphExecutionIntegSuiteEntities.Name

  def tenantId = SimpleGraphExecutionIntegSuiteEntities.entityTenantId

  val testFile = "../graphexecutor/src/it/resources/csv/hotels_model_matrix_head_10.csv"

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

  val split: Split = Split(splitRatio = 0.7, seed = 1)
  val createRidgeRegression: DOperation =
    CreateRidgeRegression(regularization = 0.5, iterationsNumber = 1)

  val targetColumnName = "rating"
  val predictionColumnName = "rating_prediction"
  val trainRegressor = TrainRegressor(SelectImportantFeatures.ColumnsNames.toSet, targetColumnName)
  val scoreRegressor = ScoreRegressor(predictionColumnName)

  val evaluateRegressor = EvaluateRegression(
    targetColumnName = targetColumnName,
    predictionColumnName = predictionColumnName)

  val distanceOperationNode: Node = node(distanceOperation)
  val applyDistanceOperationNode: Node = node(applyDistanceOperation)
  val logHighestPrice1Node: Node = node(logHighestPrice1)
  val logLowestPrice1Node: Node = node(logLowestPrice1)
  val logReviewCount1Node: Node = node(logReviewCount1)
  val applyLogHighestPrice1Node: Node = node(applyLogHighestPrice1)
  val applyLogLowestPrice1Node: Node = node(applyLogLowestPrice1)
  val applyLogReviewCount1Node: Node = node(applyLogReviewCount1)
  val saveDataFrameNode: Node = node(saveDataFrame)
  val splitNode: Node = node(split)
  val createRidgeRegressionNode: Node = node(createRidgeRegression)
  val convertBooleanToDoubleNode: Node = node(convertBooleanToDouble)
  val trainRegressorNode: Node = node(trainRegressor)
  val scoreRegressorNode: Node = node(scoreRegressor)
  val evaluateRegressorNode: Node = node(evaluateRegressor)

  def nodes(loadDataFrameNode: Node) = {
    Seq(loadDataFrameNode,
      distanceOperationNode,
      applyDistanceOperationNode,
      logHighestPrice1Node,
      logLowestPrice1Node,
      logReviewCount1Node,
      applyLogHighestPrice1Node,
      applyLogLowestPrice1Node,
      applyLogReviewCount1Node,
      saveDataFrameNode,
      convertBooleanToDoubleNode,
      splitNode,
      createRidgeRegressionNode,
      trainRegressorNode,
      scoreRegressorNode,
      evaluateRegressorNode)
  }

  def edges(loadDataFrameNode: Node) = {
    Seq(
      Edge(loadDataFrameNode, 0, applyDistanceOperationNode, 1),
      Edge(distanceOperationNode, 0, applyDistanceOperationNode, 0),

      Edge(applyDistanceOperationNode, 0, applyLogHighestPrice1Node, 1),
      Edge(logHighestPrice1Node, 0, applyLogHighestPrice1Node, 0),

      Edge(applyLogHighestPrice1Node, 0, applyLogLowestPrice1Node, 1),
      Edge(logLowestPrice1Node, 0, applyLogLowestPrice1Node, 0),

      Edge(applyLogLowestPrice1Node, 0, applyLogReviewCount1Node, 1),
      Edge(logReviewCount1Node, 0, applyLogReviewCount1Node, 0),

      Edge(applyLogReviewCount1Node, 0, saveDataFrameNode, 0),
      Edge(applyLogReviewCount1Node, 0, convertBooleanToDoubleNode, 0),

      Edge(convertBooleanToDoubleNode, 0, splitNode, 0),

      Edge(splitNode, 0, trainRegressorNode, 1),
      Edge(createRidgeRegressionNode, 0, trainRegressorNode, 0),
      Edge(trainRegressorNode, 0, scoreRegressorNode, 0),
      Edge(splitNode, 1, scoreRegressorNode, 1),
      Edge(scoreRegressorNode, 0, evaluateRegressorNode, 0)
    )
  }

  protected def node(operation: DOperation): Node = Node(Node.Id.randomId, operation)

}
