/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest

import spray.httpx.SprayJsonSupport
import spray.json._
import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.sessionmanager.rest.requests.{ClusterDetails, CreateSession}
import io.deepsense.sessionmanager.rest.responses.ListSessionsResponse
import io.deepsense.sessionmanager.service.{Session, Status}

trait ClusterDetailsJsonProtocol
  extends DefaultJsonProtocol
  with IdJsonProtocol
  with SprayJsonSupport {

  implicit val clusterDetailsFormat = jsonFormat7(ClusterDetails)
}

object ClusterDetailsJsonProtocol extends ClusterDetailsJsonProtocol
