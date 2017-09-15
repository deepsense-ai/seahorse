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

package io.deepsense.deeplang.doperations

import java.util.UUID

import scala.collection.immutable.ListMap
import scala.reflect.runtime.{universe => ru}


import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRow

import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}
import io.deepsense.deeplang.parameters.{ChoiceParameter, NumericParameter, ParametersSchema, RangeValidator}
import io.deepsense.deeplang.{DKnowledge, DOperation2To2, ExecutionContext}
import io.deepsense.reportlib.model.ReportContent

abstract class CrossValidate[T <: Evaluable]()
  extends DOperation2To2[
    Trainable with T, DataFrame,
    Scorable with T, Report]
  with CrossValidateParams
  with WithTrainParameters {

  import CrossValidate._

  def reportName: String

  override val parameters = trainParameters ++ ParametersSchema(
    "Number of folds" -> numberOfFoldsParameter,
    "Shuffle" -> shuffleParameter
  )

  override protected val dataFramePortIndex: Int = 1

  override protected def _execute(
      context: ExecutionContext)(
      trainable: Trainable with T,
      dataFrame: DataFrame): (Scorable with T, Report) = {

    logger.debug("Execution of CrossValidator starts")

    val dataFrameSize = dataFrame.sparkDataFrame.count()

    val effectiveNumberOfFolds = dataFrameSize match {
      case 1 => 0  // If dataFrame is of size 1, no folds can be performed
      case _ => math.min(  // If number of folds is too big, we use dataFrame size as folds number
        dataFrameSize,
        math.round(numberOfFoldsParameter.value.get)).toInt
    }

    val seed = BinaryChoice.withName(shuffleParameter.value.get) match {
      case BinaryChoice.NO => 0
      case BinaryChoice.YES => math.round(seedShuffleParameter.value.get)
    }

    logger.debug(
      s"Effective number of folds = $effectiveNumberOfFolds, DataFrame size = $dataFrameSize")

    // TODO: (DS-546) Do not use sample method, perform shuffling "in flight"
    // during splitting DataFrame on training and test DataFrame.
    val shuffledDataFrame = BinaryChoice.withName(shuffleParameter.value.get) match {
      case BinaryChoice.NO => dataFrame
      case BinaryChoice.YES =>
        context.dataFrameBuilder.buildDataFrame(
          dataFrame.sparkDataFrame.sample(withReplacement = false, 1.0, seed))
    }

    // Number of folds == 0 means that cross-validation report is not needed
    val report =
      if (effectiveNumberOfFolds > 0) {
        generateCrossValidationReport(context, trainable, shuffledDataFrame, effectiveNumberOfFolds)
      } else {
        Report(ReportContent(reportName))
      }

    logger.debug("Train model on all available data")
    val scorable = trainable.train(context)(parametersForTrainable)(dataFrame)

    logger.debug("Execution of CrossValidator ends")
    (scorable.asInstanceOf[T with Scorable], report)
  }

  def generateCrossValidationReport(
      context: ExecutionContext,
      trainable: Trainable with T,
      dataFrame: DataFrame,
      numberOfFolds: Int): Report = {

    val schema = dataFrame.sparkDataFrame.schema
    val rddWithIndex: RDD[(Row, Long)] = dataFrame.sparkDataFrame.rdd.map(
      r => new GenericRow(r.toSeq.toArray).asInstanceOf[Row]).zipWithIndex().cache()

    val reporter : Reporter = trainable.asInstanceOf[T].getReporter

    def createFold(splitIndex: Int): Reporter.CrossValidationFold = {
      logger.debug("Preparing cross-validation report: split index [0..N-1]=" + splitIndex)
      val training = rddWithIndex
        .filter { case (r, index) => index % numberOfFolds != splitIndex }
        .map { case (r, index) => r }
      val test = rddWithIndex
        .filter { case (r, index) => index % numberOfFolds == splitIndex }
        .map { case (r, index) => r }

      val trainingDataFrame = context.dataFrameBuilder.buildDataFrame(
        context.sqlContext.createDataFrame(training, schema))
      val testDataFrame = context.dataFrameBuilder.buildDataFrame(
        context.sqlContext.createDataFrame(test, schema))

      // Train model on trainingDataFrame
      val trained: Scorable = trainable.train(context)(parametersForTrainable)(trainingDataFrame)

      // Score model on trainingDataFrame (with random column name for predictions)
      val predictionColumnName = UUID.randomUUID().toString
      val scoredDataFrame = trained.score(context)(predictionColumnName)(testDataFrame)

      val observationColumnName =
        testDataFrame.getColumnName(parametersForTrainable.targetColumn.get)

      val predictionsAndLabels =
        scoredDataFrame.sparkDataFrame
          .select(predictionColumnName, observationColumnName).rdd.map(
              r => (r.getDouble(0), r.getDouble(1)))

      Reporter.CrossValidationFold(
        trainingDataFrame.sparkDataFrame.count(),
        testDataFrame.sparkDataFrame.count(),
        predictionsAndLabels)
    }

    val report = reporter.crossValidationReport(numberOfFolds, createFold)

    rddWithIndex.unpersist()

    report
  }

  override protected def _inferKnowledge(context: InferContext)(
      trainableKnowledge: DKnowledge[Trainable with T],
      dataFrameKnowledge: DKnowledge[DataFrame]
      ): ((DKnowledge[T with Scorable], DKnowledge[Report]), InferenceWarnings) = {

    val scorableKnowledge = for {
      trainable <- trainableKnowledge.types
      (result, _) = trainable.train.infer(context)(parametersForTrainable)(dataFrameKnowledge)
    } yield result.asInstanceOf[DKnowledge[T with Scorable]]

    ((DKnowledge(scorableKnowledge), DKnowledge(Report())), InferenceWarnings.empty)
  }

  @transient
  override lazy val tTagTO_1: ru.TypeTag[Report] = ru.typeTag[Report]
  @transient
  override lazy val tTagTI_1: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

trait CrossValidateParams {
  import CrossValidate._
  val numberOfFoldsParameter = NumericParameter(
    "Number of folds",
    Some(10.0),
    required = true,
    validator = RangeValidator(
      0, Int.MaxValue / 2, beginIncluded = true, endIncluded = true, Some(1.0)),
    value = None)

  val seedShuffleParameter = NumericParameter(
    "Seed value",
    default = Some(0.0),
    required = true,
    validator = RangeValidator(
      Int.MinValue / 2, Int.MaxValue / 2, beginIncluded = true, endIncluded = true, Some(1.0)),
    value = None)

  val shuffleParameter = ChoiceParameter(
    "Perform shuffle",
    Some(BinaryChoice.YES.toString),
    required = true,
    options = ListMap(
      BinaryChoice.NO.toString -> ParametersSchema(),
      BinaryChoice.YES.toString -> ParametersSchema(
        "Seed" -> seedShuffleParameter)
    )
  )
}

object CrossValidate {
  object BinaryChoice extends Enumeration {
    type BinaryChoice = Value
    val YES = Value("Yes")
    val NO = Value("No")
  }
}
