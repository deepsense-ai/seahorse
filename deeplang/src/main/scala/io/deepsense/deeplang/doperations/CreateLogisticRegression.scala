/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS

import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.deeplang.{ExecutionContext, DOperation, DOperation0To1}
import io.deepsense.deeplang.doperables.UntrainedLogisticRegression

case class CreateLogisticRegression() extends DOperation0To1[UntrainedLogisticRegression] {
  override val name = "Logistic Regression"

  override val id: DOperation.Id = "ed20e602-ff91-11e4-a322-1697f925ec7b"

  override val parameters = ParametersSchema()

  override protected def _execute(context: ExecutionContext)(): UntrainedLogisticRegression = {
    val model = new LogisticRegressionWithLBFGS
    model
      .setIntercept(true)
      .setNumClasses(2)
      .setValidateData(false)
      .optimizer
      .setRegParam(0.0)
    UntrainedLogisticRegression(Some(model))
  }
}
