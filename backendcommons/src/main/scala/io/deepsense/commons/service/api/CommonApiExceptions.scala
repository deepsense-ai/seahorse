/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.commons.service.api

import java.util.UUID

object CommonApiExceptions {

  case class ApiException(message: String, errorCode: Int) extends Throwable

  def pathIdMustMatchBodyId(pathId: UUID, bodyId: UUID) = ApiException(
    message = s"Path id must match body id. Path id: $pathId, body id: $bodyId",
    errorCode = 400
  )

  def doesNotExist(id: UUID) = ApiException(
    message = s"Object with id $id does not exist.",
    errorCode = 404
  )

  def forbidden = ApiException(
    message = "Forbidden",
    errorCode = 403
  )

  def fieldMustBeDefined(fieldName: String) = ApiException(
    message = s"Field $fieldName must be defined",
    errorCode = 422
  )

}
