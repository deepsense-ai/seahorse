/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.Report
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{ParametersSchema, SingleColumnSelectorParameter}
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}

class EvaluateRegression() extends DOperation1To1[DataFrame, Report] {
  override val name: String = "Evaluate Regression"
  override val id: Id = "f2a43e21-331e-42d3-8c02-7db1da20bc00"
  override val parameters = ParametersSchema(
    EvaluateRegression.targetColumnParamKey ->
      SingleColumnSelectorParameter("Target Column", required = true),
    EvaluateRegression.predictionColumnParamKey ->
      SingleColumnSelectorParameter("Prediction Column", required = true)
  )

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): Report = ???
}

object EvaluateRegression {
  val targetColumnParamKey = "targetColumn"
  val predictionColumnParamKey = "predictionColumn"
}
