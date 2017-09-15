/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.commons.service.api

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
    message = s"Field `$fieldName` missing or invalid",
    errorCode = 422
  )

}
