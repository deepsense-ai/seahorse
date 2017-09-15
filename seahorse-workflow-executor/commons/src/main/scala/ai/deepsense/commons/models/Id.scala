/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.commons.models

import java.util.UUID

/**
 * Id model (UUID wrapper)
 * @param value UUID
 */
case class Id(value: UUID) {
  override def toString: String = value.toString
}

object Id {
  implicit def fromUuid(uuid: UUID): Id = Id(uuid)
  implicit def fromString(uuid: String): Id = Id(UUID.fromString(uuid))

  def randomId: Id = fromUuid(UUID.randomUUID())
}
