/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

package io.deepsense.modeldeploying

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object ModelDeploingJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val modelFormat = jsonFormat5(Model)
  implicit val createResultFormat = jsonFormat1(CreateResult)
  implicit val getScoringRequestFormat = jsonFormat1(GetScoringRequest)
  implicit val scoreResult = jsonFormat1(ScoreResult)
}
