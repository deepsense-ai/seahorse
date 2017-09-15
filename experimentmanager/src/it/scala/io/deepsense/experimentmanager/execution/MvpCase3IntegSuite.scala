/**
 * Copyright (c) 2015, CodiLime Inc.
 */
package io.deepsense.experimentmanager.execution

import org.scalatest.Ignore

import io.deepsense.deeplang.doperations._
import io.deepsense.deeplang.parameters._
import io.deepsense.graph.{Edge, Endpoint, Node}
import io.deepsense.graphexecutor.BikesIntegSuiteEntities

// TODO: Currently this test takes too long time to execute (>15min),
// even for RegressorTrainingIterations = 1
// Un-ignore this test when performance improvements allow this test to finish after few minutes
@Ignore
class MvpCase3IntegSuite extends ExperimentExecutionSpec {

  override def executionTimeLimitSeconds = 120L

  // TODO: Increase constant if performance allows (up to 100)
  private val RegressorTrainingIterations = 1

  // TODO: Increase constant if performance allows (up to 10)
  private val CrossValidateRegressorNumOfFolds = 2

  override def experimentName = "MVP Case 3: Demand Forecasting"

  override def esFactoryName = BikesIntegSuiteEntities.Name

  override def tenantId = BikesIntegSuiteEntities.bikesTenantId

  override def requiredFiles: Map[String, String] = Map(
    "/BikesDemandDataFrame" -> BikesIntegSuiteEntities.demandLocation,
    "/BikesWeatherDataFrame" -> BikesIntegSuiteEntities.weatherLocation
  )

  val loadDFWId = Node.Id.randomId
  val loadDFDId = Node.Id.randomId
  val joinId = Node.Id.randomId
  val decomposeId = Node.Id.randomId
  val oneHotDId = Node.Id.randomId
  val convertTypeBeforeOneHotId = Node.Id.randomId
  val projectId = Node.Id.randomId
  val splitId = Node.Id.randomId
  val untraindedRRId = Node.Id.randomId
  val convertTypeBeforeSplitId = Node.Id.randomId
  val crossValidateRId = Node.Id.randomId
  val scoreRId = Node.Id.randomId
  val evaluateRId = Node.Id.randomId

  val nodes = Seq(
    // Weather DataFrame
    Node(loadDFWId, LoadDataFrame(BikesIntegSuiteEntities.weatherId.toString)),
    // Demand DataFrame
    Node(loadDFDId, LoadDataFrame(BikesIntegSuiteEntities.demandId.toString)),
    Node(joinId, joinOperation),
    Node(decomposeId, timestampDecomposer),
    Node(
      convertTypeBeforeOneHotId,
      ConvertType(
        ColumnType.categorical,
        Set("datetime_year", "datetime_month", "datetime_day", "datetime_hour"),
        Set()
    )),
    Node(oneHotDId, oneHotEncoder),
    Node(projectId, projectColumns),
    Node(splitId, Split(0.7, 123456789)),
    Node(untraindedRRId, CreateRidgeRegression(0.0000001, RegressorTrainingIterations)),
    Node(convertTypeBeforeSplitId, ConvertType(ColumnType.numeric, Set(), (1 to 76).toSet)),
    Node(crossValidateRId, crossValidateRegressor),
    Node(scoreRId, new ScoreRegressor),
    Node(evaluateRId, EvaluateRegression("log_count", "log_count_prediction"))
  )

  val edges = Seq(
    Edge(Endpoint(loadDFWId, 0), Endpoint(joinId, 0)),
    Edge(Endpoint(loadDFDId, 0), Endpoint(joinId, 1)),
    Edge(Endpoint(joinId, 0), Endpoint(decomposeId, 0)),
    Edge(Endpoint(decomposeId, 0), Endpoint(convertTypeBeforeOneHotId, 0)),
    Edge(Endpoint(convertTypeBeforeOneHotId, 0), Endpoint(oneHotDId, 0)),
    Edge(Endpoint(oneHotDId, 0), Endpoint(projectId, 0)),
    Edge(Endpoint(projectId, 0), Endpoint(convertTypeBeforeSplitId, 0)),
    Edge(Endpoint(convertTypeBeforeSplitId, 0), Endpoint(splitId, 0)),
    Edge(Endpoint(splitId, 0), Endpoint(crossValidateRId, 1)),
    Edge(Endpoint(splitId, 1), Endpoint(scoreRId, 1)),
    Edge(Endpoint(untraindedRRId, 0), Endpoint(crossValidateRId, 0)),
    Edge(Endpoint(crossValidateRId, 0), Endpoint(scoreRId, 0)),
    Edge(Endpoint(scoreRId, 0), Endpoint(evaluateRId, 0))
  )

  private def joinOperation: Join = {
    Join(
      Join.joinColumnsParameter(Seq(("datetime", "datetime"))),
      prefixLeft = None,
      prefixRight = None)
  }

  private def timestampDecomposer: DecomposeDatetime = {
    DecomposeDatetime(
      NameSingleColumnSelection("datetime"),
      Seq("year", "month", "day", "hour"),
      prefix = None)
  }

  private def oneHotEncoder: OneHotEncoder = {
    OneHotEncoder(
      MultipleColumnSelection(Vector(NameColumnSelection(Set(
        "datetime_year",
        "datetime_month",
        "datetime_day",
        "datetime_hour")))),
      true,
      None)
  }

  // TODO: Use apply
  private def projectColumns: ProjectColumns = {
    val operation = new ProjectColumns
    val valueParam = operation.parameters.getColumnSelectorParameter(operation.selectedColumns)
    valueParam.value = Some(MultipleColumnSelection(Vector(IndexColumnSelection((1 to 77).toSet))))
    operation
  }

  // TODO: Use apply
  private def crossValidateRegressor: CrossValidateRegressor = {
    val regressor = new CrossValidateRegressor
    import io.deepsense.deeplang.doperations.CrossValidateRegressor._
    regressor.parameters.getNumericParameter(numOfFoldsParamKey).value =
      Some(CrossValidateRegressorNumOfFolds)
    regressor.parameters.getChoiceParameter(CrossValidateRegressor.shuffleParamKey).value =
      Some(shuffleYes)
    regressor
      .parameters
      .getChoiceParameter(shuffleParamKey)
      .options
      .get(shuffleYes)
      .get
      .getNumericParameter(seedParamKey)
      .value = Some(0.0)
    regressor.parameters.getSingleColumnSelectorParameter("target column").value =
      Some(NameSingleColumnSelection("log_count"))
    regressor.parameters.getColumnSelectorParameter("feature columns").value =
      Some(MultipleColumnSelection(Vector(IndexColumnSelection((1 to 76).toSet))))
    regressor
  }
}
