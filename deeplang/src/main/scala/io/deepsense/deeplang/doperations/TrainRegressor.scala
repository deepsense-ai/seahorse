/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{Scorable, Trainable}
import io.deepsense.deeplang.parameters.{ColumnSelectorParameter, SingleColumnSelection, SingleColumnSelectorParameter, ParametersSchema}

class TrainRegressor extends DOperation2To1[Trainable, DataFrame, Scorable] {
  override val id: DOperation.Id = "c526714c-e7fb-11e4-b02c-1681e6b88ec1"

  override val name = "Train regressor"

  private val featureColumnsField = "feature columns"

  private val targetColumnField = "target column"

  override val parameters = ParametersSchema(
    featureColumnsField -> ColumnSelectorParameter(
      "Columns which are to be used as features in regression", required = true),
    targetColumnField -> SingleColumnSelectorParameter(
      "Column against which the regression will be performed", required = true))

  private def parametersForTrainable: Trainable.Parameters = Trainable.Parameters(
    parameters.getColumnSelection(featureColumnsField).get,
    parameters.getSingleColumnSelection(targetColumnField).get)

  override protected def _execute(
      context: ExecutionContext)(
      trainable: Trainable, dataframe: DataFrame): Scorable = {
    trainable.train(context)(parametersForTrainable)(dataframe)
  }

  override protected def _inferKnowledge(context: InferContext)(
      trainableKnowledge: DKnowledge[Trainable],
      dataframeKnowledge: DKnowledge[DataFrame]): DKnowledge[Scorable] = {

    DKnowledge(
      for (trainable <- trainableKnowledge.types)
        yield trainable.train.infer(context)(parametersForTrainable)(dataframeKnowledge))
  }
}
