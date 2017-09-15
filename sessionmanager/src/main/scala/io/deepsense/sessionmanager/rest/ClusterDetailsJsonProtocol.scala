/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.sessionmanager.rest.requests.ClusterDetails

trait ClusterDetailsJsonProtocol
  extends DefaultJsonProtocol
  with IdJsonProtocol
  with SprayJsonSupport {

  implicit val clusterDetailsFormat = jsonFormat13(ClusterDetails)
}

object ClusterDetailsJsonProtocol extends ClusterDetailsJsonProtocol
