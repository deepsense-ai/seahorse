/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import scala.collection.immutable.ListMap
import scala.collection.mutable

import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRow

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters._
import io.deepsense.reportlib.model.{ReportContent, Table}

case class CrossValidateRegressor()
  extends DOperation2To2[Regressor with Trainable, DataFrame, Regressor with Scorable, Report]
  with WithTrainParameters {

  import CrossValidateRegressor._

  override val dataFramePortIndex = 1

  override val parameters = trainParameters ++ ParametersSchema(
    numOfFoldsParamKey ->
      NumericParameter(
        "Number of folds",
        Some(10.0),
        required = true,
        validator = RangeValidator(0, Int.MaxValue / 2, true, true, Some(1.0))),
    shuffleParamKey ->
      ChoiceParameter(
        "Perform shuffle",
        Some(shuffleYes),
        required = true,
        options = ListMap(
          shuffleNo -> ParametersSchema(),
          shuffleYes -> ParametersSchema(
            seedParamKey ->
              NumericParameter(
                "Seed value",
                default = Some(0.0),
                required = true,
                RangeValidator(Int.MinValue / 2, Int.MaxValue / 2, true, true, Some(1.0))
              )
          )
        )
      )
  )

  override val id: DOperation.Id = "95ca5225-b8e0-45c7-8ecd-a2c9d4d6861f"

  override val name: String = "Cross-validate Regressor"

  override protected def _execute(context: ExecutionContext)
                                 (trainable: Regressor with Trainable,
                                  dataFrame: DataFrame): (Regressor with Scorable, Report) = {
    logger.info("Execution of CrossValidateRegressor starts")

    val dataFrameSize = dataFrame.sparkDataFrame.count()
    // If number of folds is too big, we use dataFrame size as folds number
    val effectiveNumberOfFolds = math.min(
      // If dataFrame is of size 1, no folds can be performed
      if (dataFrameSize == 1) 0 else dataFrameSize,
      math.round(parameters.getDouble(numOfFoldsParamKey).get)).toInt
    val performShuffle = parameters.getChoice(shuffleParamKey).get.label == shuffleYes
    val seed =
      if (performShuffle) {
        math.round(
          parameters.getChoice(shuffleParamKey).get.selectedSchema.getDouble(seedParamKey).get)
      } else {
        0
      }

    logger.debug(
      s"Effective effectiveNumberOfFolds=$effectiveNumberOfFolds, DataFrame size=$dataFrameSize")

    // Perform shuffling if necessary
    // TODO: (DS-546) Do not use sample method, perform shuffling "in flight"
    // during splitting DataFrame on training and test DataFrame.
    val shuffledDataFrame =
      if (performShuffle) {
        context.dataFrameBuilder.buildDataFrame(
          dataFrame.sparkDataFrame.sample(withReplacement = false, 1.0, seed))
      } else {
        dataFrame
      }

    // Create initially empty report
    // Number of folds == 0 would mean that cross-validation report is not needed
    val report =
      if (effectiveNumberOfFolds > 0) {
        generateCrossValidationReport(context, trainable, shuffledDataFrame, effectiveNumberOfFolds)
      } else {
        Report(ReportContent(reportName))
      }

    logger.info("Train regressor on all data available")
    val scorable = trainable.train(context)(parametersForTrainable)(dataFrame)

    logger.info("Execution of CrossValidateRegressor ends")
    (scorable.asInstanceOf[Regressor with Scorable], report)
  }

  def generateCrossValidationReport(
      context: ExecutionContext,
      trainable: Trainable,
      dataFrame: DataFrame,
      effectiveNumberOfFolds: Int): Report = {
    logger.info("Generating cross-validation report")
    val schema = dataFrame.sparkDataFrame.schema
    val rddWithIndex: RDD[(Row, Long)] = dataFrame.sparkDataFrame.rdd.map(
      r => new GenericRow(r.toSeq.toArray).asInstanceOf[Row]).zipWithIndex().cache()

    val foldMetrics = new mutable.MutableList[RegressionMetricsRow]
    (0 to effectiveNumberOfFolds - 1).foreach {
      case splitIndex =>
        val k = effectiveNumberOfFolds
        logger.info("Preparing cross-validation report: split index [0..N-1]=" + splitIndex)
        val training =
          rddWithIndex.filter { case (r, index) => index % k != splitIndex }
            .map { case (r, index) => r }
        val test =
          rddWithIndex.filter { case (r, index) => index % k == splitIndex }
            .map { case (r, index) => r }

        val trainingSparkDataFrame = context.sqlContext.createDataFrame(training, schema)
        val trainingDataFrame = context.dataFrameBuilder.buildDataFrame(trainingSparkDataFrame)
        val testSparkDataFrame = context.sqlContext.createDataFrame(test, schema)
        val testDataFrame = context.dataFrameBuilder.buildDataFrame(testSparkDataFrame)

        // Train model on trainingDataFrame
        val trained: Scorable = trainable.train(context)(parametersForTrainable)(trainingDataFrame)

        // Score model on trainingDataFrame
        val scored = trained.score(context)(None)(testDataFrame)

        // Prepare prediction and observations RDDs to facilitate computation of metrics
        val predictions = scored.sparkDataFrame.rdd.map(r => r.getDouble(r.size - 1))
        val observations =
          testDataFrame
            .sparkDataFrame
            .select(testDataFrame.getColumnName(parametersForTrainable.targetColumn.get))
            .rdd
            .map(r => r.getDouble(0))

        // Compute metrics for current fold
        val metrics = new RegressionMetrics(predictions zip observations)
        foldMetrics +=
          RegressionMetricsRow(
            (splitIndex + 1).toString,
            training.count().toString,
            test.count().toString,
            metrics.explainedVariance,
            metrics.meanAbsoluteError,
            metrics.meanSquaredError,
            metrics.r2,
            metrics.rootMeanSquaredError)
    }
    rddWithIndex.unpersist()

    // Prepare summary row
    foldMetrics += averageRegressionMetricsRow(foldMetrics.toList)

    // Prepare Cross-validation Regression report
    val table = Table(
      CrossValidateRegressor.reportTableName,
      "",
      Some(CrossValidateRegressor.reportColumnNames),
      Some(foldMetrics.map(m => m.foldNumber).toList),
      foldMetrics.map(m => m.toRowList).toList)
    new Report(ReportContent(CrossValidateRegressor.reportName, tables = Map(table.name -> table)))
  }

  def averageRegressionMetricsRow(others: List[RegressionMetricsRow]): RegressionMetricsRow = {
    val listSize = others.size
    others.foldLeft(new RegressionMetricsRow("", "", "", 0.0, 0.0, 0.0, 0.0, 0.0)) {
      (acc, c) =>
        new RegressionMetricsRow(
          "average",
          "",
          "",
          acc.explainedVariance + c.explainedVariance / listSize,
          acc.meanAbsoluteError + c.meanAbsoluteError / listSize,
          acc.meanSquaredError + c.meanSquaredError / listSize,
          acc.r2 + c.r2 / listSize,
          acc.rootMeanSquaredError + c.rootMeanSquaredError / listSize)
    }
  }

  override protected def _inferKnowledge(context: InferContext)(
      trainableKnowledge: DKnowledge[Regressor with Trainable],
      dataframeKnowledge: DKnowledge[DataFrame]
      ): (DKnowledge[Regressor with Scorable], DKnowledge[Report]) = {
    (DKnowledge(
      for (trainable <- trainableKnowledge.types)
      yield trainable
        .train
        .infer(context)(parametersForTrainable)(dataframeKnowledge)
        .asInstanceOf[DKnowledge[Regressor with Scorable]]),
      DKnowledge(context
        .dOperableCatalog
        .concreteSubclassesInstances[Report]))
  }
}

object CrossValidateRegressor {
  val shuffleParamKey = "shuffle"
  val shuffleYes = "YES"
  val shuffleNo = "NO"

  val reportName = "Cross-validate Regressor Report"
  val reportTableName = "Cross-validate report table"

  val numOfFoldsParamKey = "num of folds"
  val seedParamKey = "seed"

  val reportColumnNames = List(
    "foldNumber",
    "trainSetSize",
    "testSetSize",
    "explainedVariance",
    "meanAbsoluteError",
    "meanSquaredError",
    "r2",
    "rootMeanSquaredError")
}

case class RegressionMetricsRow(
    foldNumber: String,
    trainSetSize: String,
    testSetSize: String,
    explainedVariance: Double,
    meanAbsoluteError: Double,
    meanSquaredError: Double,
    r2: Double,
    rootMeanSquaredError: Double) {

  def toRowList: List[Option[String]] = {
    List(
      Option(foldNumber),
      Option(trainSetSize),
      Option(testSetSize),
      Option(explainedVariance.toString),
      Option(meanAbsoluteError.toString),
      Option(meanSquaredError.toString),
      Option(r2.toString),
      Option(rootMeanSquaredError.toString))
  }
}
