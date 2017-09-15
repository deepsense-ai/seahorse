/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.commons.rest

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.commons.models.ClusterDetails

trait ClusterDetailsJsonProtocol
  extends DefaultJsonProtocol
  with IdJsonProtocol
  with SprayJsonSupport {

  implicit val clusterDetailsFormat = jsonFormat13(ClusterDetails)
}

object ClusterDetailsJsonProtocol extends ClusterDetailsJsonProtocol
