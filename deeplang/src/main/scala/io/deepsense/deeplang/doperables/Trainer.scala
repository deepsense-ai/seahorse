/**
 * Copyright (c) 2015, CodiLime Inc.
 */
package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.parameters.{ColumnSelectorParameter, ParametersSchema, SingleColumnSelectorParameter}

trait Trainer {
  private val featureColumnsField = "feature columns"

  private val targetColumnField = "target column"

  val trainerParameters = ParametersSchema(
    featureColumnsField -> ColumnSelectorParameter(
      "Columns which are to be used as features in regression", required = true),
    targetColumnField -> SingleColumnSelectorParameter(
      "Column against which the regression will be performed", required = true))

  protected def parametersForTrainable: Trainable.Parameters = Trainable.Parameters(
    trainerParameters.getColumnSelection(featureColumnsField).get,
    trainerParameters.getSingleColumnSelection(targetColumnField).get)
}
