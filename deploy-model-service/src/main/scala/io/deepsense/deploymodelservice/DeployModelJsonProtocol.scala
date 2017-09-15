/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

package io.deepsense.deploymodelservice

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

import io.deepsense.commons.json.IdJsonProtocol

object DeployModelJsonProtocol
  extends DefaultJsonProtocol
  with IdJsonProtocol
  with SprayJsonSupport {

  implicit val modelFormat = jsonFormat5(Model.apply)
  implicit val createModelResponseFormat = jsonFormat1(CreateModelResponse)
  implicit val getScoringRequestFormat = jsonFormat1(GetScoringRequest)
  implicit val scoreModelResponseFormat = jsonFormat1(ScoreModelResponse)
}
