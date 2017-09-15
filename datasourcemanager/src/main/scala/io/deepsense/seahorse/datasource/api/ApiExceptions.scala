/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.api

import java.util.UUID

object ApiExceptions {

  case class PathIdMustMatchBodyId(pathId: UUID, bodyId: UUID) extends ApiExceptionWithJsonBody(
    message = s"Path id must match body id. Path id: $pathId, body id: $bodyId",
    errorCode = 400
  )

}
