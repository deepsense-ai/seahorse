/**
 * Copyright (c) 2015, CodiLime Inc.
 */
package io.deepsense.deeplang.doperations

import org.apache.spark.sql
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang._
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, SingleColumnSelection}
import io.deepsense.reportlib.model.ReportContent

/**
 * Tests only mocked model trained by CrossValidateRegressor (ignores report)
 */
class CrossValidateRegressorSpec extends UnitSpec with MockitoSugar {

  val regressor = new CrossValidateRegressor
  // NOTE: When folds == 0, only regression train is performed (returned report is empty)
  regressor.parameters.getNumericParameter(CrossValidateRegressor.numOfFoldsParamKey).value =
    Some(0.0)
  regressor.parameters.getChoiceParameter(CrossValidateRegressor.shuffleParamKey).value =
    Some(CrossValidateRegressor.shuffleNo)
  regressor
    .parameters
    .getChoiceParameter(CrossValidateRegressor.shuffleParamKey)
    .options
    .get(CrossValidateRegressor.shuffleYes)
    .get
    .getNumericParameter(CrossValidateRegressor.seedParamKey)
    .value = Some(0.0)

  val trainableParametersStub = Trainable.Parameters(
    Some(mock[MultipleColumnSelection]), Some(mock[SingleColumnSelection]))

  regressor.parameters.
    getSingleColumnSelectorParameter("target column").value = trainableParametersStub.targetColumn

  regressor.parameters.
    getColumnSelectorParameter("feature columns").value = trainableParametersStub.featureColumns

  "CrossValidateRegressor with parameters set" should {
    "train untrained model on DataFrame" in {
      val trainableMock = mock[UntrainedRidgeRegression]
      val trainMethodMock = mock[DMethod1To1[Trainable.Parameters, DataFrame, Scorable]]

      val executionContextStub = mock[ExecutionContext]
      val scorableStub = mock[TrainedRidgeRegression]
      val dataframeStub = mock[DataFrame]
      val dataframeSparkStub = mock[sql.DataFrame]
      when(dataframeStub.sparkDataFrame).thenReturn(dataframeSparkStub)
      when(dataframeSparkStub.count()).thenReturn(1L)

      when(trainableMock.train).thenReturn(trainMethodMock)
      when(trainMethodMock.apply(
        executionContextStub)(trainableParametersStub)(dataframeStub)).thenReturn(scorableStub)

      val result = regressor.execute(executionContextStub)(Vector(trainableMock, dataframeStub))

      result shouldBe Vector(scorableStub, Report(ReportContent(regressor.name)))
    }

    "infer results of it's types" in {
      val regressorWithoutParams = new CrossValidateRegressor
      val inferContextStub = mock[InferContext]
      val dDOperableCatalogMock = mock[DOperableCatalog]
      when(inferContextStub.dOperableCatalog).thenReturn(dDOperableCatalogMock)
      // NOTE: We are also checking that mockedReportSet is returned by inferKnowledge
      val mockedReportSet = Set[Report]()
      when(dDOperableCatalogMock.concreteSubclassesInstances[Report]).thenReturn(mockedReportSet)

      val scorableStubs = Vector(mock[Scorable], mock[Scorable], mock[Scorable])
      val scorableKnowledgeStub1 = DKnowledge(Set(scorableStubs(0), scorableStubs(1)))
      val scorableKnowledgeStub2 = DKnowledge(Set(scorableStubs(1), scorableStubs(2)))
      val dataframeKnowledgeStub = mock[DKnowledge[DataFrame]]

      val trainableMock1 = mock[UntrainedRidgeRegression]
      val trainMethodMock1 = mock[DMethod1To1[Trainable.Parameters, DataFrame, Scorable]]

      val trainableMock2 = mock[UntrainedRidgeRegression]
      val trainMethodMock2 = mock[DMethod1To1[Trainable.Parameters, DataFrame, Scorable]]

      when(trainableMock1.train).thenReturn(trainMethodMock1)
      when(trainMethodMock1.infer(any())(any())(any())).thenReturn(scorableKnowledgeStub1)

      when(trainableMock2.train).thenReturn(trainMethodMock2)
      when(trainMethodMock2.infer(any())(any())(any())).thenReturn(scorableKnowledgeStub2)

      val result = regressorWithoutParams.inferKnowledge(
        inferContextStub)(
        Vector(DKnowledge(trainableMock1, trainableMock2), dataframeKnowledgeStub))

      result.head shouldBe DKnowledge(scorableStubs.toSet)
      result shouldBe Vector(DKnowledge(scorableStubs.toSet), DKnowledge(mockedReportSet))
    }
  }
}
